package com.ecommerce.service;

import com.ecommerce.model.dto.AIPayloadDTO;
import com.ecommerce.model.dto.AIResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class AICommunicationService {

    @Value("${ai.service.url:http://localhost:8000/api/chat}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public AICommunicationService() {
        this.restTemplate = new RestTemplate();
    }

    public AIResponseDTO sendToAI(AIPayloadDTO payload) {
        try {
            ResponseEntity<AIResponseDTO> response = restTemplate.postForEntity(
                aiServiceUrl, 
                payload, 
                AIResponseDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            // Log securely on backend only
            System.err.println("[SECURITY] AI Service Communication Error: " + e.getMessage());
            throw new RuntimeException("AI_SERVICE_ERROR");
        }
    }
}
