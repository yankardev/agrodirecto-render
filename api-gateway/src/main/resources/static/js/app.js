/* AgroDirecto - interfaz web (sin framework). Rutas con hash: #/catalogo, #/carrito, ... */
const $ = (sel, el = document) => el.querySelector(sel);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const soles = n => 'S/ ' + Number(n || 0).toFixed(2);
const fechaHora = f => new Date(f).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' });

const EMOJIS = [['papa', '🥔'], ['camote', '🍠'], ['yuca', '🥔'], ['zanahoria', '🥕'], ['tomate', '🍅'], ['cebolla', '🧅'],
  ['lechuga', '🥬'], ['palta', '🥑'], ['mandarina', '🍊'], ['plátano', '🍌'], ['fresa', '🍓'], ['quinua', '🌾'],
  ['maíz', '🌽'], ['haba', '🫛'], ['queso', '🧀'], ['huevo', '🥚'], ['culantro', '🌿'], ['limón', '🍋'], ['uva', '🍇'], ['ajo', '🧄']];
const FONDOS = ['#e3efd9', '#fbe9b7', '#ecdcef', '#d9ebf0', '#f6dccf'];
const emojiDe = nombre => (EMOJIS.find(([k]) => nombre.toLowerCase().includes(k)) || [0, '🌱'])[1];

/* ---------- utilidades de interfaz ---------- */
function aviso(texto, tipo = 'ok') {
  const el = document.createElement('div');
  el.className = 'aviso ' + (tipo === 'error' ? 'error' : '');
  el.textContent = texto;
  $('#avisos').appendChild(el);
  setTimeout(() => el.remove(), 5000);
}
const vista = html => { $('#vista').innerHTML = html; };
const cargando = () => vista('<p class="vacio">Cargando…</p>');
const errorVista = e => vista(`<div class="vacio"><p>${esc(e.message)}</p><button class="btn btn-outline-primary" data-accion="recargar">Reintentar</button></div>`);
const estadoBadge = e => `<span class="estado ${esc(e)}">${esc(e.charAt(0) + e.slice(1).toLowerCase())}</span>`;

const MENU = {
  CLIENTE: [['#/catalogo', 'Catálogo'], ['#/carrito', 'Carrito'], ['#/compras', 'Mis compras']],
  AGRICULTOR: [['#/productos', 'Mis productos'], ['#/pedidos', 'Pedidos recibidos']],
  ADMINISTRADOR: [['#/catalogo', 'Catálogo'], ['#/admin/usuarios', 'Usuarios'], ['#/admin/categorias', 'Categorías'],
    ['#/admin/unidades', 'Unidades'], ['#/admin/distritos', 'Distritos'], ['#/admin/reportes', 'Reportes']]
};
const INICIO = { CLIENTE: '#/catalogo', AGRICULTOR: '#/productos', ADMINISTRADOR: '#/admin/reportes' };
let carritoCantidad = 0;
let flujoAvisos = null;

function dibujarBarra(rutaActual) {
  const s = Sesion.leer();
  const links = s ? MENU[s.usuario.rol].map(([h, t]) => {
    const extra = h === '#/carrito' && carritoCantidad > 0 ? `<span class="insignia">${carritoCantidad}</span>` : '';
    return `<li><a href="${h}" class="${rutaActual === h.slice(1) ? 'activo' : ''}">${t}${extra}</a></li>`;
  }).join('') : '';
  const derecha = s
    ? `<div class="usuario"><span>${esc(s.usuario.nombre)}</span><button data-accion="salir">Salir</button></div>`
    : `<div class="usuario"><a class="text-white" href="#/login">Ingresar</a><a class="btn btn-primary btn-sm" href="#/registro">Crear cuenta</a></div>`;
  $('#barra').innerHTML = `<div class="barra-int"><a class="marca" href="#/">Agro<span>Directo</span></a><ul class="menu">${links}</ul>${derecha}</div>`;
}

async function refrescarCarrito() {
  const s = Sesion.leer();
  if (!s || s.usuario.rol !== 'CLIENTE') { carritoCantidad = 0; return; }
  try { carritoCantidad = (await api('GET', '/api/carrito')).cantidadTotal; } catch { /* no bloquea */ }
  dibujarBarra(rutaActual());
}

function conectarAvisos() {
  const s = Sesion.leer();
  if (flujoAvisos) { flujoAvisos.close(); flujoAvisos = null; }
  if (!s) return;
  flujoAvisos = new EventSource('/api/notificaciones/stream?token=' + encodeURIComponent(s.token));
  const alRecibir = recargarEn => e => {
    try { aviso(JSON.parse(e.data).mensaje); } catch { /* ignorar */ }
    if (rutaActual() === recargarEn) router();
  };
  flujoAvisos.addEventListener('pedido-nuevo', alRecibir('/pedidos'));
  flujoAvisos.addEventListener('pedido-actualizado', alRecibir('/compras'));
}

/* ---------- enrutador ---------- */
const rutaActual = () => location.hash.slice(1) || '/';

