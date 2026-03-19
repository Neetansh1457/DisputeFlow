import hashlib
from dataclasses import dataclass
from typing import Optional
import PyPDF2
import io


@dataclass
class ValidationResult:
    is_valid: bool
    file_hash: Optional[str] = None
    page_count: Optional[int] = None
    issue_reason: Optional[str] = None


def validate_pdf(file_bytes: bytes, filename: str) -> ValidationResult:
    """
    Validates a PDF file:
    - Checks it is actually a PDF
    - Checks it is not corrupted
    - Computes MD5 hash for duplicate detection
    - Returns page count
    """
    if not file_bytes:
        return ValidationResult(is_valid=False, issue_reason="File is empty")

    # Check file size — 10MB limit
    if len(file_bytes) > 10 * 1024 * 1024:
        return ValidationResult(
            is_valid=False,
            issue_reason="File exceeds 10MB limit"
        )

    # Check PDF magic bytes — every valid PDF starts with %PDF
    if not file_bytes.startswith(b'%PDF'):
        return ValidationResult(
            is_valid=False,
            issue_reason="File is not a valid PDF"
        )

    # Try to read the PDF
    try:
        reader = PyPDF2.PdfReader(io.BytesIO(file_bytes))
        page_count = len(reader.pages)

        if page_count == 0:
            return ValidationResult(
                is_valid=False,
                issue_reason="PDF has no pages"
            )

    except Exception as e:
        return ValidationResult(
            is_valid=False,
            issue_reason=f"PDF is corrupted or unreadable: {str(e)}"
        )

    # Compute MD5 hash
    file_hash = hashlib.md5(file_bytes).hexdigest()

    return ValidationResult(
        is_valid=True,
        file_hash=file_hash,
        page_count=page_count
    )