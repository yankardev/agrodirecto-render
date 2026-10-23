package com.agrodirecto.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DistritoRequest(
        @NotBlank(message = "El nombre del distrito es obligatorio") @Size(max = 80) String nombre,
        Boolean activo) {
}
