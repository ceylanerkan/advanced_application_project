package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.model.dto.AIPayloadDTO;
import com.ecommerce.model.dto.AIRequestDTO;
import com.ecommerce.model.dto.AIResponseDTO;
import com.ecommerce.service.AICommunicationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "Endpoints for the Multi-Agent Text2SQL AI Chatbot")
public class AIChatController {

    private final AICommunicationService aiCommunicationService;

    @Operation(summary = "Ask the AI a question", description = "Processes a natural language query through the LangGraph AI agents and returns insights or visualizations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful AI response"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token"),
            @ApiResponse(responseCode = "500", description = "AI service error")
    })
    @PostMapping("/ask")
    public ResponseEntity<AIResponseDTO> askAI(@RequestBody AIRequestDTO requestDTO) {
        try {
            // ENFORCE BACKEND SECURITY CONTEXT
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AIResponseDTO(null, null, "error", "Unauthorized access")
                );
            }

            User currentUser = (User) authentication.getPrincipal();

            // BUILD SECURE AI PAYLOAD
            AIPayloadDTO payload = new AIPayloadDTO();
            payload.setQuestion(requestDTO.getQuestion());
            payload.setSessionId(requestDTO.getSessionId());

            // INJECT VERIFIED SECURITY DATA
            payload.setCurrentUserId(currentUser.getId());
            payload.setCurrentUserRole(currentUser.getRoleType());

            // Dispatch to AI Service
            AIResponseDTO response = aiCommunicationService.sendToAI(payload);
            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            // GRACEFUL ERROR HANDLING
            if ("AI_SERVICE_ERROR".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AIResponseDTO(null, null, "error", "The AI service is currently unavailable."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AIResponseDTO(null, null, "error", "An internal system error occurred."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AIResponseDTO(null, null, "error", "An unexpected error occurred."));
        }
    }
}