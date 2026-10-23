package com.agrodirecto.common.contrato;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReservaRequest(@NotEmpty(message = "La reserva no tiene productos") List<@Valid ItemStock> items) {
}
