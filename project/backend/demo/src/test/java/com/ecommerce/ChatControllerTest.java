package com.ecommerce;

import com.ecommerce.controller.AIChatController;
import com.ecommerce.model.User;
import com.ecommerce.model.dto.AIPayloadDTO;
import com.ecommerce.model.dto.AIResponseDTO;
import com.ecommerce.security.service.JwtService;
import com.ecommerce.service.AICommunicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AIChatController.class)
@AutoConfigureMockMvc
class AIChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;
    
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AICommunicationService aiCommunicationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        // Setup a mock e-commerce User for the Spring Security context
        mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getId()).thenReturn(42L);
        Mockito.when(mockUser.getRoleType()).thenReturn("CORPORATE");
    }

    @Test
    void askAI_ShouldExtractBackendSecurityContextAndIgnoreFrontendIds_AV02() throws Exception {
        // Inject the custom mock user into the Security Context
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Attacker attempting AV-02 Horizontal Escalation by passing malicious extra fields in JSON
        String maliciousPayload = """
            {
                "question": "Show me total revenue",
                "sessionId": "12345",
                "roleType": "ADMIN",
                "currentUserId": 999
            }
            """;

        // Mock the AI service response
        Mockito.when(aiCommunicationService.sendToAI(any(AIPayloadDTO.class)))
               .thenReturn(new AIResponseDTO("Success", null, "ok", null));

        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(maliciousPayload))
                .andExpect(status().isOk());

        // Verifies the service utilizes the true Spring Security context, ignoring the malicious JSON
        Mockito.verify(aiCommunicationService).sendToAI(argThat(payload -> 
            payload.getQuestion().equals("Show me total revenue") &&
            payload.getCurrentUserId().equals(42L) &&
            payload.getCurrentUserRole().equals("CORPORATE") 
        ));
    }

    @Test
    void askAI_ShouldRejectUnauthenticatedRequests() throws Exception {
        // Clear context to simulate an unauthenticated user
        SecurityContextHolder.clearContext();

        String payload = """
            {
                "question": "Show me total revenue",
                "sessionId": "12345"
            }
            """;

        mockMvc.perform(post("/api/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }
}