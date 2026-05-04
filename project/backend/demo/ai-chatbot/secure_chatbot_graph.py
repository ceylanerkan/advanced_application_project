import json
import re
from typing import TypedDict, Optional, Union, Dict, Any
from langgraph.graph import StateGraph, END
from sqlalchemy import create_engine, text

from agents import (
    guardrails_agent, sql_agent, analysis_agent, 
    visualization_agent, error_agent,translator_agent
)

# --- 0. Database Configuration ---
DB_URI = "mysql+pymysql://root:admin@localhost:3306/advanced_project" 
engine = create_engine(DB_URI)

class AgentState(TypedDict):
    question: str
    english_question: str
    sql_query: Optional[str]
    query_result: Optional[Union[str, Dict[str, Any]]]
    error: Optional[str]
    final_answer: Optional[str]
    visualization_code: Optional[str]
    is_in_scope: bool
    message_type: str 
    iteration_count: int
    current_user_id: int
    current_user_role: str

# --- 2. Node Functions ---

# In secure_chatbot_graph.py

# In secure_chatbot_graph.py
def guardrails_node(state: AgentState):
    # 1. Translate
    translation_response = translator_agent.invoke({"input": state['question']})
    english_q = translation_response.content.strip()
    
    print(f"\n[DEBUG] ORIGINAL TURKISH: {state['question']}")
    print(f"[DEBUG] TRANSLATED ENGLISH: {english_q}")
    
    # 2. Guardrails
    prompt_input = f"User Role: {state['current_user_role']}\nQuestion: {english_q}"
    response = guardrails_agent.invoke({"input": prompt_input})
    res = response.content.upper()
    
    print(f"[DEBUG] GUARDRAIL DECISION: {res}\n")
    
    # 3. Route
    if "[REJECT]" in res:
        rejection_message = response.content.replace("[REJECT]", "").strip()
        return {"is_in_scope": False, "final_answer": rejection_message, "message_type": "REJECT", "english_question": english_q}
    if "[GREETING]" in res:
        return {"is_in_scope": True, "message_type": "GREETING", "english_question": english_q}
    
    return {"is_in_scope": True, "message_type": "DATA_QUERY", "english_question": english_q}
# In secure_chatbot_graph.py
def sql_node(state: AgentState):
    # Use the english_question here
    prompt_input = f"User: {state['current_user_id']} ({state['current_user_role']})\nQ: {state['english_question']}"
    
    if state.get("error"):
        prompt_input += f"\nFix this syntax error: {state['error']}\nPrevious SQL: {state['sql_query']}"
        response = error_agent.invoke({"input": prompt_input})
    else:
        response = sql_agent.invoke({"input": prompt_input})
    
    content = response.content.strip()
    match = re.search(r'```sql\s*(.*?)\s*```', content, re.DOTALL | re.IGNORECASE)
    
    if match:
        sql = match.group(1).strip()
    else:
        sql = content.replace('```sql', '').replace('```', '').strip()
        
    return {"sql_query": sql, "iteration_count": state.get("iteration_count", 0) + 1}

def execute_sql_node(state: AgentState):
    sql = state.get("sql_query", "")
    role = state.get("current_user_role", "USER")
    user_id = str(state.get("current_user_id"))
    
    print(f"\n[DEBUG] EXECUTING SQL: {sql}\n")
    
    # 1. [AV-03 & AV-11] Prevent SQL Injection and Mass Assignment Write Operations
    forbidden = r"\b(DROP|DELETE|UPDATE|INSERT|ALTER|TRUNCATE|GRANT|REVOKE)\b"
    if re.search(forbidden, sql.upper()):
        print("[DEBUG] SECURITY BLOCK: Unauthorized DML/DDL command.")
        return {"error": "Security Violation: Only SELECT commands are allowed.", "query_result": None}
        
    # 2. [AV-12] Prevent SELECT * Exfiltration and Sensitive Columns Exposure
    sensitive_columns = r"\b(password_hash|internal_cost|supplier_margin|api_key)\b"
    if re.search(r'\bSELECT\s+\*|,\s*\*|\b[a-zA-Z_]\w*\.\*', sql, re.IGNORECASE) or re.search(sensitive_columns, sql.lower()):
        print("[DEBUG] SECURITY BLOCK: Sensitive column or SELECT * detected.")
        return {"error": "Security Violation: Broad column selection or sensitive data exposure is prohibited.", "query_result": None}
        
    # 3. [AV-01, AV-02, AV-05, AV-10] DETERMINISTIC SECURITY CHECK (BOLA & Cross-Tenant)
    # If the user is NOT an admin, their user_id MUST appear in the SQL in a meaningful context
    # (e.g. after = or inside IN(...)) to prevent cross-tenant data leaks.
    if role != "ADMIN":
        # Match patterns like: = 5, =5, IN(5, ...), IN (5, ...)
        user_id_in_context = re.search(
            r'=\s*' + re.escape(user_id) + r'\b|IN\s*\([^)]*\b' + re.escape(user_id) + r'\b',
            sql,
            re.IGNORECASE
        )
        if not user_id_in_context:
            print("[DEBUG] DETERMINISTIC BLOCK: User ID not found in SQL WHERE/IN context.")
            return {
                "error": "Security Violation: Insufficient privileges. You can only view your own data.",
                "query_result": None
            }
    
    try: 
        with engine.connect() as conn:
            result = conn.execute(text(sql))
            rows = [dict(row._mapping) for row in result]
            return {"query_result": json.dumps(rows, default=str), "error": None}
    except Exception as e:
        return {"error": str(e), "query_result": None}
    
