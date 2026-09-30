package com.example.queuewise.service;

import com.example.queuewise.model.Token;
import com.example.queuewise.model.TokenStatus;
import org.springframework.stereotype.Service;
import com.example.queuewise.repository.TokenRepository;


import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    public synchronized Token createToken(String serviceType) {
        int nextNumber = tokenRepository.findFirstByOrderByIdDesc()
                .map(lastToken -> Integer.parseInt(
                        lastToken.getTokenNumber().substring(1)) + 1)
                .orElse(1);

        Token token = new Token("A%03d".formatted(nextNumber), serviceType);
        Token savedToken = tokenRepository.save(token);
        queueEventService.publish(savedToken);
        return savedToken;
    }

    public synchronized List<Token> getWaitingTokens() {
        return tokenRepository.findByStatusOrderByIdAsc(TokenStatus.WAITING);
    }
    public synchronized Token getToken(String tokenNumber) {
        return tokenRepository.findByTokenNumber(tokenNumber).orElse(null);
    }

    public synchronized Map<String, String> callNextToken() {
        List<Token> waitingTokens =
                tokenRepository.findByStatusOrderByIdAsc(TokenStatus.WAITING);

        if (waitingTokens.isEmpty()) {
            return Map.of("message", "No tokens waiting");
        }

        Token token = waitingTokens.get(0);
        token.setStatus(TokenStatus.CALLED);
        Token savedToken = tokenRepository.save(token);
        queueEventService.publish(savedToken);

        return Map.of(
                "tokenNumber", savedToken.getTokenNumber(),
                "serviceType", savedToken.getServiceType(),
                "status", savedToken.getStatus().name()
        );
    }
    public synchronized Token completeToken(String tokenNumber) {
        Token token = getToken(tokenNumber);

        if (token == null) {
            return null;
        }

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new IllegalStateException("Only a CALLED token can be completed");
        }

        token.setStatus(TokenStatus.COMPLETED);
        Token savedToken = tokenRepository.save(token);
        queueEventService.publish(savedToken);
        return savedToken;
    }

    public synchronized Token skipToken(String tokenNumber) {
        Token token = getToken(tokenNumber);

        if (token == null) {
            return null;
        }

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new IllegalStateException("Only a CALLED token can be skipped");
        }

        token.setStatus(TokenStatus.SKIPPED);
        Token savedToken = tokenRepository.save(token);
        queueEventService.publish(savedToken);
        return savedToken;

    }
    private final TokenRepository tokenRepository;
    private final QueueEventService queueEventService;
    public TokenService(
            TokenRepository tokenRepository,
            QueueEventService queueEventService) {
        this.tokenRepository = tokenRepository;
        this.queueEventService = queueEventService;
    }
}