from fastapi import APIRouter, UploadFile, File
from app.models import DisputeResponse, UploadResponse, AcceptResponse
from app.simulator import SIMULATORS
import logging

router = APIRouter(prefix="/amex", tags=["AMEX"])
logger = logging.getLogger(__name__)
simulator = SIMULATORS["amex"]


@router.get("/dispute/{case_id}", response_model=DisputeResponse)
def check_dispute(case_id: str):
    logger.info(f"AMEX — checking dispute: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        from fastapi import HTTPException
        raise HTTPException(status_code=503, detail=error)

    dispute = simulator.get_dispute_status(case_id)

    # AMEX specific — S01 is a write-off code
    if dispute["status"] == "OPEN" and case_id.endswith("00"):
        dispute["reason_code"] = "S01"

    return DisputeResponse(
        case_id=case_id,
        status=dispute["status"],
        reason_code=dispute["reason_code"],
        description="AMEX dispute record",
        bank_name="AMEX"
    )


@router.post("/dispute/{case_id}/upload", response_model=UploadResponse)
async def upload_document(case_id: str, file: UploadFile = File(...)):
    logger.info(f"AMEX — uploading document for case: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        return UploadResponse(case_id=case_id, success=False, message=error)

    contents = await file.read()
    if len(contents) == 0:
        return UploadResponse(
            case_id=case_id,
            success=False,
            message="Empty file received"
        )

    logger.info(f"AMEX — upload successful for case: {case_id}")
    return UploadResponse(
        case_id=case_id,
        success=True,
        message="Document uploaded successfully to AMEX portal"
    )


@router.post("/dispute/{case_id}/accept", response_model=AcceptResponse)
def accept_dispute(case_id: str):
    logger.info(f"AMEX — accepting dispute: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        return AcceptResponse(case_id=case_id, success=False, message=error)

    return AcceptResponse(
        case_id=case_id,
        success=True,
        message="Dispute accepted on AMEX portal"
    )