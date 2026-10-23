package com.agrodirecto.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDto(Long id, Long compraId, Long agricultorId, String agricultor, String cliente,
                        String estado, BigDecimal subtotal, LocalDateTime fecha,
                        String direccionEntrega, String distrito, String telefono, List<DetalleDto> detalles) {
}
