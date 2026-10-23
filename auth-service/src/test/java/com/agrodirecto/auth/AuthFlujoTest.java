package com.agrodirecto.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba funcional de seguridad: login, roles y rutas protegidas (base H2 en memoria). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AuthFlujoTest {

    @Autowired
    MockMvc mvc;
    private final ObjectMapper json = new ObjectMapper();

    private String login(String email, String clave) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return json.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        assertFalse(login("cliente1@agrodirecto.com", "Agro123").isBlank());
    }

    @Test
    void loginConClaveIncorrectaDa401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente1@agrodirecto.com\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosSinTokenDa401() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeListarUsuarios() throws Exception {
        String token = login("cliente1@agrodirecto.com", "Agro123");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorSiPuedeListarUsuarios() throws Exception {
        String token = login("admin@agrodirecto.com", "Admin123");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void endpointInternoSinClaveEsRechazado() throws Exception {
        mvc.perform(get("/internal/usuarios/2/aprobado")).andExpect(status().is4xxClientError());
    }

    @Test
    void registroRechazaCorreoDuplicado() throws Exception {
        String cuerpo = "{\"nombre\":\"Rosa\",\"email\":\"rosa@test.com\",\"password\":\"clave123\",\"rol\":\"AGRICULTOR\"}";
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }
}
