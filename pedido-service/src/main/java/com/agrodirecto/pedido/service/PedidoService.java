package com.agrodirecto.pedido.service;

import com.agrodirecto.common.contrato.ItemStock;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.client.CatalogoClient;
import com.agrodirecto.pedido.dto.Aviso;
import com.agrodirecto.pedido.dto.CompraDto;
import com.agrodirecto.pedido.dto.PedidoDto;
import com.agrodirecto.pedido.entity.Compra;
import com.agrodirecto.pedido.entity.EstadoPedido;
import com.agrodirecto.pedido.entity.Pedido;
import com.agrodirecto.pedido.repository.CompraRepository;
import com.agrodirecto.pedido.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final CompraRepository compras;
    private final CatalogoClient catalogo;
    private final NotificacionService notificaciones;

    public PedidoService(PedidoRepository pedidos, CompraRepository compras, CatalogoClient catalogo,
                         NotificacionService notificaciones) {
        this.pedidos = pedidos;
        this.compras = compras;
        this.catalogo = catalogo;
        this.notificaciones = notificaciones;
    }

    @Transactional(readOnly = true)
    public List<PedidoDto> delAgricultor(Long agricultorId) {
        return pedidos.findByAgricultorIdOrderByFechaDesc(agricultorId).stream().map(PedidoMapper::aDto).toList();
    }

    /** Cambia el estado de un pedido propio. Las transiciones se validan aquí, no en la pantalla. */
    @Transactional
    public PedidoDto cambiarEstado(Long pedidoId, String nuevoEstado, AuthUser agricultor) {
        EstadoPedido destino;
        try {
            destino = EstadoPedido.valueOf(nuevoEstado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Estado no válido.");
        }
        Pedido pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));
        if (!pedido.getAgricultorId().equals(agricultor.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Ese pedido no te pertenece.");
        }
        if (!pedido.getEstado().puedePasarA(destino)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + pedido.getEstado() + " a " + destino + ".");
        }
        pedido.setEstado(destino);
        pedidos.save(pedido);

        if (destino == EstadoPedido.CANCELADO) {
            // Si el catálogo falla, la excepción revierte este cambio de estado: nunca se cancela sin devolver el stock.
            List<ItemStock> aDevolver = pedido.getDetalles().stream()
                    .map(d -> new ItemStock(d.getProductoId(), d.getCantidad()))
                    .toList();
            catalogo.liberar(aDevolver);
        }

        PedidoDto dto = PedidoMapper.aDto(pedido);
        notificaciones.enviar(pedido.getCompra().getClienteId(), "pedido-actualizado",
                new Aviso("Tu pedido #" + pedido.getId() + " con " + pedido.getAgricultorNombre()
                        + " ahora está " + destino.name().toLowerCase() + ".", pedido.getId()));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CompraDto> comprasDelCliente(Long clienteId) {
        return compras.findByClienteIdOrderByFechaDesc(clienteId).stream().map(PedidoMapper::aDto).toList();
    }

    @Transactional(readOnly = true)
    public CompraDto compra(Long id, AuthUser usuario) {
        Compra c = compras.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Compra no encontrada."));
        boolean esAdmin = "ADMINISTRADOR".equals(usuario.rol());
        if (!esAdmin && !c.getClienteId().equals(usuario.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Esa compra no te pertenece.");
        }
        return PedidoMapper.aDto(c);
    }
}
