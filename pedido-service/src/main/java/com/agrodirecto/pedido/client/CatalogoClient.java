package com.agrodirecto.pedido.client;

import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.contrato.ItemStock;
import com.agrodirecto.common.contrato.ReservaRequest;
import com.agrodirecto.common.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;

/** Cliente HTTP del servicio de catálogo. */
@Component
public class CatalogoClient {

    /** Lo que el carrito necesita saber de un producto. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductoInfo(Long id, String nombre, BigDecimal precio, int stock,
                               Long agricultorId, String agricultorNombre, String unidadAbrev) {
    }

    private static final ObjectMapper JSON = new ObjectMapper();
    private final RestClient http;

    public CatalogoClient(@Value("${services.catalogo.url}") String url,
                          @Value("${app.internal-key}") String claveInterna) {
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3000);
        fabrica.setReadTimeout(8000);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica)
                .defaultHeader("X-Internal-Key", claveInterna).build();
    }

    /** Consulta un producto con el token del cliente (el catálogo valida que sea visible). */
    public ProductoInfo obtener(Long productoId, String authorization) {
        try {
            return http.get().uri("/api/catalogo/{id}", productoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(ProductoInfo.class);
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    public List<ItemReservado> reservar(List<ItemStock> items) {
        try {
            return http.post().uri("/internal/stock/reservar")
                    .contentType(MediaType.APPLICATION_JSON).body(new ReservaRequest(items))
                    .retrieve().body(new ParameterizedTypeReference<List<ItemReservado>>() {
                    });
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    public void liberar(List<ItemStock> items) {
        try {
            http.post().uri("/internal/stock/liberar")
                    .contentType(MediaType.APPLICATION_JSON).body(new ReservaRequest(items))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    private BusinessException traducir(RestClientResponseException e) {
        String mensaje = "No se pudo completar la operación con el catálogo.";
        try {
            JsonNode n = JSON.readTree(e.getResponseBodyAsString());
            if (n.hasNonNull("message")) {
                mensaje = n.get("message").asText();
            }
        } catch (Exception ignorado) {
            // se usa el mensaje genérico
        }
        HttpStatus estado = HttpStatus.resolve(e.getStatusCode().value());
        if (estado == null || estado.is5xxServerError()) {
            estado = HttpStatus.BAD_GATEWAY;
        }
        return new BusinessException(estado, mensaje);
    }

    private BusinessException noDisponible() {
        return new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                "El catálogo no responde en este momento. Intenta de nuevo en unos segundos.");
    }
}
