# AgroDirecto – Microservicios (Spring Boot + SQL Server)

Plataforma que conecta agricultores con clientes: catálogo, carrito, compras con pedidos por agricultor,
aprobación de agricultores, reportes exportables y avisos en tiempo real.

## Arquitectura

```
Navegador ──► api-gateway :8080 ──┬─► auth-service     :8081 ─► AgroAuthDB
 (HTML/JS)    (sirve el frontend  ├─► catalogo-service :8082 ─► AgroCatalogoDB
              y enruta /api/**)   └─► pedido-service   :8083 ─► AgroPedidoDB
                                      (los servicios se llaman entre sí por /internal/**)
```

| Módulo | Responsabilidad |
|---|---|
| `agro-common` | JWT, filtros de seguridad, manejo de errores y contratos compartidos |
| `auth-service` | Registro/login, usuarios, aprobación de agricultores, distritos |
| `catalogo-service` | Categorías, unidades, productos y **stock** (reserva con bloqueo) |
| `pedido-service` | Carrito, checkout, pedidos, **reportes PDF/Excel/CSV**, avisos SSE |
| `api-gateway` | Punto de entrada único + frontend en `src/main/resources/static` |

Capas dentro de cada servicio: `controller → service → repository → entity` (+ `dto`, `client`, `config`).

## Cómo se cubre el Logro del curso

| Requisito | Dónde está |
|---|---|
| Aplicación web de n capas | Frontend + gateway + servicios (controller/service/repository) + SQL Server |
| Servicios en la nube | Dockerfile + `docker-compose.yml` (desplegable en Azure/AWS/Render) |
| Metodología ágil | `docs/backlog-scrum.md` |
| Persistencia | JPA/Hibernate + SQL Server, una base por servicio |
| Visualización | Frontend web (catálogo, paneles por rol, reportes) |
| Versionamiento | Git + GitHub Actions (`.github/workflows/ci.yml`) |
| Seguridad transaccional | JWT + roles, `@Transactional`, reserva de stock con bloqueo pesimista, saga con compensación en el checkout |
| Pruebas funcionales y de rendimiento | JUnit/Mockito/MockMvc + JMeter (`tests/jmeter`), plan en `docs/plan-de-pruebas.md` |
| Reportes exportables | `/api/reportes/exportar` → PDF, Excel (.xlsx), CSV |

## Ejecutar en IntelliJ IDEA (modo rápido, sin instalar SQL Server)

1. **File ▸ Open** y elige el `pom.xml` de la raíz ▸ *Open as Project*. Espera a que Maven descargue las dependencias.
2. JDK 17 o superior (Project Structure ▸ SDK).
3. En la barra de ejecución elige **AgroDirecto - Todos** (aparecen 5 configuraciones en `.run/`) y pulsa ▶.
   Usan el perfil `h2` (base en memoria).
4. Abre **http://localhost:8080**.

> Si las configuraciones no aparecen: crea una *Application* por servicio con la clase `...Application`,
> módulo correspondiente y VM options `-Dspring.profiles.active=h2`.
> **Arranca `auth-service` antes que `catalogo-service`** si ejecutas uno por uno (los datos de prueba del catálogo asumen los IDs 2, 3 y 4 de los agricultores).

## Ejecutar con SQL Server

**Con Docker (todo el sistema):**
```bash
docker compose up --build        # luego http://localhost:8080
```

**SQL Server instalado en tu PC:** ejecuta `database/00_crear_bases.sql` y quita `-Dspring.profiles.active=h2`
de las configuraciones. Variables opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `INTERNAL_KEY`.

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@agrodirecto.com | Admin123 |
| Agricultor | agricultor1@agrodirecto.com (también 2 y 3) | Agro123 |
| Cliente | cliente1@agrodirecto.com | Agro123 |

Los agricultores nuevos quedan **pendientes**: el administrador debe aprobarlos en *Usuarios*.

## Pruebas

```bash
mvn test                 # unitarias + funcionales (H2, sin SQL Server)
```
Rendimiento: abre `tests/jmeter/agrodirecto-carga.jmx` en JMeter con el sistema levantado
(`jmeter -n -t tests/jmeter/agrodirecto-carga.jmx -Jusuarios=50 -l resultados.jtl`).

## Flujo de compra (patrón saga)

1. `pedido-service` pide al catálogo reservar el stock (`/internal/stock/reservar`): todo o nada, con bloqueo de filas.
2. Registra la compra y un pedido por agricultor en una transacción local.
3. Si el paso 2 falla, libera el stock (`/internal/stock/liberar`). Al cancelar un pedido también se devuelve.

## Notas

- Los endpoints `/internal/**` exigen la cabecera `X-Internal-Key` y el gateway **no** los enruta.
- Cambia `JWT_SECRET` e `INTERNAL_KEY` en cualquier entorno real; los valores por defecto son solo de desarrollo.
- Las tablas las crea Hibernate (`ddl-auto=update`); para producción se recomienda migraciones (Flyway).
- Los datos entre servicios son eventualmente consistentes (por ejemplo, al aprobar un agricultor se avisa al catálogo; si no responde, queda registrado en el log).