const RUTAS = [
  ['/', null, vInicio], ['/login', null, vLogin], ['/registro', null, vRegistro],
  ['/catalogo', ['CLIENTE', 'ADMINISTRADOR'], vCatalogo], ['/carrito', ['CLIENTE'], vCarrito], ['/compras', ['CLIENTE'], vCompras],
  ['/productos', ['AGRICULTOR'], vMisProductos], ['/productos/nuevo', ['AGRICULTOR'], () => vFormProducto(null)],
  [/^\/productos\/editar\/(\d+)$/, ['AGRICULTOR'], m => vFormProducto(m[1])], ['/pedidos', ['AGRICULTOR'], vPedidos],
  ['/admin/usuarios', ['ADMINISTRADOR'], vUsuarios], ['/admin/reportes', ['ADMINISTRADOR'], vReportes],
  ['/admin/categorias', ['ADMINISTRADOR'], () => vMantenimiento(CFG_CATEGORIA)],
  ['/admin/unidades', ['ADMINISTRADOR'], () => vMantenimiento(CFG_UNIDAD)],
  ['/admin/distritos', ['ADMINISTRADOR'], () => vMantenimiento(CFG_DISTRITO)]
];

async function router() {
  const ruta = rutaActual();
  const s = Sesion.leer();
  dibujarBarra(ruta);
  for (const [patron, roles, fn] of RUTAS) {
    const m = patron instanceof RegExp ? ruta.match(patron) : (patron === ruta ? [] : null);
    if (!m) continue;
    if (roles && (!s || !roles.includes(s.usuario.rol))) {
      location.hash = s ? INICIO[s.usuario.rol] : '#/login';
      return;
    }
    try { await fn(m); } catch (e) { errorVista(e); }
    $('#vista').focus({ preventScroll: true });
    return;
  }
  vista('<p class="vacio">Esta página no existe.</p>');
}

/* ---------- portada, login y registro ---------- */
function vInicio() {
  const s = Sesion.leer();
  if (s) { location.hash = INICIO[s.usuario.rol]; return; }
  vista(`
  <section class="portada">
    <div>
      <h1>Compra directo a quien cultiva.</h1>
      <p class="lead">Papas, frutas, granos y lácteos de agricultores de Lima y alrededores. Sin intermediarios: el agricultor fija su precio y tú sabes de quién viene cada producto.</p>
      <div class="acciones"><a class="btn btn-primary btn-lg" href="#/registro">Crear cuenta</a><a class="btn btn-outline-primary btn-lg" href="#/login">Ya tengo cuenta</a></div>
    </div>
    <div class="puesto" aria-hidden="true">
      ${['🥔', '🥑', '🌽', '🍊', '🥕', '🧀'].map((e, i) => `<div style="background:${FONDOS[i % 5]}">${e}</div>`).join('')}
    </div>
  </section>
  <section class="pasos">
    <div><h3>Elige</h3><p>Navega el catálogo y filtra por categoría.</p></div>
    <div><h3>Pide</h3><p>Tu compra se divide automáticamente por agricultor.</p></div>
    <div><h3>Recibe</h3><p>Cada agricultor confirma y entrega su parte. Pagas contra entrega.</p></div>
  </section>`);
}

function vLogin() {
  vista(`
  <div class="panel mx-auto" style="max-width:420px">
    <h1 class="h3 mb-3">Ingresar</h1>
    <form id="f-login" class="d-grid gap-3">
      <label>Correo<input class="form-control" type="email" name="email" required autocomplete="username"></label>
      <label>Contraseña<input class="form-control" type="password" name="password" required autocomplete="current-password"></label>
      <button class="btn btn-primary" type="submit">Ingresar</button>
    </form>
    <p class="mt-3 mb-0">¿No tienes cuenta? <a href="#/registro">Crear cuenta</a></p>
    <details class="mt-3"><summary>Cuentas de prueba</summary>
      <small>admin@agrodirecto.com / Admin123<br>agricultor1@agrodirecto.com / Agro123<br>cliente1@agrodirecto.com / Agro123</small></details>
  </div>`);
  $('#f-login').onsubmit = async ev => {
    ev.preventDefault();
    const f = Object.fromEntries(new FormData(ev.target));
    try {
      const r = await api('POST', '/api/auth/login', f);
      Sesion.guardar({ token: r.token, usuario: r.usuario });
      conectarAvisos();
      await refrescarCarrito();
      location.hash = INICIO[r.usuario.rol];
    } catch (e) { aviso(e.message, 'error'); }
  };
}

