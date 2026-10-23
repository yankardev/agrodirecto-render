package com.agrodirecto.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgregarCarritoRequest(
        @NotNull(message = "Falta el producto") Long productoId,
        @NotNull(message = "Falta la cantidad") @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad) {
}
