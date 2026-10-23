package com.agrodirecto.pedido.service;

import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.pedido.client.CatalogoClient;
import com.agrodirecto.pedido.client.CatalogoClient.ProductoInfo;
import com.agrodirecto.pedido.dto.CarritoDto;
import com.agrodirecto.pedido.dto.CarritoItemDto;
import com.agrodirecto.pedido.entity.CarritoItem;
import com.agrodirecto.pedido.repository.CarritoItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoItemRepository repo;
    private final CatalogoClient catalogo;

    public CarritoService(CarritoItemRepository repo, CatalogoClient catalogo) {
        this.repo = repo;
        this.catalogo = catalogo;
    }

    @Transactional(readOnly = true)
    public CarritoDto ver(Long clienteId) {
        List<CarritoItemDto> items = repo.findByClienteIdOrderByIdAsc(clienteId).stream()
                .map(i -> new CarritoItemDto(i.getProductoId(), i.getProductoNombre(), i.getPrecio(), i.getCantidad(),
                        i.getPrecio().multiply(BigDecimal.valueOf(i.getCantidad())),
                        i.getAgricultorId(), i.getAgricultorNombre(), i.getUnidad()))
                .toList();
        BigDecimal total = items.stream().map(CarritoItemDto::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantidad = items.stream().mapToInt(CarritoItemDto::cantidad).sum();
        return new CarritoDto(items, total, cantidad);
    }

    @Transactional
    public CarritoDto agregar(Long clienteId, Long productoId, int cantidad, String authorization) {
        ProductoInfo p = catalogo.obtener(productoId, authorization);
        CarritoItem item = repo.findByClienteIdAndProductoId(clienteId, productoId).orElseGet(() -> {
            CarritoItem nuevo = new CarritoItem();
            nuevo.setClienteId(clienteId);
            nuevo.setProductoId(p.id());
            nuevo.setProductoNombre(p.nombre());
            nuevo.setAgricultorId(p.agricultorId());
            nuevo.setAgricultorNombre(p.agricultorNombre());
            nuevo.setCantidad(0);
            return nuevo;
        });
        int total = item.getCantidad() + cantidad;
        if (total > p.stock()) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Solo quedan " + p.stock() + " " + p.unidadAbrev() + " de " + p.nombre() + ".");
        }
        item.setPrecio(p.precio());
        item.setUnidad(p.unidadAbrev());
        item.setCantidad(total);
        repo.save(item);
        return ver(clienteId);
    }

    @Transactional
    public CarritoDto cambiarCantidad(Long clienteId, Long productoId, int cantidad) {
        if (cantidad <= 0) {
            repo.deleteByClienteIdAndProductoId(clienteId, productoId);
        } else {
            CarritoItem item = repo.findByClienteIdAndProductoId(clienteId, productoId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Ese producto no está en tu carrito."));
            item.setCantidad(cantidad);
            repo.save(item);
        }
        return ver(clienteId);
    }

    @Transactional
    public CarritoDto quitar(Long clienteId, Long productoId) {
        repo.deleteByClienteIdAndProductoId(clienteId, productoId);
        return ver(clienteId);
    }

    @Transactional
    public void vaciar(Long clienteId) {
        repo.deleteByClienteId(clienteId);
    }
}
