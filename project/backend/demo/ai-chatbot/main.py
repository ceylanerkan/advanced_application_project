import os
from dotenv import load_dotenv

# Load variables from the .env file securely
load_dotenv()

def initialize_groq_environment():
    api_key = os.getenv("GROQ_API_KEY")
    
    if not api_key:
        raise ValueError("GROQ_API_KEY is missing. Check your .env file.")
    
    masked_key = f"{api_key[:6]}...{api_key[-4:]}" if len(api_key) > 10 else "Invalid Key Length"
    
    print("Environment successfully initialized for Groq AI!")
    print(f"Loaded Groq API Key: {masked_key}")
    print("LangGraph and python-dotenv are ready. Next step: Configure the Guardrails Agent.")

if __name__ == "__main__":
    initialize_groq_environment()