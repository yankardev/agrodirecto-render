package com.agrodirecto.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioEstadoRequest(@NotBlank(message = "Falta el nuevo estado") String estado) {
}
