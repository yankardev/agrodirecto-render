package com.agrodirecto.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraDto(Long id, LocalDateTime fecha, BigDecimal total, String estado, String direccionEntrega,
                        String distrito, String telefono, String metodoPago, List<PedidoDto> pedidos) {
}
