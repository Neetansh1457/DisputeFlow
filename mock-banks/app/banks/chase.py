from fastapi import APIRouter, UploadFile, File, HTTPException
from app.models import DisputeResponse, UploadResponse, AcceptResponse
from app.simulator import SIMULATORS
import logging

router = APIRouter(prefix="/chase", tags=["CHASE"])
logger = logging.getLogger(__name__)
simulator = SIMULATORS["chase"]


@router.get("/dispute/{case_id}", response_model=DisputeResponse)
def check_dispute(case_id: str):
    logger.info(f"CHASE — checking dispute: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        raise HTTPException(status_code=503, detail=error)

    dispute = simulator.get_dispute_status(case_id)

    return DisputeResponse(
        case_id=case_id,
        status=dispute["status"],
        reason_code=dispute["reason_code"],
        description="CHASE dispute record",
        bank_name="CHASE"
    )


@router.post("/dispute/{case_id}/upload", response_model=UploadResponse)
async def upload_document(case_id: str, file: UploadFile = File(...)):
    logger.info(f"CHASE — uploading document for case: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        return UploadResponse(case_id=case_id, success=False, message=error)

    contents = await file.read()
    if len(contents) == 0:
        return UploadResponse(case_id=case_id, success=False, message="Empty file")

    return UploadResponse(
        case_id=case_id,
        success=True,
        message="Document uploaded successfully to CHASE portal"
    )


@router.post("/dispute/{case_id}/accept", response_model=AcceptResponse)
def accept_dispute(case_id: str):
    logger.info(f"CHASE — accepting dispute: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        return AcceptResponse(case_id=case_id, success=False, message=error)

    return AcceptResponse(
        case_id=case_id,
        success=True,
        message="Dispute accepted on CHASE portal"
    )