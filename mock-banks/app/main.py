from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.banks import amex, hsbc, pmtc, chase
import logging

logging.basicConfig(level=logging.INFO)

app = FastAPI(
    title="DisputeFlow Mock Bank APIs",
    description="Simulates real bank portal behaviour for testing",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register all bank routers
app.include_router(amex.router)
app.include_router(hsbc.router)
app.include_router(pmtc.router)
app.include_router(chase.router)


@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "disputeflow-mock-banks",
        "banks": ["AMEX", "HSBC", "PMTC", "CHASE"]
    }