async function vRegistro() {
  const distritos = (await api('GET', '/api/distritos')).filter(d => d.activo);
  vista(`
  <div class="panel mx-auto" style="max-width:520px">
    <h1 class="h3 mb-3">Crear cuenta</h1>
    <form id="f-registro" class="d-grid gap-3">
      <label>Soy
        <select class="form-select" name="rol"><option value="CLIENTE">Cliente (quiero comprar)</option><option value="AGRICULTOR">Agricultor (quiero vender)</option></select></label>
      <label>Nombre completo<input class="form-control" name="nombre" required maxlength="100"></label>
      <label>Correo<input class="form-control" type="email" name="email" required></label>
      <label>Contraseña<input class="form-control" type="password" name="password" required minlength="6"></label>
      <label>Teléfono<input class="form-control" name="telefono" maxlength="20"></label>
      <label>Dirección<input class="form-control" name="direccion" maxlength="200"></label>
      <label>Distrito<select class="form-select" name="distrito"><option value="">Elige…</option>${distritos.map(d => `<option>${esc(d.nombre)}</option>`).join('')}</select></label>
      <button class="btn btn-primary" type="submit">Crear cuenta</button>
    </form>
  </div>`);
  $('#f-registro').onsubmit = async ev => {
    ev.preventDefault();
    const f = Object.fromEntries(new FormData(ev.target));
    try {
      await api('POST', '/api/auth/registro', f);
      aviso(f.rol === 'AGRICULTOR' ? 'Cuenta creada. Un administrador debe aprobarla antes de que publiques productos.' : 'Cuenta creada. Ya puedes ingresar.');
      location.hash = '#/login';
    } catch (e) { aviso(e.message, 'error'); }
  };
}

/* ---------- cliente: catálogo, carrito, compras ---------- */
let filtroCatalogo = { q: '', categoriaId: '', pagina: 0 };
async function vCatalogo() {
  const s = Sesion.leer();
  const categorias = (await api('GET', '/api/categorias')).filter(c => c.activo);
  const f = filtroCatalogo;
  const qs = new URLSearchParams({ pagina: f.pagina, tamano: 12 });
  if (f.q) qs.set('q', f.q);
  if (f.categoriaId) qs.set('categoriaId', f.categoriaId);
  const r = await api('GET', '/api/catalogo?' + qs);
  const esCliente = s.usuario.rol === 'CLIENTE';
  vista(`
  <div class="titulo-vista"><h1>Catálogo</h1></div>
  <form id="f-filtro" class="filtros">
    <input class="form-control" type="search" name="q" placeholder="Buscar producto" value="${esc(f.q)}" aria-label="Buscar producto">
    <select class="form-select" name="categoriaId" aria-label="Categoría"><option value="">Todas las categorías</option>
      ${categorias.map(c => `<option value="${c.id}" ${String(c.id) === String(f.categoriaId) ? 'selected' : ''}>${esc(c.nombre)}</option>`).join('')}</select>
    <button class="btn btn-outline-primary" type="submit">Buscar</button>
  </form>
  ${r.contenido.length === 0 ? '<p class="vacio">No encontramos productos con esos filtros.</p>' : `
  <div class="rejilla">${r.contenido.map(p => `
    <article class="etiqueta">
      <div class="foto" style="background:${FONDOS[p.categoriaId % 5]}" aria-hidden="true">${emojiDe(p.nombre)}</div>
      <div class="cuerpo">
        <h3>${esc(p.nombre)}</h3>
        <div class="de">de ${esc(p.agricultorNombre)} · ${esc(p.categoria)}</div>
        <div class="precio">${soles(p.precio)} <small>/ ${esc(p.unidadAbrev)}</small></div>
        <div class="stock ${p.stock <= 10 ? 'bajo' : ''}">${p.stock === 0 ? 'Agotado' : (p.stock <= 10 ? 'Quedan solo ' + p.stock : p.stock + ' disponibles')}</div>
        ${esCliente ? `<div class="compra"><input class="form-control form-control-sm" type="number" min="1" max="${p.stock}" value="1" aria-label="Cantidad de ${esc(p.nombre)}" id="cant-${p.id}">
          <button class="btn btn-primary btn-sm flex-fill" data-accion="agregar" data-id="${p.id}" ${p.stock === 0 ? 'disabled' : ''}>Agregar</button></div>` : ''}
      </div>
    </article>`).join('')}</div>
  ${r.totalPaginas > 1 ? `<nav class="d-flex gap-2 justify-content-center mt-4">
    <button class="btn btn-outline-primary btn-sm" data-accion="pagina" data-p="${r.pagina - 1}" ${r.pagina === 0 ? 'disabled' : ''}>Anterior</button>
    <span class="align-self-center">Página ${r.pagina + 1} de ${r.totalPaginas}</span>
    <button class="btn btn-outline-primary btn-sm" data-accion="pagina" data-p="${r.pagina + 1}" ${r.pagina + 1 >= r.totalPaginas ? 'disabled' : ''}>Siguiente</button></nav>` : ''}`}`);
  $('#f-filtro').onsubmit = ev => {
    ev.preventDefault();
    const d = Object.fromEntries(new FormData(ev.target));
    filtroCatalogo = { q: d.q.trim(), categoriaId: d.categoriaId, pagina: 0 };
    router();
  };
}

