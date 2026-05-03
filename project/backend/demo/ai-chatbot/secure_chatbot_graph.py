import json
from typing import TypedDict, Optional, Union, Dict, Any
from langgraph.graph import StateGraph, END
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError

# Import the 5 hardened agents we defined earlier
from agents import (
    guardrails_agent,
    sql_agent,
    analysis_agent,
    visualization_agent,
    error_agent
)

# ==========================================
# 0. Database Configuration
# ==========================================
# ⚠️ REPLACE THIS STRING with your actual database credentials!
# Format: dialect+driver://username:password@host:port/database
# MySQL Example: "mysql+pymysql://root:password123@localhost:3306/ecommerce_db"
# PostgreSQL Example: "postgresql://postgres:password123@localhost:5432/ecommerce_db"
DB_URI = "mysql+pymysql://root:admin@localhost:3306/ecommerce_db" 
engine = create_engine(DB_URI)

# ==========================================
# 1. State Management
# ==========================================
class AgentState(TypedDict):
    question: str
    sql_query: Optional[str]
    query_result: Optional[Union[str, Dict[str, Any]]]
    error: Optional[str]
    final_answer: Optional[str]
    visualization_code: Optional[str]
    is_in_scope: bool
    iteration_count: int
    current_user_id: int
    current_user_role: str

# ==========================================
# 2. Node Functions
# ==========================================

def guardrails_node(state: AgentState):
    """Checks if the question is in scope and secure."""
    
    # Using strict delimiters to prevent False Positives (AV-01 protection)
    prompt_input = f"""
    [TRUSTED SYSTEM METADATA]
    Authenticated User ID: {state['current_user_id']}
    Authenticated Role: {state['current_user_role']}

    [UNTRUSTED USER QUESTION]
    Question: {state['question']}
    """
    response = guardrails_agent.invoke({"input": prompt_input})
    
    # We use a simple heuristic to determine rejection. 
    response_text = response.content.lower()
    is_rejected = "reject" in response_text or "out-of-scope" in response_text or "out of scope" in response_text or "violation" in response_text
    
    if is_rejected:
        return {"is_in_scope": False, "final_answer": response.content}
    else:
        return {"is_in_scope": True}

def sql_node(state: AgentState):
    """Generates or fixes SQL based on the error state."""
    prompt_input = f"User ID: {state['current_user_id']}, Role: {state['current_user_role']}\nQuestion: {state['question']}"
    
    iteration_count = state.get("iteration_count", 0)
    
    # If there is an error, route to the Error Recovery Agent
    if state.get("error"):
        prompt_input += f"\nPrevious Error: {state['error']}\nPrevious Query: {state['sql_query']}\nPlease fix this query."
        response = error_agent.invoke({"input": prompt_input})
    else:
        # Otherwise use the standard SQL Agent
        response = sql_agent.invoke({"input": prompt_input})
    
    # Clean up any potential markdown formatting from the LLM output
    sql_query = response.content.replace('```sql', '').replace('```', '').strip()
    return {"sql_query": sql_query, "iteration_count": iteration_count + 1}

def execute_sql_node(state: AgentState):
    """Executes the generated SQL query against the real database."""
    sql = state.get("sql_query", "")
    
    # 1. Enforce strict Read-Only constraints (Crucial for Security!)
    upper_sql = sql.upper()
    if any(forbidden in upper_sql for forbidden in ["DROP ", "UPDATE ", "DELETE ", "INSERT ", "ALTER ", "TRUNCATE "]):
        return {"error": "Unauthorized SQL command detected. Only SELECT is allowed.", "query_result": None}
    
    # 2. Enforce SELECT * ban (AV-12 requirement)
    if "SELECT *" in upper_sql:
        return {"error": "SELECT * is forbidden. Explicitly name columns.", "query_result": None}
    
    # 3. Execute the query against the real database
    try:
        with engine.connect() as connection:
            result = connection.execute(text(sql))
            # Fetch all rows and convert them to a list of dictionaries
            rows = [dict(row._mapping) for row in result]
            
            # Helper to convert dates/decimals to strings for JSON serialization
            def custom_serializer(obj):
                if hasattr(obj, 'isoformat'):
                    return obj.isoformat()
                return str(obj)

            json_result = json.dumps(rows, default=custom_serializer)
            return {"query_result": json_result, "error": None}
            
    except SQLAlchemyError as e:
        # Capture the database error so the Error Agent can fix it!
        error_msg = str(e.__dict__.get('orig', e))
        return {"error": error_msg, "query_result": None}
    except Exception as e:
        return {"error": str(e), "query_result": None}

