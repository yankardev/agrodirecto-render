/* Sesión y cliente HTTP. Todas las llamadas pasan por el API Gateway (mismo origen). */
const Sesion = {
  leer() { try { return JSON.parse(localStorage.getItem('agro.sesion')); } catch { return null; } },
  guardar(s) { localStorage.setItem('agro.sesion', JSON.stringify(s)); },
  borrar() { localStorage.removeItem('agro.sesion'); }
};

async function api(metodo, url, cuerpo) {
  const s = Sesion.leer();
  const cabeceras = {};
  if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';
  if (s) cabeceras['Authorization'] = 'Bearer ' + s.token;
  let resp;
  try {
    resp = await fetch(url, { method: metodo, headers: cabeceras, body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined });
  } catch {
    throw new Error('No hay conexión con el servidor. Revisa que los servicios estén encendidos.');
  }
  if (resp.status === 401 && s) {
    Sesion.borrar();
    location.hash = '#/login';
    throw new Error('Tu sesión expiró. Inicia sesión de nuevo.');
  }
  if (!resp.ok) {
    let mensaje = 'Ocurrió un error inesperado. Intenta de nuevo.';
    try { const j = await resp.json(); if (j.message) mensaje = j.message; } catch { /* sin cuerpo */ }
    throw new Error(mensaje);
  }
  if (resp.status === 204) return null;
  try { return await resp.json(); } catch { return null; }
}

async function descargar(url, nombreSugerido) {
  const s = Sesion.leer();
  const resp = await fetch(url, { headers: s ? { Authorization: 'Bearer ' + s.token } : {} });
  if (!resp.ok) {
    let mensaje = 'No se pudo generar el archivo.';
    try { const j = await resp.json(); if (j.message) mensaje = j.message; } catch { /* sin cuerpo */ }
    throw new Error(mensaje);
  }
  const blob = await resp.blob();
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = nombreSugerido;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(a.href), 2000);
}