async function vCarrito() {
  const [c, distritos] = await Promise.all([api('GET', '/api/carrito'), api('GET', '/api/distritos')]);
  carritoCantidad = c.cantidadTotal; dibujarBarra('/carrito');
  const u = (await api('GET', '/api/auth/me'));
  if (c.items.length === 0) {
    vista('<div class="vacio"><h1 class="h3">Tu carrito está vacío</h1><p>Agrega productos desde el catálogo.</p><a class="btn btn-primary" href="#/catalogo">Ver catálogo</a></div>');
    return;
  }
  vista(`
  <div class="titulo-vista"><h1>Carrito</h1><button class="btn btn-outline-primary btn-sm" data-accion="vaciar">Vaciar carrito</button></div>
  <div class="panel tabla-wrap"><table class="table align-middle mb-0">
    <thead><tr><th>Producto</th><th>Agricultor</th><th class="text-end">Precio</th><th style="width:110px">Cantidad</th><th class="text-end">Subtotal</th><th></th></tr></thead>
    <tbody>${c.items.map(i => `<tr>
      <td>${emojiDe(i.producto)} ${esc(i.producto)}</td><td>${esc(i.agricultor)}</td>
      <td class="text-end">${soles(i.precio)}/${esc(i.unidad)}</td>
      <td><input class="form-control form-control-sm" type="number" min="1" value="${i.cantidad}" data-accion="cantidad" data-id="${i.productoId}" aria-label="Cantidad de ${esc(i.producto)}"></td>
      <td class="text-end">${soles(i.subtotal)}</td>
      <td><button class="btn btn-sm btn-outline-primary" data-accion="quitar" data-id="${i.productoId}" aria-label="Quitar ${esc(i.producto)}">Quitar</button></td></tr>`).join('')}</tbody>
    <tfoot><tr><th colspan="4" class="text-end">Total</th><th class="text-end">${soles(c.total)}</th><th></th></tr></tfoot></table></div>
  <form id="f-checkout" class="panel d-grid gap-3">
    <h2 class="h5 mb-0">Datos de entrega</h2>
    <p class="mb-0 text-secondary">Pagas contra entrega. Tu compra se reparte en un pedido por agricultor.</p>
    <label>Dirección<input class="form-control" name="direccionEntrega" required maxlength="200" value="${esc(u.direccion)}"></label>
    <label>Distrito<select class="form-select" name="distrito" required><option value="">Elige…</option>
      ${distritos.filter(d => d.activo).map(d => `<option ${d.nombre === u.distrito ? 'selected' : ''}>${esc(d.nombre)}</option>`).join('')}</select></label>
    <label>Teléfono de contacto<input class="form-control" name="telefono" required maxlength="20" value="${esc(u.telefono)}"></label>
    <button class="btn btn-primary btn-lg" type="submit">Confirmar compra · ${soles(c.total)}</button>
  </form>`);
  $('#f-checkout').onsubmit = async ev => {
    ev.preventDefault();
    const btn = $('button[type=submit]', ev.target); btn.disabled = true;
    try {
      const compra = await api('POST', '/api/compras/checkout', Object.fromEntries(new FormData(ev.target)));
      aviso(`Compra #${compra.id} registrada. Total ${soles(compra.total)}.`);
      carritoCantidad = 0;
      location.hash = '#/compras';
    } catch (e) { aviso(e.message, 'error'); btn.disabled = false; }
  };
}

async function vCompras() {
  const compras = await api('GET', '/api/compras');
  vista(`<div class="titulo-vista"><h1>Mis compras</h1></div>` + (compras.length === 0
    ? '<p class="vacio">Aún no has hecho compras.</p>'
    : compras.map(c => `
    <section class="pedido">
      <header><strong>Compra #${c.id}</strong><span>${fechaHora(c.fecha)}</span>${estadoBadge(c.estado)}<span class="ms-auto fw-semibold">${soles(c.total)}</span></header>
      ${c.pedidos.map(p => `<ul><li class="list-unstyled ms-n3 mb-1"><strong>${esc(p.agricultor)}</strong> ${estadoBadge(p.estado)}</li>
        ${p.detalles.map(d => `<li>${d.cantidad} × ${esc(d.producto)} — ${soles(d.subtotal)}</li>`).join('')}</ul>`).join('')}
      <div class="pie text-secondary">Entrega: ${esc(c.direccionEntrega)}, ${esc(c.distrito)} · ${esc(c.metodoPago)}</div>
    </section>`).join('')));
}