def analysis_node(state: AgentState):
    """Explains the query results in natural language."""
    # If we failed 3 times and still have an error, the Analysis Node can explain the failure
    if state.get("error"):
        prompt_input = f"Question: {state['question']}\nWe failed to execute the SQL query successfully after 3 attempts. Error: {state['error']}\nExplain this failure."
    else:
        prompt_input = f"Question: {state['question']}\nQuery Result: {state['query_result']}"
        
    response = analysis_agent.invoke({"input": prompt_input})
    return {"final_answer": response.content}

def visualization_node(state: AgentState):
    """Generates JSON configuration for Plotly if a chart is requested."""
    prompt_input = f"Question: {state['question']}\nQuery Result: {state['query_result']}"
    response = visualization_agent.invoke({"input": prompt_input})
    
    # Clean markdown to extract just the JSON (Fixed string literal)
    viz_code = response.content.replace('```json', '').replace('```', '').strip()
    
    return {"visualization_code": viz_code}

# ==========================================
# 3. Graph Execution Flow (Routing Edges)
# ==========================================

def check_scope(state: AgentState):
    """Determine whether to proceed to SQL generation or end the graph."""
    if state.get("is_in_scope"):
        return "sql_node"
    return END

def check_sql_execution(state: AgentState):
    """Determine whether to retry SQL generation or proceed to analysis."""
    if state.get("error"):
        # Max 3 retries
        if state.get("iteration_count", 0) >= 3:
            return "analysis_node"
        return "sql_node"
    return "analysis_node"

def check_visualization_needed(state: AgentState):
    """Determine if a graph is needed based on the user's question."""
    question = state.get("question", "").lower()
    
    # Simple heuristic to determine if user asked for a chart
    if any(keyword in question for keyword in ["chart", "graph", "plot", "visualize"]):
        return "visualization_node"
    return END

# ==========================================
# 4. Build and Compile the LangGraph
# ==========================================
workflow = StateGraph(AgentState)

# Add all nodes
workflow.add_node("guardrails_node", guardrails_node)
workflow.add_node("sql_node", sql_node)
workflow.add_node("execute_sql_node", execute_sql_node)
workflow.add_node("analysis_node", analysis_node)
workflow.add_node("visualization_node", visualization_node)

# Set the entry point
workflow.set_entry_point("guardrails_node")

# Edge: Guardrails -> (SQL Node OR End)
workflow.add_conditional_edges(
    "guardrails_node",
    check_scope,
    {
        "sql_node": "sql_node",
        END: END
    }
)

# Edge: SQL Node -> Execute SQL Node
workflow.add_edge("sql_node", "execute_sql_node")

# Edge: Execute SQL Node -> (Retry SQL Node OR Analysis Node)
workflow.add_conditional_edges(
    "execute_sql_node",
    check_sql_execution,
    {
        "sql_node": "sql_node",             # Goes back to sql_node which triggers Error Agent if error state exists
        "analysis_node": "analysis_node"    # Proceeds to analysis if successful or max retries hit
    }
)

# Edge: Analysis Node -> (Visualization Node OR End)
workflow.add_conditional_edges(
    "analysis_node",
    check_visualization_needed,
    {
        "visualization_node": "visualization_node",
        END: END
    }
)

# Edge: Visualization Node -> End
workflow.add_edge("visualization_node", END)

# Compile the secure chatbot graph
secure_chatbot_app = workflow.compile()
