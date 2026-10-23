package com.agrodirecto.pedido.dto;

/** Mensaje enviado en tiempo real (SSE) al navegador. */
public record Aviso(String mensaje, Long id) {
}
