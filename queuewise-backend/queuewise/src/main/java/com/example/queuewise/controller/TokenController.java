package com.example.queuewise.controller;

import com.example.queuewise.service.TokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.example.queuewise.model.Token;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.queuewise.service.QueueEventService;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

@RestController
public class TokenController {

    private final TokenService tokenService;
    private final QueueEventService queueEventService;

    public TokenController(
            TokenService tokenService,
            QueueEventService queueEventService) {

        this.tokenService = tokenService;
        this.queueEventService = queueEventService;
    }

    @PostMapping("/api/tokens")
    public ResponseEntity<?> createToken(
            @RequestBody Map<String, String> request) {

        String serviceType = request.get("serviceType");

        if (serviceType == null || serviceType.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "serviceType is required"));
        }

        return ResponseEntity.ok(tokenService.createToken(serviceType));
    }

    @GetMapping("/api/tokens/waiting")
    public List<Token> getWaitingTokens() {
        return tokenService.getWaitingTokens();
    }

    @PostMapping("/api/tokens/call-next")
    public Map<String, String> callNextToken() {
        return tokenService.callNextToken();
    }
    @GetMapping("/api/tokens/{tokenNumber}")
    public ResponseEntity<?> getToken(@PathVariable String tokenNumber) {
        Token token = tokenService.getToken(tokenNumber);

        if (token == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Token not found"));
        }

        return ResponseEntity.ok(token);
    }
    @PostMapping("/api/tokens/{tokenNumber}/complete")
    public ResponseEntity<?> completeToken(@PathVariable String tokenNumber) {
        try {
            Token token = tokenService.completeToken(tokenNumber);

            if (token == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Token not found"));
            }

            return ResponseEntity.ok(token);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/api/tokens/{tokenNumber}/skip")
    public ResponseEntity<?> skipToken(@PathVariable String tokenNumber) {
        try {
            Token token = tokenService.skipToken(tokenNumber);

            if (token == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Token not found"));
            }

            return ResponseEntity.ok(token);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping(
            path = "/api/tokens/events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribeToQueueEvents() {
        return queueEventService.subscribe();
    }
}