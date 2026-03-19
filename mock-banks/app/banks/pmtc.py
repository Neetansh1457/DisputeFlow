from fastapi import APIRouter, UploadFile, File, HTTPException
from app.models import DisputeResponse, UploadResponse, AcceptResponse
from app.simulator import SIMULATORS
import logging

router = APIRouter(prefix="/pmtc", tags=["PMTC"])
logger = logging.getLogger(__name__)
simulator = SIMULATORS["pmtc"]


@router.get("/dispute/{case_id}", response_model=DisputeResponse)
def check_dispute(case_id: str):
    logger.info(f"PMTC — checking dispute: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        raise HTTPException(status_code=503, detail=error)

    dispute = simulator.get_dispute_status(case_id)

    # PMTC specific — code 98 is a reversal
    if dispute["status"] == "OPEN" and case_id.endswith("00"):
        dispute["reason_code"] = "98"

    return DisputeResponse(
        case_id=case_id,
        status=dispute["status"],
        reason_code=dispute["reason_code"],
        description="PMTC dispute record",
        bank_name="PMTC"
    )


@router.post("/dispute/{case_id}/upload", response_model=UploadResponse)
async def upload_document(case_id: str, file: UploadFile = File(...)):
    logger.info(f"PMTC — uploading document for case: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        return UploadResponse(case_id=case_id, success=False, message=error)

    contents = await file.read()
    if len(contents) == 0:
        return UploadResponse(case_id=case_id, success=False, message="Empty file")

    return UploadResponse(
        case_id=case_id,
        success=True,
        message="Document uploaded successfully to PMTC portal"
    )


@router.post("/dispute/{case_id}/accept", response_model=AcceptResponse)
def accept_dispute(case_id: str):
    logger.info(f"PMTC — accepting dispute: {case_id}")

    error = simulator.simulate_portal_call()
    if error:
        return AcceptResponse(case_id=case_id, success=False, message=error)

    return AcceptResponse(
        case_id=case_id,
        success=True,
        message="Dispute accepted on PMTC portal"
    )