/* ---------- agricultor ---------- */
async function vMisProductos() {
  const [yo, productos] = await Promise.all([api('GET', '/api/auth/me'), api('GET', '/api/mis-productos')]);
  const s = Sesion.leer(); s.usuario = yo; Sesion.guardar(s);
  vista(`
  <div class="titulo-vista"><h1>Mis productos</h1><a class="btn btn-primary" href="#/productos/nuevo">Publicar producto</a></div>
  ${yo.aprobado ? '' : '<div class="aviso-cuenta">Tu cuenta está pendiente de aprobación. Cuando el administrador la apruebe podrás publicar y tus productos serán visibles.</div>'}
  ${productos.length === 0 ? '<p class="vacio">Todavía no publicaste productos.</p>' : `
  <div class="panel tabla-wrap"><table class="table align-middle mb-0">
    <thead><tr><th>Producto</th><th>Categoría</th><th class="text-end">Precio</th><th class="text-end">Stock</th><th>Estado</th><th></th></tr></thead>
    <tbody>${productos.map(p => `<tr>
      <td>${emojiDe(p.nombre)} ${esc(p.nombre)}</td><td>${esc(p.categoria)}</td>
      <td class="text-end">${soles(p.precio)}/${esc(p.unidadAbrev)}</td><td class="text-end">${p.stock}</td>
      <td><span class="estado ${p.activo && p.habilitado ? 'si' : 'no'}">${!p.habilitado ? 'Sin aprobar' : (p.activo ? 'Visible' : 'Oculto')}</span></td>
      <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="#/productos/editar/${p.id}">Editar</a>
        ${p.activo ? `<button class="btn btn-sm btn-outline-primary" data-accion="ocultar" data-id="${p.id}">Ocultar</button>` : ''}</td></tr>`).join('')}</tbody></table></div>`}`);
}

async function vFormProducto(id) {
  const [cats, unis, p] = await Promise.all([api('GET', '/api/categorias'), api('GET', '/api/unidades'), id ? api('GET', '/api/mis-productos/' + id) : null]);
  const v = p || { nombre: '', descripcion: '', precio: '', stock: 0, categoriaId: '', unidadId: '', activo: true };
  const opciones = (lista, sel) => lista.filter(x => x.activo || x.id === sel).map(x => `<option value="${x.id}" ${x.id === sel ? 'selected' : ''}>${esc(x.nombre)}</option>`).join('');
  vista(`
  <div class="panel mx-auto" style="max-width:560px">
    <h1 class="h3 mb-3">${id ? 'Editar producto' : 'Publicar producto'}</h1>
    <form id="f-producto" class="d-grid gap-3">
      <label>Nombre<input class="form-control" name="nombre" required maxlength="100" value="${esc(v.nombre)}"></label>
      <label>Descripción<textarea class="form-control" name="descripcion" maxlength="300" rows="2">${esc(v.descripcion)}</textarea></label>
      <div class="row g-3">
        <label class="col">Precio (S/)<input class="form-control" type="number" step="0.01" min="0.01" name="precio" required value="${esc(v.precio)}"></label>
        <label class="col">Stock<input class="form-control" type="number" min="0" name="stock" required value="${esc(v.stock)}"></label></div>
      <div class="row g-3">
        <label class="col">Categoría<select class="form-select" name="categoriaId" required><option value="">Elige…</option>${opciones(cats, v.categoriaId)}</select></label>
        <label class="col">Unidad<select class="form-select" name="unidadId" required><option value="">Elige…</option>${opciones(unis, v.unidadId)}</select></label></div>
      <div class="form-check"><input class="form-check-input" type="checkbox" name="activo" id="activo" ${v.activo ? 'checked' : ''}><label class="form-check-label" for="activo">Visible en el catálogo</label></div>
      <div class="d-flex gap-2"><button class="btn btn-primary" type="submit">Guardar</button><a class="btn btn-outline-primary" href="#/productos">Cancelar</a></div>
    </form>
  </div>`);
  $('#f-producto').onsubmit = async ev => {
    ev.preventDefault();
    const d = Object.fromEntries(new FormData(ev.target));
    const cuerpo = { nombre: d.nombre, descripcion: d.descripcion, precio: Number(d.precio), stock: Number(d.stock),
      categoriaId: Number(d.categoriaId), unidadId: Number(d.unidadId), activo: !!d.activo };
    try {
      await api(id ? 'PUT' : 'POST', id ? '/api/mis-productos/' + id : '/api/mis-productos', cuerpo);
      aviso('Producto guardado.');
      location.hash = '#/productos';
    } catch (e) { aviso(e.message, 'error'); }
  };
}

async function vPedidos() {
  const pedidos = await api('GET', '/api/pedidos');
  const SIGUIENTE = { PENDIENTE: ['CONFIRMADO', 'Confirmar pedido'], CONFIRMADO: ['ENTREGADO', 'Marcar entregado'] };
  vista(`<div class="titulo-vista"><h1>Pedidos recibidos</h1></div>` + (pedidos.length === 0
    ? '<p class="vacio">Todavía no recibes pedidos.</p>'
    : pedidos.map(p => `
    <section class="pedido">
      <header><strong>Pedido #${p.id}</strong><span>${fechaHora(p.fecha)}</span>${estadoBadge(p.estado)}<span class="ms-auto fw-semibold">${soles(p.subtotal)}</span></header>
      <ul>${p.detalles.map(d => `<li>${d.cantidad} × ${esc(d.producto)} — ${soles(d.subtotal)}</li>`).join('')}</ul>
      <div class="pie">
        <span class="text-secondary me-auto">${esc(p.cliente)} · ${esc(p.direccionEntrega)}, ${esc(p.distrito)} · Tel. ${esc(p.telefono)}</span>
        ${SIGUIENTE[p.estado] ? `<button class="btn btn-primary btn-sm" data-accion="estado" data-id="${p.id}" data-estado="${SIGUIENTE[p.estado][0]}">${SIGUIENTE[p.estado][1]}</button>` : ''}
        ${p.estado === 'PENDIENTE' || p.estado === 'CONFIRMADO' ? `<button class="btn btn-outline-primary btn-sm" data-accion="estado" data-id="${p.id}" data-estado="CANCELADO">Cancelar</button>` : ''}
      </div>
    </section>`).join('')));
}

