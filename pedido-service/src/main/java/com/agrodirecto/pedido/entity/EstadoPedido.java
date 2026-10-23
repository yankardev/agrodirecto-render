package com.agrodirecto.pedido.entity;

/** Ciclo de vida de un pedido: PENDIENTE -> CONFIRMADO -> ENTREGADO. Se puede CANCELAR antes de entregar. */
public enum EstadoPedido {
    PENDIENTE, CONFIRMADO, ENTREGADO, CANCELADO;

    public boolean puedePasarA(EstadoPedido destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADO || destino == CANCELADO;
            case CONFIRMADO -> destino == ENTREGADO || destino == CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
