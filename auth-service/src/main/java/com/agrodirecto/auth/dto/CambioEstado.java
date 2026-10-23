package com.agrodirecto.auth.dto;

import jakarta.validation.constraints.NotNull;

public record CambioEstado(@NotNull Boolean activo) {
}