/* ---------- administrador ---------- */
async function vUsuarios() {
  const rol = vUsuarios.rol || '';
  const lista = await api('GET', '/api/usuarios' + (rol ? '?rol=' + rol : ''));
  vista(`
  <div class="titulo-vista"><h1>Usuarios</h1>
    <select id="f-rol" class="form-select" style="max-width:220px" aria-label="Filtrar por rol">
      ${[['', 'Todos'], ['CLIENTE', 'Clientes'], ['AGRICULTOR', 'Agricultores'], ['ADMINISTRADOR', 'Administradores']].map(([v, t]) => `<option value="${v}" ${v === rol ? 'selected' : ''}>${t}</option>`).join('')}</select></div>
  <div class="panel tabla-wrap"><table class="table align-middle mb-0">
    <thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Aprobado</th><th>Estado</th><th></th></tr></thead>
    <tbody>${lista.map(u => `<tr><td>${esc(u.nombre)}</td><td>${esc(u.email)}</td><td>${esc(u.rol.toLowerCase())}</td>
      <td>${u.rol === 'AGRICULTOR' ? `<span class="estado ${u.aprobado ? 'si' : 'no'}">${u.aprobado ? 'Aprobado' : 'Pendiente'}</span>` : '—'}</td>
      <td><span class="estado ${u.activo ? 'si' : 'no'}">${u.activo ? 'Activo' : 'Inactivo'}</span></td>
      <td class="text-end">${u.rol === 'AGRICULTOR' ? `<button class="btn btn-sm btn-outline-primary" data-accion="aprobar" data-id="${u.id}" data-valor="${!u.aprobado}">${u.aprobado ? 'Revocar' : 'Aprobar'}</button>` : ''}
        <button class="btn btn-sm btn-outline-primary" data-accion="activar" data-id="${u.id}" data-valor="${!u.activo}">${u.activo ? 'Desactivar' : 'Activar'}</button></td></tr>`).join('')}</tbody></table></div>`);
  $('#f-rol').onchange = ev => { vUsuarios.rol = ev.target.value; router(); };
}

const CFG_CATEGORIA = { titulo: 'Categorías', url: '/api/categorias', campos: [['nombre', 'Nombre', 'text', true], ['descripcion', 'Descripción', 'text', false]] };
const CFG_UNIDAD = { titulo: 'Unidades de medida', url: '/api/unidades', campos: [['nombre', 'Nombre', 'text', true], ['abreviatura', 'Abreviatura', 'text', true]] };
const CFG_DISTRITO = { titulo: 'Distritos', url: '/api/distritos', campos: [['nombre', 'Nombre', 'text', true]], borrar: true };
let mantEdicion = null;

async function vMantenimiento(cfg) {
  const lista = await api('GET', cfg.url);
  const e = mantEdicion && mantEdicion.url === cfg.url ? mantEdicion.fila : null;
  vista(`
  <div class="titulo-vista"><h1>${cfg.titulo}</h1></div>
  <form id="f-mant" class="panel row g-3 align-items-end">
    ${cfg.campos.map(([k, t, tipo, req]) => `<label class="col-md">${t}<input class="form-control" type="${tipo}" name="${k}" ${req ? 'required' : ''} value="${esc(e ? e[k] : '')}"></label>`).join('')}
    <div class="col-md-auto d-flex gap-2"><button class="btn btn-primary" type="submit">${e ? 'Guardar cambios' : 'Agregar'}</button>
      ${e ? '<button class="btn btn-outline-primary" type="button" data-accion="cancelar-edicion">Cancelar</button>' : ''}</div>
  </form>
  <div class="panel tabla-wrap"><table class="table align-middle mb-0">
    <thead><tr>${cfg.campos.map(c => `<th>${c[1]}</th>`).join('')}<th>Estado</th><th></th></tr></thead>
    <tbody>${lista.map(f => `<tr>${cfg.campos.map(c => `<td>${esc(f[c[0]])}</td>`).join('')}
      <td><span class="estado ${f.activo ? 'si' : 'no'}">${f.activo ? 'Activo' : 'Inactivo'}</span></td>
      <td class="text-end"><button class="btn btn-sm btn-outline-primary" data-accion="mant-editar" data-id="${f.id}">Editar</button>
        <button class="btn btn-sm btn-outline-primary" data-accion="mant-borrar" data-id="${f.id}">${cfg.borrar ? 'Eliminar' : 'Desactivar'}</button></td></tr>`).join('')}</tbody></table></div>`);
  mantEdicion = mantEdicion && mantEdicion.url === cfg.url ? { ...mantEdicion, lista, cfg } : { url: cfg.url, fila: null, lista, cfg };
  $('#f-mant').onsubmit = async ev => {
    ev.preventDefault();
    const d = Object.fromEntries(new FormData(ev.target));
    try {
      if (mantEdicion.fila) { await api('PUT', cfg.url + '/' + mantEdicion.fila.id, { ...d, activo: mantEdicion.fila.activo }); }
      else { await api('POST', cfg.url, d); }
      aviso('Guardado.'); mantEdicion = null; router();
    } catch (er) { aviso(er.message, 'error'); }
  };
}

