package com.agrodirecto.pedido.repository;

import com.agrodirecto.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByAgricultorIdOrderByFechaDesc(Long agricultorId);
}
