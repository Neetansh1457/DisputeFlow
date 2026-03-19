from fastapi import FastAPI

app = FastAPI(
    title="DisputeFlow Processor",
    description="Python processing engine for DisputeFlow",
    version="1.0.0"
)

@app.get("/health")
def health():
    return {"status": "ok", "service": "disputeflow-processor"}