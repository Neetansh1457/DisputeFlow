from dataclasses import dataclass
from typing import Optional
from enum import Enum


class DecisionAction(str, Enum):
    UPLOAD = "UPLOAD"
    ACCEPT = "ACCEPT"
    SKIP = "SKIP"
    FLAG_FOR_REVIEW = "FLAG_FOR_REVIEW"


@dataclass
class ProcessingDecision:
    action: DecisionAction
    reason_code: Optional[str] = None
    remarks: Optional[str] = None
    auto_processed: bool = False


# Reason code rules per bank
REASON_CODE_RULES = {
    "AMEX": {
        "S01": DecisionAction.ACCEPT,
        "S02": DecisionAction.ACCEPT,
    },
    "PMTC": {
        "98": DecisionAction.ACCEPT,
        "37": DecisionAction.SKIP,
    }
}


def make_decision(
    bank_prefix: str,
    reason_code: Optional[str],
    dispute_status: Optional[str]
) -> ProcessingDecision:
    """
    Applies reason code rules to decide what action to take.

    Priority:
    1. If dispute is already closed → SKIP
    2. If reason code matches a known rule → apply that rule
    3. If reason code is unknown → FLAG_FOR_REVIEW
    4. If no reason code → default to UPLOAD
    """

    # Check dispute status first
    if dispute_status and dispute_status.upper() in ["CLOSED", "RESOLVED"]:
        return ProcessingDecision(
            action=DecisionAction.SKIP,
            remarks="Dispute already closed on bank portal",
            auto_processed=True
        )

    # No reason code — default to upload
    if not reason_code:
        return ProcessingDecision(
            action=DecisionAction.UPLOAD,
            remarks="No reason code — proceeding with document upload",
            auto_processed=True
        )

    # Look up reason code rules for this bank
    bank_rules = REASON_CODE_RULES.get(bank_prefix.upper(), {})
    action = bank_rules.get(reason_code.upper())

    if action == DecisionAction.ACCEPT:
        return ProcessingDecision(
            action=DecisionAction.ACCEPT,
            reason_code=reason_code,
            remarks=f"Reason code {reason_code} → auto-accepted dispute",
            auto_processed=True
        )

    if action == DecisionAction.SKIP:
        return ProcessingDecision(
            action=DecisionAction.SKIP,
            reason_code=reason_code,
            remarks=f"Reason code {reason_code} → dispute skipped",
            auto_processed=True
        )

    # Unknown reason code — flag for manual review
    return ProcessingDecision(
        action=DecisionAction.FLAG_FOR_REVIEW,
        reason_code=reason_code,
        remarks=f"Unknown reason code {reason_code} for bank {bank_prefix} — needs manual review",
        auto_processed=False
    )