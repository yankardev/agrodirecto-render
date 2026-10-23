package com.agrodirecto.pedido.dto;

import java.math.BigDecimal;

public record DetalleDto(Long productoId, String producto, BigDecimal precio, int cantidad, BigDecimal subtotal) {
}
