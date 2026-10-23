package com.agrodirecto.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-prueba-con-al-menos-32-caracteres-ok";
    private final JwtService jwt = new JwtService(SECRETO, 5);
    private final AuthUser ana = new AuthUser(7L, "ana@correo.com", "Ana", "CLIENTE");

    @Test
    void generaYValidaUnToken() {
        var usuario = jwt.validar(jwt.generar(ana));
        assertTrue(usuario.isPresent());
        assertEquals(7L, usuario.get().id());
        assertEquals("CLIENTE", usuario.get().rol());
        assertEquals("ana@correo.com", usuario.get().email());
    }

    @Test
    void rechazaUnTokenManipulado() {
        assertTrue(jwt.validar(jwt.generar(ana) + "x").isEmpty());
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        var otro = new JwtService("otro-secreto-distinto-con-mas-de-32-caracteres-ok", 5);
        assertTrue(jwt.validar(otro.generar(ana)).isEmpty());
    }

    @Test
    void rechazaUnTokenExpirado() {
        var expirado = new JwtService(SECRETO, -1);
        assertTrue(jwt.validar(expirado.generar(ana)).isEmpty());
    }

    @Test
    void rechazaTextoQueNoEsUnToken() {
        assertTrue(jwt.validar("no-es-un-token").isEmpty());
    }
}
