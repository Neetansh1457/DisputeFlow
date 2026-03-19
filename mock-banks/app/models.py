from pydantic import BaseModel
from typing import Optional
from enum import Enum


class DisputeStatus(str, Enum):
    OPEN = "OPEN"
    CLOSED = "CLOSED"
    RESOLVED = "RESOLVED"
    PENDING = "PENDING"


class DisputeResponse(BaseModel):
    case_id: str
    status: DisputeStatus
    reason_code: Optional[str] = None
    description: Optional[str] = None
    bank_name: str


class UploadResponse(BaseModel):
    case_id: str
    success: bool
    message: str


class AcceptResponse(BaseModel):
    case_id: str
    success: bool
    message: str