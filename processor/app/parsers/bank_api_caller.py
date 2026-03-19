import requests
from dataclasses import dataclass
from typing import Optional


@dataclass
class BankApiResponse:
    success: bool
    dispute_status: Optional[str] = None
    reason_code: Optional[str] = None
    error_message: Optional[str] = None


def check_dispute_status(
    mock_endpoint: str,
    case_id: str,
    timeout_seconds: int = 30
) -> BankApiResponse:
    """
    Calls the mock bank API to check the current dispute status.
    Returns the dispute status and reason code if available.
    """
    try:
        response = requests.get(
            f"{mock_endpoint}/dispute/{case_id}",
            timeout=timeout_seconds
        )

        if response.status_code == 200:
            data = response.json()
            return BankApiResponse(
                success=True,
                dispute_status=data.get("status"),
                reason_code=data.get("reason_code")
            )

        return BankApiResponse(
            success=False,
            error_message=f"Bank API returned status {response.status_code}"
        )

    except requests.Timeout:
        return BankApiResponse(
            success=False,
            error_message="Bank portal timed out"
        )
    except requests.ConnectionError:
        return BankApiResponse(
            success=False,
            error_message="Bank portal is unreachable"
        )
    except Exception as e:
        return BankApiResponse(
            success=False,
            error_message=f"Unexpected error: {str(e)}"
        )


def upload_document(
    mock_endpoint: str,
    case_id: str,
    file_bytes: bytes,
    filename: str,
    timeout_seconds: int = 30
) -> BankApiResponse:
    """
    Uploads a document to the mock bank portal.
    """
    try:
        files = {"file": (filename, file_bytes, "application/pdf")}
        data = {"case_id": case_id}

        response = requests.post(
            f"{mock_endpoint}/dispute/{case_id}/upload",
            files=files,
            data=data,
            timeout=timeout_seconds
        )

        if response.status_code == 200:
            return BankApiResponse(success=True)

        return BankApiResponse(
            success=False,
            error_message=f"Upload failed with status {response.status_code}"
        )

    except requests.Timeout:
        return BankApiResponse(
            success=False,
            error_message="Bank portal timed out during upload"
        )
    except requests.ConnectionError:
        return BankApiResponse(
            success=False,
            error_message="Bank portal unreachable during upload"
        )
    except Exception as e:
        return BankApiResponse(
            success=False,
            error_message=f"Upload error: {str(e)}"
        )


def accept_dispute(
    mock_endpoint: str,
    case_id: str,
    reason_code: str,
    timeout_seconds: int = 30
) -> BankApiResponse:
    """
    Accepts a dispute on the mock bank portal.
    Used when reason code indicates write-off or reversal.
    """
    try:
        response = requests.post(
            f"{mock_endpoint}/dispute/{case_id}/accept",
            json={"case_id": case_id, "reason_code": reason_code},
            timeout=timeout_seconds
        )

        if response.status_code == 200:
            return BankApiResponse(success=True)

        return BankApiResponse(
            success=False,
            error_message=f"Accept failed with status {response.status_code}"
        )

    except Exception as e:
        return BankApiResponse(
            success=False,
            error_message=f"Accept error: {str(e)}"
        )