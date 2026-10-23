package com.agrodirecto.common.contrato;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemStock(@NotNull Long productoId, @Min(value = 1, message = "La cantidad debe ser mayor a cero") int cantidad) {
}
