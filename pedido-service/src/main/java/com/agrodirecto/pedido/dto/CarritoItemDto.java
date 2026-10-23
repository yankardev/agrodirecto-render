package com.agrodirecto.pedido.dto;

import java.math.BigDecimal;

public record CarritoItemDto(Long productoId, String producto, BigDecimal precio, int cantidad,
                             BigDecimal subtotal, Long agricultorId, String agricultor, String unidad) {
}
