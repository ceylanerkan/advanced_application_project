import os
import uvicorn
import logging
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
from dotenv import load_dotenv

# Import your compiled LangGraph from your separate file
from secure_chatbot_graph import secure_chatbot_app

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

# Load environment variables (like GROQ_API_KEY)
load_dotenv()
if not os.getenv("GROQ_API_KEY"):
    raise ValueError("GROQ_API_KEY is missing. Check your .env file.")

# ==========================================
# 1. Define Data Models (Matches Java DTOs)
# ==========================================

# This matches the AIPayloadDTO coming from Spring Boot
class AIRequestPayload(BaseModel):
    question: str
    sessionId: Optional[str] = None
    currentUserId: int
    currentUserRole: str

# This matches the AIResponseDTO expected by Spring Boot
class AIResponsePayload(BaseModel):
    final_answer: Optional[str] = None
    visualization_code: Optional[str] = None
    status: str
    error: Optional[str] = None

# ==========================================
# 2. FastAPI Application Setup
# ==========================================
app = FastAPI(
    title="E-Commerce AI Agent API",
    description="LangGraph Multi-Agent Backend for Spring Boot Integration",
    version="1.0"
)

# ==========================================
# 3. API Endpoints
# ==========================================

@app.post("/api/ai/invoke", response_model=AIResponsePayload)
async def invoke_agent(payload: AIRequestPayload):
    """
    Receives user context and query from Spring Boot, runs the LangGraph, 
    and returns the structured answer.
    """
    logging.info(f"Received query from User {payload.currentUserId} (Role: {payload.currentUserRole})")
    
    # Initialize the LangGraph state mapping the incoming Java payload
    initial_state = {
        "question": payload.question,
        "sql_query": None,
        "query_result": None,
        "error": None,
        "final_answer": None,
        "visualization_code": None,
        "is_in_scope": True,
        "iteration_count": 0,
        "current_user_id": payload.currentUserId,
        "current_user_role": payload.currentUserRole
    }

    try:
        # Execute the LangGraph workflow
        result_state = secure_chatbot_app.invoke(initial_state)
        
        # Check if the Guardrails agent blocked the query
        if not result_state.get("is_in_scope"):
            return AIResponsePayload(
                final_answer=result_state.get("final_answer"),
                status="rejected",
                error="Query was blocked by Guardrails Agent due to out-of-scope or security violation."
            )

        # Successful execution
        return AIResponsePayload(
            final_answer=result_state.get("final_answer"),
            visualization_code=result_state.get("visualization_code"),
            status="success",
            error=None
        )

    except Exception as e:
        logging.error(f"Graph Execution Failed: {e}")
        # Return a safe error structure back to Spring Boot
        return AIResponsePayload(
            final_answer=None,
            visualization_code=None,
            status="error",
            error=str(e)
        )

# ==========================================
# 4. Server Execution
# ==========================================
if __name__ == "__main__":
    # Runs the FastAPI server on port 8000
    logging.info("Starting LangGraph AI Server on port 8000...")
    uvicorn.run(app, host="0.0.0.0", port=8000)