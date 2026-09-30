package com.example.queuewise.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.queuewise.model.Token;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QueueEventService {

    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        clients.add(emitter);

        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> {
            clients.remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> clients.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("QueueWise event stream connected"));
        } catch (IOException e) {
            clients.remove(emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }
    public void publish(Token token) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event()
                        .name("token-updated")
                        .data(token));
            } catch (Exception e) {
                clients.remove(emitter);
                emitter.complete();
            }
        }
    }
}