import random
import time
from typing import Optional


# Simulates realistic bank portal behaviour
class ScenarioSimulator:

    def __init__(self, bank_name: str, failure_rate: float = 0.1,
                 timeout_rate: float = 0.05, timeout_seconds: float = 2.0):
        self.bank_name = bank_name
        self.failure_rate = failure_rate
        self.timeout_rate = timeout_rate
        self.timeout_seconds = timeout_seconds

    def simulate_portal_call(self) -> Optional[str]:
        """
        Returns None if call succeeds.
        Returns error message if call fails.
        """
        # Simulate timeout
        if random.random() < self.timeout_rate:
            time.sleep(self.timeout_seconds)
            return f"{self.bank_name} portal timed out"

        # Simulate failure
        if random.random() < self.failure_rate:
            return f"{self.bank_name} portal returned an error"

        # Simulate realistic response time (0.2 - 1.5 seconds)
        time.sleep(random.uniform(0.2, 1.5))
        return None

    def get_dispute_status(self, case_id: str) -> dict:
        """
        Determines dispute status based on case ID patterns.
        Makes testing predictable without being completely static.
        """
        # Case IDs ending in 99 are always closed
        if case_id.endswith("99"):
            return {"status": "CLOSED", "reason_code": None}

        # Case IDs ending in 00 are reversals
        if case_id.endswith("00"):
            return {"status": "OPEN", "reason_code": self._get_reversal_code()}

        # Everything else is open
        return {"status": "OPEN", "reason_code": None}

    def _get_reversal_code(self) -> Optional[str]:
        return None


# One simulator per bank with different characteristics
SIMULATORS = {
    "amex":  ScenarioSimulator("AMEX",  failure_rate=0.05, timeout_rate=0.03),
    "hsbc":  ScenarioSimulator("HSBC",  failure_rate=0.10, timeout_rate=0.05),
    "pmtc":  ScenarioSimulator("PMTC",  failure_rate=0.08, timeout_rate=0.04),
    "chase": ScenarioSimulator("CHASE", failure_rate=0.12, timeout_rate=0.06),
}