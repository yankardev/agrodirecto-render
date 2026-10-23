package com.agrodirecto.pedido.repository;

import com.agrodirecto.pedido.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByClienteIdOrderByFechaDesc(Long clienteId);
}
