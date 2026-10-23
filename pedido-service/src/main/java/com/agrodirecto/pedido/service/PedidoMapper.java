package com.agrodirecto.pedido.service;

import com.agrodirecto.pedido.dto.CompraDto;
import com.agrodirecto.pedido.dto.DetalleDto;
import com.agrodirecto.pedido.dto.PedidoDto;
import com.agrodirecto.pedido.entity.Compra;
import com.agrodirecto.pedido.entity.DetallePedido;
import com.agrodirecto.pedido.entity.EstadoPedido;
import com.agrodirecto.pedido.entity.Pedido;

import java.util.Comparator;

/** Convierte entidades en DTOs. Debe llamarse dentro de una transacción (hay colecciones LAZY). */
public final class PedidoMapper {

    private PedidoMapper() {
    }

    public static DetalleDto aDto(DetallePedido d) {
        return new DetalleDto(d.getProductoId(), d.getProductoNombre(), d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal());
    }

    public static PedidoDto aDto(Pedido p) {
        Compra c = p.getCompra();
        return new PedidoDto(p.getId(), c.getId(), p.getAgricultorId(), p.getAgricultorNombre(), c.getClienteNombre(),
                p.getEstado().name(), p.getSubtotal(), p.getFecha(), c.getDireccionEntrega(), c.getDistrito(), c.getTelefono(),
                p.getDetalles().stream().map(PedidoMapper::aDto).toList());
    }

    public static CompraDto aDto(Compra c) {
        return new CompraDto(c.getId(), c.getFecha(), c.getTotal(), estadoGeneral(c), c.getDireccionEntrega(),
                c.getDistrito(), c.getTelefono(), c.getMetodoPago(),
                c.getPedidos().stream().map(PedidoMapper::aDto).toList());
    }

    /**
     * El estado de la compra es el del pedido menos avanzado, sin contar los cancelados.
     * Si todos están cancelados, la compra está cancelada.
     */
    public static String estadoGeneral(Compra c) {
        return c.getPedidos().stream()
                .map(Pedido::getEstado)
                .filter(e -> e != EstadoPedido.CANCELADO)
                .min(Comparator.naturalOrder())
                .orElse(EstadoPedido.CANCELADO)
                .name();
    }
}
