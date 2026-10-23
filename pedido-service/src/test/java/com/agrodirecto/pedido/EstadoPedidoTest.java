package com.agrodirecto.pedido;

import com.agrodirecto.pedido.entity.EstadoPedido;
import org.junit.jupiter.api.Test;

import static com.agrodirecto.pedido.entity.EstadoPedido.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoPedidoTest {

    @Test
    void flujoNormalPendienteConfirmadoEntregado() {
        assertTrue(PENDIENTE.puedePasarA(CONFIRMADO));
        assertTrue(CONFIRMADO.puedePasarA(ENTREGADO));
    }

    @Test
    void sePuedeCancelarAntesDeEntregar() {
        assertTrue(PENDIENTE.puedePasarA(CANCELADO));
        assertTrue(CONFIRMADO.puedePasarA(CANCELADO));
    }

    @Test
    void noSePuedeSaltarPasos() {
        assertFalse(PENDIENTE.puedePasarA(ENTREGADO));
    }

    @Test
    void losEstadosFinalesNoCambian() {
        for (EstadoPedido destino : EstadoPedido.values()) {
            assertFalse(ENTREGADO.puedePasarA(destino));
            assertFalse(CANCELADO.puedePasarA(destino));
        }
    }

    @Test
    void noSePuedeRetroceder() {
        assertFalse(CONFIRMADO.puedePasarA(PENDIENTE));
    }
}
