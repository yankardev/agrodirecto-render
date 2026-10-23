package com.agrodirecto.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class AuthServiceApplicationTests {

    @Test
    void elContextoArranca() {
        // Verifica que la configuración (seguridad, JPA, beans) carga sin errores.
    }
}
