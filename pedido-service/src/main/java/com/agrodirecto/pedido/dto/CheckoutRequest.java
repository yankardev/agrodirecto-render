package com.agrodirecto.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "La dirección de entrega es obligatoria") @Size(max = 200) String direccionEntrega,
        @NotBlank(message = "Elige el distrito de entrega") @Size(max = 80) String distrito,
        @NotBlank(message = "El teléfono de contacto es obligatorio") @Size(max = 20) String telefono) {
}
