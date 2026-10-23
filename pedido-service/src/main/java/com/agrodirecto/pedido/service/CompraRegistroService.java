package com.agrodirecto.pedido.service;

import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.dto.CheckoutRequest;
import com.agrodirecto.pedido.dto.CompraDto;
import com.agrodirecto.pedido.entity.*;
import com.agrodirecto.pedido.repository.CarritoItemRepository;
import com.agrodirecto.pedido.repository.CompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parte local y transaccional del checkout: crea la compra, un pedido por agricultor, el detalle con el
 * precio congelado y vacía el carrito. Todo o nada: si algo falla, no queda nada a medias.
 */
@Service
public class CompraRegistroService {

    private final CompraRepository compras;
    private final CarritoItemRepository carrito;

    public CompraRegistroService(CompraRepository compras, CarritoItemRepository carrito) {
        this.compras = compras;
        this.carrito = carrito;
    }

    @Transactional
    public CompraDto registrar(AuthUser cliente, CheckoutRequest datos, List<ItemReservado> reservados) {
        LocalDateTime ahora = LocalDateTime.now();
        Compra compra = new Compra();
        compra.setClienteId(cliente.id());
        compra.setClienteNombre(cliente.nombre());
        compra.setDireccionEntrega(datos.direccionEntrega().trim());
        compra.setDistrito(datos.distrito().trim());
        compra.setTelefono(datos.telefono().trim());
        compra.setFecha(ahora);

        Map<Long, Pedido> porAgricultor = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ItemReservado r : reservados) {
            Pedido pedido = porAgricultor.get(r.agricultorId());
            if (pedido == null) {
                pedido = new Pedido();
                pedido.setCompra(compra);
                pedido.setAgricultorId(r.agricultorId());
                pedido.setAgricultorNombre(r.agricultorNombre());
                pedido.setEstado(EstadoPedido.PENDIENTE);
                pedido.setFecha(ahora);
                porAgricultor.put(r.agricultorId(), pedido);
                compra.getPedidos().add(pedido);
            }
            BigDecimal subtotal = r.precio().multiply(BigDecimal.valueOf(r.cantidad()));
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProductoId(r.productoId());
            detalle.setProductoNombre(r.nombre());
            detalle.setPrecioUnitario(r.precio());
            detalle.setCantidad(r.cantidad());
            detalle.setSubtotal(subtotal);
            pedido.getDetalles().add(detalle);
            pedido.setSubtotal(pedido.getSubtotal().add(subtotal));
            total = total.add(subtotal);
        }
        compra.setTotal(total);
        Compra guardada = compras.save(compra);
        carrito.deleteByClienteId(cliente.id());
        return PedidoMapper.aDto(guardada);
    }
}
