package com.agrodirecto.pedido.report;

import com.agrodirecto.common.dto.PageResponse;
import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.pedido.entity.DetallePedido;
import com.agrodirecto.pedido.report.ReporteDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_EXPORTACION = 20_000;

    private final ReporteRepository repo;

    public ReporteService(ReporteRepository repo) {
        this.repo = repo;
    }

    public Resumen resumen(Filtro filtro) {
        Object[] r = repo.resumen(filtro.normalizado());
        long pedidos = r[0] == null ? 0 : ((Number) r[0]).longValue();
        long unidades = r[1] == null ? 0 : ((Number) r[1]).longValue();
        BigDecimal total = r[2] == null ? BigDecimal.ZERO : (BigDecimal) r[2];
        BigDecimal ticket = pedidos == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(pedidos), 2, RoundingMode.HALF_UP);
        return new Resumen(pedidos, unidades, total, ticket);
    }

    public List<ProductoVendido> productos(Filtro filtro) {
        return repo.productosMasVendidos(filtro.normalizado(), 10).stream()
                .map(o -> new ProductoVendido((String) o[0], ((Number) o[1]).longValue(), (BigDecimal) o[2]))
                .toList();
    }

    public List<VentaAgricultor> agricultores(Filtro filtro) {
        return repo.ventasPorAgricultor(filtro.normalizado()).stream()
                .map(o -> new VentaAgricultor((String) o[0], ((Number) o[1]).longValue(),
                        ((Number) o[2]).longValue(), (BigDecimal) o[3]))
                .toList();
    }

    public PageResponse<VentaDetalle> ventas(Filtro filtro, int pagina, int tamano) {
        Filtro f = filtro.normalizado();
        int p = Math.max(pagina, 0);
        int t = Math.min(Math.max(tamano, 1), 100);
        List<VentaDetalle> filas = repo.ventas(f, p * t, t).stream().map(this::aDetalle).toList();
        return PageResponse.de(filas, p, t, repo.contarVentas(f));
    }

    /** Arma la tabla del reporte pedido (ventas, productos o agricultores) para exportarla. */
    public Tabla tabla(String tipo, Filtro filtro) {
        Filtro f = filtro.normalizado();
        return switch (tipo == null ? "" : tipo.toLowerCase()) {
            case "productos" -> new Tabla("Productos más vendidos",
                    List.of("Producto", "Unidades vendidas", "Total (S/)"),
                    productos(f).stream().map(x -> List.of(x.producto(), String.valueOf(x.cantidad()), dinero(x.total()))).toList());
            case "agricultores" -> new Tabla("Ventas por agricultor",
                    List.of("Agricultor", "Pedidos", "Unidades", "Total (S/)"),
                    agricultores(f).stream().map(x -> List.of(x.agricultor(), String.valueOf(x.pedidos()),
                            String.valueOf(x.unidades()), dinero(x.total()))).toList());
            case "ventas" -> new Tabla("Detalle de ventas",
                    List.of("Fecha", "Compra", "Pedido", "Producto", "Agricultor", "Cant.", "Precio (S/)", "Subtotal (S/)", "Estado"),
                    repo.ventas(f, 0, MAX_EXPORTACION).stream().map(this::aDetalle).map(v -> List.of(
                            v.fecha().format(FECHA), "#" + v.compraId(), "#" + v.pedidoId(), v.producto(), v.agricultor(),
                            String.valueOf(v.cantidad()), dinero(v.precio()), dinero(v.subtotal()), v.estado())).toList());
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Tipo de reporte no válido. Usa: ventas, productos o agricultores.");
        };
    }

    private VentaDetalle aDetalle(DetallePedido d) {
        var p = d.getPedido();
        return new VentaDetalle(p.getFecha(), p.getCompra().getId(), p.getId(), d.getProductoNombre(),
                p.getAgricultorNombre(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal(), p.getEstado().name());
    }

    private static String dinero(BigDecimal n) {
        return n.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
