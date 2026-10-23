package com.agrodirecto.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoDto(List<CarritoItemDto> items, BigDecimal total, int cantidadTotal) {
}