const hoy = () => new Date().toISOString().slice(0, 10);
async function vReportes() {
  const f = vReportes.f || (vReportes.f = { desde: '', hasta: '', agricultorId: '', pagina: 0 });
  const qs = new URLSearchParams();
  if (f.desde) qs.set('desde', f.desde);
  if (f.hasta) qs.set('hasta', f.hasta);
  if (f.agricultorId) qs.set('agricultorId', f.agricultorId);
  const base = qs.toString();
  const [agr, res, prods, porAgr, ventas] = await Promise.all([
    api('GET', '/api/usuarios?rol=AGRICULTOR'), api('GET', '/api/reportes/resumen?' + base), api('GET', '/api/reportes/productos?' + base),
    api('GET', '/api/reportes/agricultores?' + base), api('GET', `/api/reportes/ventas?${base}&pagina=${f.pagina}&tamano=10`)]);
  vista(`
  <div class="titulo-vista"><h1>Reportes de ventas</h1></div>
  <form id="f-rep" class="filtros">
    <label>Desde<input class="form-control" type="date" name="desde" value="${esc(f.desde)}"></label>
    <label>Hasta<input class="form-control" type="date" name="hasta" value="${esc(f.hasta)}"></label>
    <label>Agricultor<select class="form-select" name="agricultorId"><option value="">Todos</option>${agr.map(a => `<option value="${a.id}" ${String(a.id) === String(f.agricultorId) ? 'selected' : ''}>${esc(a.nombre)}</option>`).join('')}</select></label>
    <button class="btn btn-outline-primary align-self-end" type="submit">Aplicar</button>
  </form>
  <div class="kpis">
    <div class="kpi"><b>${soles(res.total)}</b>Ventas</div><div class="kpi"><b>${res.pedidos}</b>Pedidos</div>
    <div class="kpi"><b>${res.unidades}</b>Unidades vendidas</div><div class="kpi"><b>${soles(res.ticketPromedio)}</b>Ticket promedio</div></div>
  <div class="panel d-flex flex-wrap gap-2 align-items-end">
    <label>Exportar<select id="x-tipo" class="form-select"><option value="ventas">Detalle de ventas</option><option value="productos">Productos más vendidos</option><option value="agricultores">Ventas por agricultor</option></select></label>
    <button class="btn btn-primary" data-accion="exportar" data-formato="pdf">PDF</button>
    <button class="btn btn-primary" data-accion="exportar" data-formato="xlsx">Excel</button>
    <button class="btn btn-primary" data-accion="exportar" data-formato="csv">CSV</button>
  </div>
  <div class="row g-3">
    <div class="col-lg-6"><div class="panel tabla-wrap"><h2 class="h5">Productos más vendidos</h2><table class="table mb-0"><thead><tr><th>Producto</th><th class="text-end">Unid.</th><th class="text-end">Total</th></tr></thead>
      <tbody>${prods.length ? prods.map(p => `<tr><td>${esc(p.producto)}</td><td class="text-end">${p.cantidad}</td><td class="text-end">${soles(p.total)}</td></tr>`).join('') : '<tr><td colspan="3" class="vacio">Sin ventas en este periodo</td></tr>'}</tbody></table></div></div>
    <div class="col-lg-6"><div class="panel tabla-wrap"><h2 class="h5">Ventas por agricultor</h2><table class="table mb-0"><thead><tr><th>Agricultor</th><th class="text-end">Pedidos</th><th class="text-end">Total</th></tr></thead>
      <tbody>${porAgr.length ? porAgr.map(a => `<tr><td>${esc(a.agricultor)}</td><td class="text-end">${a.pedidos}</td><td class="text-end">${soles(a.total)}</td></tr>`).join('') : '<tr><td colspan="3" class="vacio">Sin ventas en este periodo</td></tr>'}</tbody></table></div></div>
  </div>
  <div class="panel tabla-wrap"><h2 class="h5">Detalle de ventas</h2><table class="table mb-0">
    <thead><tr><th>Fecha</th><th>Pedido</th><th>Producto</th><th>Agricultor</th><th class="text-end">Cant.</th><th class="text-end">Subtotal</th><th>Estado</th></tr></thead>
    <tbody>${ventas.contenido.length ? ventas.contenido.map(v => `<tr><td>${fechaHora(v.fecha)}</td><td>#${v.pedidoId}</td><td>${esc(v.producto)}</td><td>${esc(v.agricultor)}</td><td class="text-end">${v.cantidad}</td><td class="text-end">${soles(v.subtotal)}</td><td>${estadoBadge(v.estado)}</td></tr>`).join('') : '<tr><td colspan="7" class="vacio">Sin ventas en este periodo</td></tr>'}</tbody></table>
    ${ventas.totalPaginas > 1 ? `<div class="d-flex gap-2 justify-content-center mt-3">
      <button class="btn btn-outline-primary btn-sm" data-accion="rep-pagina" data-p="${f.pagina - 1}" ${f.pagina === 0 ? 'disabled' : ''}>Anterior</button>
      <span class="align-self-center">Página ${ventas.pagina + 1} de ${ventas.totalPaginas}</span>
      <button class="btn btn-outline-primary btn-sm" data-accion="rep-pagina" data-p="${f.pagina + 1}" ${f.pagina + 1 >= ventas.totalPaginas ? 'disabled' : ''}>Siguiente</button></div>` : ''}</div>`);
  $('#f-rep').onsubmit = ev => { ev.preventDefault(); Object.assign(f, Object.fromEntries(new FormData(ev.target)), { pagina: 0 }); router(); };
}

/* ---------- acciones (delegación de eventos) ---------- */
const ACCIONES = {
  recargar: () => router(),
  async salir() {
    if (flujoAvisos) flujoAvisos.close();
    Sesion.borrar(); carritoCantidad = 0; location.hash = '#/'; router();
  },
  pagina: b => { filtroCatalogo.pagina = Number(b.dataset.p); router(); },
  async agregar(b) {
    const cantidad = Number($('#cant-' + b.dataset.id).value) || 1;
    await api('POST', '/api/carrito', { productoId: Number(b.dataset.id), cantidad });
    aviso('Agregado al carrito.'); refrescarCarrito();
  },
  async quitar(b) { await api('DELETE', '/api/carrito/' + b.dataset.id); router(); },
  async vaciar() { await api('DELETE', '/api/carrito'); router(); },
  async ocultar(b) { await api('DELETE', '/api/mis-productos/' + b.dataset.id); aviso('Producto oculto.'); router(); },
  async estado(b) {
    if (b.dataset.estado === 'CANCELADO' && !confirm('¿Cancelar este pedido? El stock volverá al catálogo.')) return;
    await api('PATCH', `/api/pedidos/${b.dataset.id}/estado`, { estado: b.dataset.estado }); router();
  },
  async aprobar(b) { await api('PATCH', `/api/usuarios/${b.dataset.id}/aprobacion`, { aprobado: b.dataset.valor === 'true' }); router(); },
  async activar(b) { await api('PATCH', `/api/usuarios/${b.dataset.id}/estado`, { activo: b.dataset.valor === 'true' }); router(); },
  'cancelar-edicion': () => { mantEdicion = null; router(); },
  'mant-editar'(b) { mantEdicion.fila = mantEdicion.lista.find(x => String(x.id) === b.dataset.id); router(); },
  async 'mant-borrar'(b) {
    if (!confirm('¿Seguro?')) return;
    await api('DELETE', mantEdicion.url + '/' + b.dataset.id); aviso('Listo.'); mantEdicion = null; router();
  },
  'rep-pagina': b => { vReportes.f.pagina = Number(b.dataset.p); router(); },
  async exportar(b) {
    const f = vReportes.f; const tipo = $('#x-tipo').value; const formato = b.dataset.formato;
    const qs = new URLSearchParams({ tipo, formato });
    if (f.desde) qs.set('desde', f.desde);
    if (f.hasta) qs.set('hasta', f.hasta);
    if (f.agricultorId) qs.set('agricultorId', f.agricultorId);
    await descargar('/api/reportes/exportar?' + qs, `reporte-${tipo}-${hoy()}.${formato}`);
    aviso('Reporte descargado.');
  }
};

document.addEventListener('click', async ev => {
  const el = ev.target.closest('[data-accion]');
  if (!el || el.tagName === 'INPUT') return;
  const fn = ACCIONES[el.dataset.accion];
  if (!fn) return;
  try { await fn(el); } catch (e) { aviso(e.message, 'error'); }
});
document.addEventListener('change', async ev => {
  const el = ev.target;
  if (el.dataset && el.dataset.accion === 'cantidad') {
    try { await api('PUT', '/api/carrito/' + el.dataset.id, { cantidad: Number(el.value) }); router(); }
    catch (e) { aviso(e.message, 'error'); router(); }
  }
});

window.addEventListener('hashchange', router);
conectarAvisos();
refrescarCarrito();
router();
