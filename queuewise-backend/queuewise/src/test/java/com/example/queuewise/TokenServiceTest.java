package com.example.queuewise;

import com.example.queuewise.model.Token;
import com.example.queuewise.repository.TokenRepository;
import com.example.queuewise.service.QueueEventService;
import com.example.queuewise.service.TokenService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import com.example.queuewise.model.TokenStatus;
import static org.mockito.Mockito.times;

class TokenServiceTest {

    @Test
    void cannotCompleteWaitingToken() {
        TokenRepository repository = mock(TokenRepository.class);
        QueueEventService events = mock(QueueEventService.class);
        TokenService service = new TokenService(repository, events);

        Token token = new Token("A001", "GENERAL");
        when(repository.findByTokenNumber("A001"))
                .thenReturn(Optional.of(token));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> service.completeToken("A001")
        );

        assertEquals("Only a CALLED token can be completed",
                error.getMessage());
        verify(repository).findByTokenNumber("A001");
        verify(repository, never()).save(token);
    }
    @Test
    void completesCalledToken() {
        TokenRepository repository = mock(TokenRepository.class);
        QueueEventService events = mock(QueueEventService.class);
        TokenService service = new TokenService(repository, events);

        Token token = new Token("A002", "GENERAL");
        token.setStatus(TokenStatus.CALLED);

        when(repository.findByTokenNumber("A002"))
                .thenReturn(Optional.of(token));
        when(repository.save(token)).thenReturn(token);

        Token result = service.completeToken("A002");

        assertEquals(TokenStatus.COMPLETED, result.getStatus());
        verify(repository, times(1)).save(token);
    }
    @Test
    void skipsCalledToken() {
        TokenRepository repository = mock(TokenRepository.class);
        QueueEventService events = mock(QueueEventService.class);
        TokenService service = new TokenService(repository, events);

        Token token = new Token("A003", "GENERAL");
        token.setStatus(TokenStatus.CALLED);

        when(repository.findByTokenNumber("A003"))
                .thenReturn(Optional.of(token));
        when(repository.save(token)).thenReturn(token);

        Token result = service.skipToken("A003");

        assertEquals(TokenStatus.SKIPPED, result.getStatus());
        verify(repository, times(1)).save(token);
    }
}