def analysis_node(state: AgentState):
    error = state.get("error")
    
    # If a security violation occurred, output the exact hardcoded English error directly.
    # Do not let the analysis_agent try to "explain" it.
    if error and "Security Violation" in error:
        return {"final_answer": error}

    # Otherwise, handle normal analysis
    if state.get("message_type") == "GREETING":
        prompt_input = f"Greet the user in their language: {state['question']}"
    elif error:
        prompt_input = f"Explain this syntax error in the user's language: {error}"
    else:
        prompt_input = f"Data: {state['query_result']}\nAnswer this question in its original language: {state['question']}"
        
    response = analysis_agent.invoke({"input": prompt_input})
    return {"final_answer": response.content}

def visualization_node(state: AgentState):
    prompt_input = f"Data: {state['query_result']}\nUser Request: {state['question']}\nGenerate ONLY valid Plotly JSON."
    response = visualization_agent.invoke({"input": prompt_input})
    
    # Extract JSON cleanly in case the LLM wraps it in markdown blocks
    content = response.content.strip()
    match = re.search(r'```json\s*(.*?)\s*```', content, re.DOTALL | re.IGNORECASE)
    json_code = match.group(1).strip() if match else content.replace('```json', '').replace('```', '').strip()
    
    return {
        "visualization_code": json_code, 
        "final_answer": "İşte verilerinizin grafiği:" # Short text to accompany the chart
    }

# --- 3. Routing & Graph ---
def check_scope(state: AgentState):
    if not state.get("is_in_scope"): return END
    return "analysis_node" if state.get("message_type") == "GREETING" else "sql_node"

def check_sql_execution(state: AgentState):
    error = state.get("error")
    if error:
        if "Security Violation" in error:
            return "analysis_node"
        if state.get("iteration_count", 0) < 3:
            return "sql_node"
        return "analysis_node"
    
    # If no error, check for visualization intent
    question = state.get("english_question", "").lower()
    
    # Expanded keywords for better intent detection
    visualization_keywords = [
        "graph", "chart", "plot", "visualize", 
        "categorize", "distribution", "breakdown", "split"
    ]
    
    if any(word in question for word in visualization_keywords):
        return "visualization_node"
        
    return "analysis_node"

workflow = StateGraph(AgentState)
workflow.add_node("guardrails_node", guardrails_node)
workflow.add_node("sql_node", sql_node)
workflow.add_node("execute_sql_node", execute_sql_node)
workflow.add_node("analysis_node", analysis_node)
workflow.add_node("visualization_node", visualization_node) # ADD THIS

workflow.set_entry_point("guardrails_node")
workflow.add_conditional_edges("guardrails_node", check_scope, {"sql_node": "sql_node", "analysis_node": "analysis_node", END: END})
workflow.add_edge("sql_node", "execute_sql_node")

# UPDATE THIS CONDITIONAL EDGE
workflow.add_conditional_edges(
    "execute_sql_node", 
    check_sql_execution, 
    {"sql_node": "sql_node", "analysis_node": "analysis_node", "visualization_node": "visualization_node"}
)

workflow.add_edge("analysis_node", END)
workflow.add_edge("visualization_node", END) # ADD THIS

secure_chatbot_app = workflow.compile()