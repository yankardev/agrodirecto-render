package com.agrodirecto.catalogo.client;

import com.agrodirecto.common.contrato.EstadoAprobacion;
import com.agrodirecto.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Consulta al servicio de autenticación si un agricultor está aprobado. */
@Component
public class AuthClient {

    private final RestClient http;

    public AuthClient(@Value("${services.auth.url}") String url,
                      @Value("${app.internal-key}") String claveInterna) {
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3000);
        fabrica.setReadTimeout(5000);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica)
                .defaultHeader("X-Internal-Key", claveInterna).build();
    }

    public boolean agricultorAprobado(Long agricultorId) {
        try {
            EstadoAprobacion r = http.get().uri("/internal/usuarios/{id}/aprobado", agricultorId)
                    .retrieve().body(EstadoAprobacion.class);
            return r != null && r.aprobado();
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo verificar tu cuenta. Intenta de nuevo en unos segundos.");
        }
    }
}
