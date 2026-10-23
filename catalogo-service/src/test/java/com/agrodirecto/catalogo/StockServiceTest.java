package com.agrodirecto.catalogo;

import com.agrodirecto.catalogo.entity.Categoria;
import com.agrodirecto.catalogo.entity.Producto;
import com.agrodirecto.catalogo.entity.UnidadMedida;
import com.agrodirecto.catalogo.repository.ProductoRepository;
import com.agrodirecto.catalogo.service.StockService;
import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.contrato.ItemStock;
import com.agrodirecto.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    ProductoRepository repo;

    StockService service;

    @BeforeEach
    void preparar() {
        service = new StockService(repo);
    }

    private Producto producto(long id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Papa amarilla");
        p.setPrecio(new BigDecimal("3.80"));
        p.setStock(stock);
        p.setActivo(true);
        p.setHabilitado(true);
        p.setAgricultorId(2L);
        p.setAgricultorNombre("Juan Quispe");
        p.setCategoria(new Categoria("Tubérculos", ""));
        p.setUnidad(new UnidadMedida("Kilogramo", "kg"));
        return p;
    }

    @Test
    void reservarDescuentaElStockYDevuelveLosDatosCongelados() {
        Producto p = producto(1, 10);
        when(repo.buscarConBloqueo(1L)).thenReturn(Optional.of(p));

        List<ItemReservado> r = service.reservar(List.of(new ItemStock(1L, 3)));

        assertEquals(7, p.getStock());
        assertEquals(1, r.size());
        assertEquals(new BigDecimal("3.80"), r.get(0).precio());
        assertEquals(3, r.get(0).cantidad());
        assertEquals("Juan Quispe", r.get(0).agricultorNombre());
    }

    @Test
    void sinStockSuficienteLanzaConflictoYNoDescuentaNada() {
        Producto p = producto(1, 2);
        when(repo.buscarConBloqueo(1L)).thenReturn(Optional.of(p));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.reservar(List.of(new ItemStock(1L, 5))));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals(2, p.getStock());
        verify(repo, never()).save(any());
    }

    @Test
    void unificaLosItemsRepetidosDelMismoProducto() {
        Producto p = producto(1, 10);
        when(repo.buscarConBloqueo(1L)).thenReturn(Optional.of(p));

        service.reservar(List.of(new ItemStock(1L, 2), new ItemStock(1L, 3)));

        assertEquals(5, p.getStock());
    }

    @Test
    void unProductoOcultoNoSePuedeReservar() {
        Producto p = producto(1, 10);
        p.setHabilitado(false);
        when(repo.buscarConBloqueo(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessException.class, () -> service.reservar(List.of(new ItemStock(1L, 1))));
        assertEquals(10, p.getStock());
    }

    @Test
    void liberarDevuelveElStock() {
        Producto p = producto(1, 7);
        when(repo.buscarConBloqueo(1L)).thenReturn(Optional.of(p));

        service.liberar(List.of(new ItemStock(1L, 3)));

        assertEquals(10, p.getStock());
    }
}
