package com.agrodirecto.catalogo.service;

import com.agrodirecto.catalogo.entity.Producto;
import com.agrodirecto.catalogo.repository.ProductoRepository;
import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.contrato.ItemStock;
import com.agrodirecto.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reserva y libera stock. La reserva es "todo o nada": si un solo producto no tiene stock suficiente,
 * la transacción se revierte y ningún producto se descuenta.
 */
@Service
public class StockService {

    private final ProductoRepository productos;

    public StockService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional
    public List<ItemReservado> reservar(List<ItemStock> items) {
        List<ItemReservado> reservados = new ArrayList<>();
        // Se procesa en orden de ID para que dos compras simultáneas bloqueen filas en el mismo orden (evita interbloqueos).
        for (Map.Entry<Long, Integer> e : agrupar(items).entrySet()) {
            Producto p = productos.buscarConBloqueo(e.getKey())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Un producto del carrito ya no existe."));
            if (!p.isActivo() || !p.isHabilitado()) {
                throw new BusinessException(HttpStatus.CONFLICT, "El producto \"" + p.getNombre() + "\" ya no está disponible.");
            }
            if (p.getStock() < e.getValue()) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Stock insuficiente de \"" + p.getNombre() + "\": quedan " + p.getStock() + ".");
            }
            p.setStock(p.getStock() - e.getValue());
            productos.save(p);
            reservados.add(new ItemReservado(p.getId(), p.getNombre(), p.getPrecio(), e.getValue(),
                    p.getAgricultorId(), p.getAgricultorNombre(), p.getUnidad().getAbreviatura()));
        }
        return reservados;
    }

    @Transactional
    public void liberar(List<ItemStock> items) {
        for (Map.Entry<Long, Integer> e : agrupar(items).entrySet()) {
            productos.buscarConBloqueo(e.getKey()).ifPresent(p -> {
                p.setStock(p.getStock() + e.getValue());
                productos.save(p);
            });
        }
    }

    private Map<Long, Integer> agrupar(List<ItemStock> items) {
        Map<Long, Integer> porProducto = new TreeMap<>();
        for (ItemStock i : items) {
            porProducto.merge(i.productoId(), i.cantidad(), Integer::sum);
        }
        return porProducto;
    }
}
