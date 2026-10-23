package com.agrodirecto.common.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe devolverse al cliente. */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
