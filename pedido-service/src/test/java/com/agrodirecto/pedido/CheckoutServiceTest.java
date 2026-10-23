package com.agrodirecto.pedido;

import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.contrato.ItemStock;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.client.CatalogoClient;
import com.agrodirecto.pedido.dto.CheckoutRequest;
import com.agrodirecto.pedido.dto.CompraDto;
import com.agrodirecto.pedido.dto.PedidoDto;
import com.agrodirecto.pedido.entity.CarritoItem;
import com.agrodirecto.pedido.repository.CarritoItemRepository;
import com.agrodirecto.pedido.service.CheckoutService;
import com.agrodirecto.pedido.service.CompraRegistroService;
import com.agrodirecto.pedido.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Prueba de la saga del checkout: reservar stock -> registrar compra -> compensar si falla. */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock CarritoItemRepository carrito;
    @Mock CatalogoClient catalogo;
    @Mock CompraRegistroService registro;
    @Mock NotificacionService notificaciones;

    CheckoutService service;
    final AuthUser cliente = new AuthUser(5L, "lucia@correo.com", "Lucía Torres", "CLIENTE");
    final CheckoutRequest datos = new CheckoutRequest("Calle Los Pinos 245", "Miraflores", "955123456");

    @BeforeEach
    void preparar() {
        service = new CheckoutService(carrito, catalogo, registro, notificaciones);
    }

    private CarritoItem item(long productoId, int cantidad) {
        CarritoItem i = new CarritoItem();
        i.setClienteId(5L);
        i.setProductoId(productoId);
        i.setCantidad(cantidad);
        return i;
    }

    private ItemReservado reservado(long productoId, int cantidad) {
        return new ItemReservado(productoId, "Papa", new BigDecimal("3.80"), cantidad, 2L, "Juan Quispe", "kg");
    }

    private CompraDto compraConPedido() {
        PedidoDto pedido = new PedidoDto(10L, 1L, 2L, "Juan Quispe", "Lucía Torres", "PENDIENTE",
                new BigDecimal("7.60"), LocalDateTime.now(), "Calle Los Pinos 245", "Miraflores", "955123456", List.of());
        return new CompraDto(1L, LocalDateTime.now(), new BigDecimal("7.60"), "PENDIENTE", "Calle Los Pinos 245",
                "Miraflores", "955123456", "Contra entrega", List.of(pedido));
    }

    @Test
    void checkoutExitosoReservaRegistraYAvisaAlAgricultor() {
        when(carrito.findByClienteIdOrderByIdAsc(5L)).thenReturn(List.of(item(1L, 2)));
        when(catalogo.reservar(any())).thenReturn(List.of(reservado(1L, 2)));
        when(registro.registrar(eq(cliente), eq(datos), any())).thenReturn(compraConPedido());

        CompraDto compra = service.finalizar(cliente, datos);

        assertEquals(1L, compra.id());
        verify(catalogo, never()).liberar(any());
        verify(notificaciones).enviar(eq(2L), eq("pedido-nuevo"), any());
    }

    @Test
    void siFallaElRegistroSeLiberaElStockReservado() {
        List<CarritoItem> items = List.of(item(1L, 2));
        when(carrito.findByClienteIdOrderByIdAsc(5L)).thenReturn(items);
        when(catalogo.reservar(any())).thenReturn(List.of(reservado(1L, 2)));
        when(registro.registrar(any(), any(), any())).thenThrow(new IllegalStateException("falla de base de datos"));

        assertThrows(IllegalStateException.class, () -> service.finalizar(cliente, datos));

        verify(catalogo).liberar(List.of(new ItemStock(1L, 2)));
        verify(notificaciones, never()).enviar(any(), any(), any());
    }

    @Test
    void siNoHayStockNoSeRegistraNiSeLiberaNada() {
        when(carrito.findByClienteIdOrderByIdAsc(5L)).thenReturn(List.of(item(1L, 99)));
        when(catalogo.reservar(any())).thenThrow(new BusinessException(HttpStatus.CONFLICT, "Stock insuficiente"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.finalizar(cliente, datos));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verifyNoInteractions(registro);
        verify(catalogo, never()).liberar(any());
    }

    @Test
    void conElCarritoVacioNoSeLlamaAlCatalogo() {
        when(carrito.findByClienteIdOrderByIdAsc(5L)).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.finalizar(cliente, datos));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verifyNoInteractions(catalogo, registro);
    }
}
