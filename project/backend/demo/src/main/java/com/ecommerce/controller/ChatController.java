package com.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "AI Chatbot", description = "Endpoints for the Multi-Agent Text2SQL AI Chatbot")
public class ChatController {

    @Operation(summary = "Ask the AI a question", description = "Processes a natural language query through the LangGraph AI agents and returns insights or visualizations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful AI response"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests - Rate limit exceeded (Prevents AV-09)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Object chatRequest) {
        // Logic to pass the request to your Python LangGraph service
        return ResponseEntity.ok().build();
    }
}