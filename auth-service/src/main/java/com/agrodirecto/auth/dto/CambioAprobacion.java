package com.agrodirecto.auth.dto;

import jakarta.validation.constraints.NotNull;

public record CambioAprobacion(@NotNull Boolean aprobado) {
}
