package com.agrodirecto.pedido.repository;

import com.agrodirecto.pedido.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    List<CarritoItem> findByClienteIdOrderByIdAsc(Long clienteId);

    Optional<CarritoItem> findByClienteIdAndProductoId(Long clienteId, Long productoId);

    void deleteByClienteId(Long clienteId);

    void deleteByClienteIdAndProductoId(Long clienteId, Long productoId);
}
