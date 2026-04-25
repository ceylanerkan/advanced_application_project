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
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Endpoints for the Multi-Agent Text2SQL AI Chatbot (legacy path)")
public class ChatController {

    private final AICommunicationService aiCommunicationService;

    @Operation(summary = "Ask the AI a question", description = "Processes a natural language query through the LangGraph AI agents. Delegates to the same AI service as /api/ai-chat.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful AI response"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "AI service error")
    })
    @PostMapping("/ask")
    public ResponseEntity<AIResponseDTO> askQuestion(@RequestBody AIRequestDTO requestDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AIResponseDTO(null, null, "error", "Unauthorized access")
                );
            }

            User currentUser = (User) authentication.getPrincipal();

            AIPayloadDTO payload = new AIPayloadDTO();
            payload.setQuestion(requestDTO.getQuestion());
            payload.setSessionId(requestDTO.getSessionId());
            payload.setCurrentUserId(currentUser.getId());
            payload.setCurrentUserRole(currentUser.getRoleType());

            AIResponseDTO response = aiCommunicationService.sendToAI(payload);
            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
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