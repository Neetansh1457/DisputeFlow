from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import logging

from app.parsers.filename_parser import parse_filename
from app.validators.pdf_validator import validate_pdf
from app.parsers.decision_router import make_decision, DecisionAction
from app.parsers.bank_api_caller import check_dispute_status, upload_document, accept_dispute

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="DisputeFlow Processor",
    description="Python processing engine for DisputeFlow",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class ProcessRequest(BaseModel):
    job_id: str
    bank_prefix: str
    bank_name: str
    mock_endpoint: str
    case_id: str
    reason_code: Optional[str] = None
    document_type: str = "REPRESENTATION"
    timeout_seconds: int = 30


class ProcessResponse(BaseModel):
    job_id: str
    action_taken: str
    success: bool
    dispute_status: Optional[str] = None
    reason_code: Optional[str] = None
    file_hash: Optional[str] = None
    auto_processed: bool = False
    remarks: Optional[str] = None
    error_message: Optional[str] = None


class PreviewRequest(BaseModel):
    file_names: list[str]


@app.get("/health")
def health():
    return {"status": "ok", "service": "disputeflow-processor"}


@app.post("/process", response_model=ProcessResponse)
async def process_job(
    request: ProcessRequest,
    file: UploadFile = File(...)
):
    """
    Main processing endpoint.
    Called by Spring Boot Kafka consumer for each upload job.
    """
    logger.info(f"Processing job {request.job_id} for bank {request.bank_name}")

    # 1. Read and validate PDF
    file_bytes = await file.read()
    validation = validate_pdf(file_bytes, file.filename)

    if not validation.is_valid:
        logger.warning(f"PDF validation failed for job {request.job_id}: {validation.issue_reason}")
        return ProcessResponse(
            job_id=request.job_id,
            action_taken="FAILED",
            success=False,
            error_message=validation.issue_reason
        )

    # 2. Check dispute status on bank portal
    bank_response = check_dispute_status(
        mock_endpoint=request.mock_endpoint,
        case_id=request.case_id,
        timeout_seconds=request.timeout_seconds
    )

    dispute_status = bank_response.dispute_status if bank_response.success else None
    reason_code = request.reason_code or (
        bank_response.reason_code if bank_response.success else None
    )

    # 3. Make decision based on reason code + dispute status
    decision = make_decision(
        bank_prefix=request.bank_prefix,
        reason_code=reason_code,
        dispute_status=dispute_status
    )

    logger.info(f"Decision for job {request.job_id}: {decision.action}")

    # 4. Execute decision
    if decision.action == DecisionAction.ACCEPT:
        result = accept_dispute(
            mock_endpoint=request.mock_endpoint,
            case_id=request.case_id,
            reason_code=reason_code or "",
            timeout_seconds=request.timeout_seconds
        )
        return ProcessResponse(
            job_id=request.job_id,
            action_taken="ACCEPTED",
            success=result.success,
            dispute_status=dispute_status,
            reason_code=reason_code,
            file_hash=validation.file_hash,
            auto_processed=decision.auto_processed,
            remarks=decision.remarks,
            error_message=result.error_message
        )

    elif decision.action == DecisionAction.SKIP:
        return ProcessResponse(
            job_id=request.job_id,
            action_taken="SKIPPED",
            success=True,
            dispute_status=dispute_status,
            reason_code=reason_code,
            file_hash=validation.file_hash,
            auto_processed=decision.auto_processed,
            remarks=decision.remarks
        )

    elif decision.action == DecisionAction.FLAG_FOR_REVIEW:
        return ProcessResponse(
            job_id=request.job_id,
            action_taken="FLAGGED_FOR_REVIEW",
            success=True,
            dispute_status=dispute_status,
            reason_code=reason_code,
            file_hash=validation.file_hash,
            auto_processed=False,
            remarks=decision.remarks
        )

    else:
        # Default — UPLOAD
        if not bank_response.success:
            return ProcessResponse(
                job_id=request.job_id,
                action_taken="FAILED",
                success=False,
                file_hash=validation.file_hash,
                error_message=f"Cannot upload — bank portal unavailable: {bank_response.error_message}"
            )

        result = upload_document(
            mock_endpoint=request.mock_endpoint,
            case_id=request.case_id,
            file_bytes=file_bytes,
            filename=file.filename,
            timeout_seconds=request.timeout_seconds
        )

        return ProcessResponse(
            job_id=request.job_id,
            action_taken="UPLOADED",
            success=result.success,
            dispute_status=dispute_status,
            reason_code=reason_code,
            file_hash=validation.file_hash,
            auto_processed=decision.auto_processed,
            remarks=decision.remarks,
            error_message=result.error_message
        )


@app.post("/preview")
def preview_files(request: PreviewRequest):
    """
    Dry run — parses filenames and returns what was detected.
    Called before batch submission to show the confirmation table.
    """
    previews = []
    for filename in request.file_names:
        parsed = parse_filename(filename)
        previews.append({
            "file_name": filename,
            "detected_bank": parsed.bank_prefix,
            "detected_case_id": parsed.case_id,
            "detected_reason_code": parsed.reason_code,
            "is_ready": parsed.is_valid,
            "issue_reason": parsed.issue_reason
        })

    ready_count = sum(1 for p in previews if p["is_ready"])

    return {
        "previews": previews,
        "total_files": len(previews),
        "ready_count": ready_count,
        "needs_review_count": len(previews) - ready_count
    }