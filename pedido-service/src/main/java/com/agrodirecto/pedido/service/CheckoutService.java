package com.agrodirecto.pedido.service;

import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.contrato.ItemStock;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.client.CatalogoClient;
import com.agrodirecto.pedido.dto.Aviso;
import com.agrodirecto.pedido.dto.CheckoutRequest;
import com.agrodirecto.pedido.dto.CompraDto;
import com.agrodirecto.pedido.entity.CarritoItem;
import com.agrodirecto.pedido.repository.CarritoItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Checkout distribuido (patrón saga con compensación):
 * 1) el catálogo reserva el stock en una transacción de todo o nada;
 * 2) aquí se registra la compra en otra transacción;
 * 3) si el paso 2 falla, se libera el stock reservado para no dejar inventario bloqueado.
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CarritoItemRepository carrito;
    private final CatalogoClient catalogo;
    private final CompraRegistroService registro;
    private final NotificacionService notificaciones;

    public CheckoutService(CarritoItemRepository carrito, CatalogoClient catalogo,
                           CompraRegistroService registro, NotificacionService notificaciones) {
        this.carrito = carrito;
        this.catalogo = catalogo;
        this.registro = registro;
        this.notificaciones = notificaciones;
    }

    public CompraDto finalizar(AuthUser cliente, CheckoutRequest datos) {
        List<CarritoItem> items = carrito.findByClienteIdOrderByIdAsc(cliente.id());
        if (items.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tu carrito está vacío.");
        }
        List<ItemStock> pedidoStock = items.stream()
                .map(i -> new ItemStock(i.getProductoId(), i.getCantidad()))
                .toList();

        List<ItemReservado> reservados = catalogo.reservar(pedidoStock); // 409 si falta stock

        CompraDto compra;
        try {
            compra = registro.registrar(cliente, datos, reservados);
        } catch (RuntimeException e) {
            compensar(pedidoStock);
            throw e;
        }

        compra.pedidos().forEach(p -> notificaciones.enviar(p.agricultorId(), "pedido-nuevo",
                new Aviso("Nuevo pedido #" + p.id() + " de " + cliente.nombre(), p.id())));
        return compra;
    }

    private void compensar(List<ItemStock> items) {
        try {
            catalogo.liberar(items);
        } catch (RuntimeException ex) {
            log.error("No se pudo liberar el stock reservado; requiere revisión manual: {}", items, ex);
        }
    }
}
