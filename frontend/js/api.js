// URL de la API: se define en config.js (window.__API_URL__).
// Si no esta, se usa el backend local.
const API_URL = window.__API_URL__ || 'http://localhost:8080/api';

function getToken() {
    return localStorage.getItem('token');
}

function getUsuario() {
    try {
        return JSON.parse(localStorage.getItem('usuario') || 'null');
    } catch (e) {
        return null;
    }
}

function cerrarSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
}

async function apiRequest(path, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    let res;
    try {
        res = await fetch(API_URL + path, {
            method,
            headers,
            body: body !== null ? JSON.stringify(body) : undefined,
        });
    } catch (e) {
        throw new Error('No se pudo conectar con el servidor.');
    }

    let data = null;
    try {
        data = await res.json();
    } catch (e) {
        // sin cuerpo en la respuesta
    }

    if (res.status === 401) {
        cerrarSesion();
        if (!window.location.pathname.endsWith('index.html')) {
            window.location.href = 'index.html';
        }
        throw new Error((data && data.message) || 'No autorizado');
    }

    if (!res.ok) {
        throw new Error((data && data.message) || 'Error del servidor (' + res.status + ')');
    }

    return data;
}

function icono(nombre) {
    const iconos = {
        plus: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
        editar: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.83 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/></svg>',
        eliminar: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>',
        devolver: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>',
        check: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>',
        equis: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
        alerta: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    };
    return iconos[nombre] || '';
}

function spinnerBoton() {
    return '<svg class="icono-spin" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><path d="M12 2a10 10 0 0 1 10 10"/></svg>';
}

function filaCarga(colspan) {
    return '<tr class="cargando-row"><td colspan="' + colspan + '" role="status"><div class="cargando-fila"><span class="spinner"></span> Cargando...</div></td></tr>';
}

function mostrarToast(mensaje, tipo) {
    const contenedor = document.getElementById('toast-cont');
    if (!contenedor) return;
    if (!contenedor.getAttribute('aria-live')) contenedor.setAttribute('aria-live', 'polite');
    const esError = tipo === 'error';
    const toast = document.createElement('div');
    toast.className = 'toast ' + (esError ? 'toast-error' : 'toast-exito');
    toast.setAttribute('role', esError ? 'alert' : 'status');
    toast.innerHTML = (esError ? icono('equis') : icono('check')) + '<span></span>';
    toast.querySelector('span').textContent = mensaje;
    contenedor.appendChild(toast);
    setTimeout(() => toast.remove(), 3800);
}