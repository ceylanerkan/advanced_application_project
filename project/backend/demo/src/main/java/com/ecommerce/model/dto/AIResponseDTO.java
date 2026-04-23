package com.ecommerce.model.dto;

public class AIResponseDTO {
    private String finalAnswer;
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
