package com.ecommerce.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AIResponseDTO {

// Forces Java to output "finalAnswer" to Angular, 
    // but accepts BOTH "final_answer" and "finalAnswer" from Python
    @JsonProperty("finalAnswer")
    @JsonAlias({"final_answer", "finalAnswer"})
    private String finalAnswer;

    @JsonProperty("visualizationCode")
    @JsonAlias({"visualization_code", "visualizationCode"})
    private String visualizationCode;

    private String status;
    private String error;

    public AIResponseDTO() {}

    public AIResponseDTO(String finalAnswer, String visualizationCode, String status, String error) {
        this.finalAnswer = finalAnswer;
        this.visualizationCode = visualizationCode;
        this.status = status;
        this.error = error;
    }

    public String getFinalAnswer() { return finalAnswer; }
    public void setFinalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; }
    public String getVisualizationCode() { return visualizationCode; }
    public void setVisualizationCode(String visualizationCode) { this.visualizationCode = visualizationCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
