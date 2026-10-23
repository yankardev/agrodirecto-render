package com.agrodirecto.pedido.service;

import com.agrodirecto.pedido.dto.Aviso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Avisos en tiempo real con Server-Sent Events (equivale a SignalR en la versión .NET). */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private final Map<Long, List<SseEmitter>> suscriptores = new ConcurrentHashMap<>();

    public SseEmitter suscribir(Long usuarioId) {
        SseEmitter emisor = new SseEmitter(3_600_000L);
        List<SseEmitter> lista = suscriptores.computeIfAbsent(usuarioId, id -> new CopyOnWriteArrayList<>());
        lista.add(emisor);
        Runnable quitar = () -> lista.remove(emisor);
        emisor.onCompletion(quitar);
        emisor.onTimeout(quitar);
        emisor.onError(e -> quitar.run());
        try {
            emisor.send(SseEmitter.event().name("conectado").data("ok"));
        } catch (IOException e) {
            quitar.run();
        }
        return emisor;
    }

    /** Envía un aviso a todas las pestañas abiertas del usuario. Un fallo aquí nunca debe romper la operación principal. */
    public void enviar(Long usuarioId, String evento, Aviso aviso) {
        List<SseEmitter> lista = suscriptores.get(usuarioId);
        if (lista == null) {
            return;
        }
        for (SseEmitter emisor : lista) {
            try {
                emisor.send(SseEmitter.event().name(evento).data(aviso));
            } catch (Exception e) {
                lista.remove(emisor);
                log.debug("Suscriptor desconectado (usuario {})", usuarioId);
            }
        }
    }
}
