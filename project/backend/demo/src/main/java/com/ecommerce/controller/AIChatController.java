package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.model.dto.AIPayloadDTO;
import com.ecommerce.model.dto.AIRequestDTO;
import com.ecommerce.model.dto.AIResponseDTO;
import com.ecommerce.service.AICommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class AIChatController {

    @Autowired
    private AICommunicationService aiCommunicationService;

    @PostMapping("/ask")
    public ResponseEntity<AIResponseDTO> askAI(@RequestBody AIRequestDTO requestDTO) {
        try {
            // STEP 3: ENFORCE BACKEND SECURITY CONTEXT
            // We ignore any ID passed by the client. We ONLY extract the ID and Role from 
            // the verified Spring Security Context (JWT validated).
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AIResponseDTO(null, null, "error", "Unauthorized access")
                );
            }

            User currentUser = (User) authentication.getPrincipal();

            // STEP 4: BUILD AI COMMUNICATION SERVICE
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
            // STEP 6: IMPLEMENT GRACEFUL ERROR HANDLING
            if ("AI_SERVICE_ERROR".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AIResponseDTO(null, null, "error", "The AI service is currently unavailable."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AIResponseDTO(null, null, "error", "An internal system error occurred."));
        } catch (Exception ex) {
            // No stack traces are ever leaked back to the client
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AIResponseDTO(null, null, "error", "An unexpected error occurred."));
        }
    }
}
