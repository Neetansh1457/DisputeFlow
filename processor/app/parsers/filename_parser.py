import re
from dataclasses import dataclass
from typing import Optional


@dataclass
class ParsedFilename:
    bank_prefix: Optional[str] = None
    case_id: Optional[str] = None
    reason_code: Optional[str] = None
    is_valid: bool = False
    issue_reason: Optional[str] = None


def parse_filename(filename: str) -> ParsedFilename:
    """
    Parses a PDF filename to extract bank prefix, case ID, and reason code.

    Expected formats:
        AMEX_123456.pdf
        AMEX_123456_S01.pdf
        HSBC_MON789012.pdf
        PMTC_789012_98.pdf
    """
    if not filename:
        return ParsedFilename(is_valid=False, issue_reason="Filename is empty")

    # Remove file extension
    name = re.sub(r'\.pdf$', '', filename, flags=re.IGNORECASE).strip()

    if not name:
        return ParsedFilename(is_valid=False, issue_reason="Filename has no content")

    # Split by underscore
    parts = name.split('_')

    if len(parts) < 2:
        return ParsedFilename(
            is_valid=False,
            issue_reason="Filename must follow format: BANK_CASEID.pdf"
        )

    bank_prefix = parts[0].upper()
    case_id = parts[1]
    reason_code = parts[2].upper() if len(parts) >= 3 else None

    # Validate bank prefix — letters only
    if not re.match(r'^[A-Z]{2,10}$', bank_prefix):
        return ParsedFilename(
            is_valid=False,
            issue_reason=f"Invalid bank prefix: {bank_prefix}"
        )

    common_words = {"UNKNOWN", "TEST", "SAMPLE", "DEMO", "FILE", "DOC", "NEW"}
    if bank_prefix in common_words:
        return ParsedFilename(
            is_valid=False,
            issue_reason=f"Could not detect a valid bank from filename"
        )

    # Validate case ID — alphanumeric only
    if not re.match(r'^[A-Za-z0-9]+$', case_id):
        return ParsedFilename(
            is_valid=False,
            issue_reason=f"Invalid case ID format: {case_id}"
        )

    return ParsedFilename(
        bank_prefix=bank_prefix,
        case_id=case_id,
        reason_code=reason_code,
        is_valid=True
    )