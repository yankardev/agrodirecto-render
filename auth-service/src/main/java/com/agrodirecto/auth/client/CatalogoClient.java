package com.agrodirecto.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Avisa al servicio de catálogo cuando cambia la aprobación de un agricultor. */
@Component
public class CatalogoClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogoClient.class);
    private final RestClient http;

    public CatalogoClient(@Value("${services.catalogo.url}") String url,
                          @Value("${app.internal-key}") String claveInterna) {
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3000);
        fabrica.setReadTimeout(5000);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica)
                .defaultHeader("X-Internal-Key", claveInterna).build();
    }

    /** Muestra u oculta todos los productos del agricultor. Si el catálogo no responde, solo se registra el aviso. */
    public void habilitarProductos(Long agricultorId, boolean habilitado) {
        try {
            http.put().uri("/internal/agricultores/{id}/habilitacion?habilitado={h}", agricultorId, habilitado)
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo sincronizar con catalogo-service (agricultor {}): {}", agricultorId, e.getMessage());
        }
    }
}
