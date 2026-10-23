package com.agrodirecto.pedido.report;

import com.agrodirecto.pedido.entity.DetallePedido;
import com.agrodirecto.pedido.entity.EstadoPedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Consultas de agregación. Los pedidos cancelados nunca cuentan como venta. */
@Repository
public class ReporteRepository {

    private static final String DESDE = " from DetallePedido d join d.pedido p where p.estado <> :cancelado";

    @PersistenceContext
    private EntityManager em;

    public Object[] resumen(Filtro f) {
        TypedQuery<Object[]> q = em.createQuery(
                "select count(distinct p.id), sum(d.cantidad), sum(d.subtotal)" + DESDE + condiciones(f), Object[].class);
        return parametros(q, f).getSingleResult();
    }

    public List<Object[]> productosMasVendidos(Filtro f, int limite) {
        TypedQuery<Object[]> q = em.createQuery(
                "select d.productoNombre, sum(d.cantidad), sum(d.subtotal)" + DESDE + condiciones(f)
                        + " group by d.productoNombre order by sum(d.subtotal) desc", Object[].class);
        return parametros(q, f).setMaxResults(limite).getResultList();
    }

    public List<Object[]> ventasPorAgricultor(Filtro f) {
        TypedQuery<Object[]> q = em.createQuery(
                "select p.agricultorNombre, count(distinct p.id), sum(d.cantidad), sum(d.subtotal)" + DESDE + condiciones(f)
                        + " group by p.agricultorId, p.agricultorNombre order by sum(d.subtotal) desc", Object[].class);
        return parametros(q, f).getResultList();
    }

    public List<DetallePedido> ventas(Filtro f, int primero, int maximo) {
        TypedQuery<DetallePedido> q = em.createQuery(
                "select d from DetallePedido d join fetch d.pedido p where p.estado <> :cancelado" + condiciones(f)
                        + " order by p.fecha desc, d.id desc", DetallePedido.class);
        return parametros(q, f).setFirstResult(primero).setMaxResults(maximo).getResultList();
    }

    public long contarVentas(Filtro f) {
        TypedQuery<Long> q = em.createQuery("select count(d)" + DESDE + condiciones(f), Long.class);
        return parametros(q, f).getSingleResult();
    }

    private String condiciones(Filtro f) {
        StringBuilder sb = new StringBuilder();
        if (f.desde() != null) sb.append(" and p.fecha >= :desde");
        if (f.hasta() != null) sb.append(" and p.fecha < :hasta");
        if (f.agricultorId() != null) sb.append(" and p.agricultorId = :agricultor");
        return sb.toString();
    }

    private <T> TypedQuery<T> parametros(TypedQuery<T> q, Filtro f) {
        q.setParameter("cancelado", EstadoPedido.CANCELADO);
        if (f.desde() != null) q.setParameter("desde", f.desde().atStartOfDay());
        if (f.hasta() != null) q.setParameter("hasta", f.hasta().plusDays(1).atStartOfDay());
        if (f.agricultorId() != null) q.setParameter("agricultor", f.agricultorId());
        return q;
    }
}
