package com.agrodirecto.pedido.report;

import com.agrodirecto.common.dto.PageResponse;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.pedido.report.ReporteDtos.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Reportes de ventas (administrador). Se ven en pantalla o se exportan a PDF, Excel y CSV. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;
    private final ExportadorReportes exportador;

    public ReporteController(ReporteService service, ExportadorReportes exportador) {
        this.service = service;
        this.exportador = exportador;
    }

    @GetMapping("/resumen")
    public Resumen resumen(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                           @RequestParam(required = false) Long agricultorId) {
        return service.resumen(new Filtro(desde, hasta, agricultorId));
    }

    @GetMapping("/productos")
    public List<ProductoVendido> productos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                           @RequestParam(required = false) Long agricultorId) {
        return service.productos(new Filtro(desde, hasta, agricultorId));
    }

    @GetMapping("/agricultores")
    public List<VentaAgricultor> agricultores(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                              @RequestParam(required = false) Long agricultorId) {
        return service.agricultores(new Filtro(desde, hasta, agricultorId));
    }

    @GetMapping("/ventas")
    public PageResponse<VentaDetalle> ventas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                             @RequestParam(required = false) Long agricultorId,
                                             @RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "10") int tamano) {
        return service.ventas(new Filtro(desde, hasta, agricultorId), pagina, tamano);
    }

    /** GET /api/reportes/exportar?tipo=ventas|productos|agricultores&formato=pdf|xlsx|csv */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam String tipo,
                                           @RequestParam(defaultValue = "pdf") String formato,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                           @RequestParam(required = false) Long agricultorId) {
        Tabla tabla = service.tabla(tipo, new Filtro(desde, hasta, agricultorId));
        byte[] contenido;
        MediaType tipoMedia;
        String extension;
        switch (formato.toLowerCase()) {
            case "pdf" -> {
                contenido = exportador.pdf(tabla);
                tipoMedia = MediaType.APPLICATION_PDF;
                extension = "pdf";
            }
            case "xlsx", "excel" -> {
                contenido = exportador.excel(tabla);
                tipoMedia = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                extension = "xlsx";
            }
            case "csv" -> {
                contenido = exportador.csv(tabla);
                tipoMedia = MediaType.parseMediaType("text/csv;charset=UTF-8");
                extension = "csv";
            }
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "Formato no válido. Usa: pdf, xlsx o csv.");
        }
        String nombre = "reporte-" + tipo.toLowerCase() + "-" + LocalDate.now() + "." + extension;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .contentType(tipoMedia)
                .body(contenido);
    }
}
