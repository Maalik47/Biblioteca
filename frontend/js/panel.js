// ==========================================================================
// Orquestador del panel: sesion, navegacion por rol y carga de secciones.
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken()) {
        window.location.href = 'index.html';
        return;
    }

    const usuario = getUsuario();
    if (usuario) {
        document.getElementById('sesion-nombre').textContent = usuario.nombre || usuario.username;
        document.getElementById('sesion-rol').textContent = usuario.rol || '';
        const inicial = (usuario.nombre || usuario.username || '?').trim().charAt(0).toUpperCase();
        document.getElementById('usuario-inicial').textContent = inicial || '?';
    }

    inyectarIconos();
    configurarSegunRol();
    wireEventosGlobales();

    apiRequest('/auth/me').catch(() => {
        // apiRequest ya redirige si el token caduco (401)
    });

    const inicial = esStaff() ? 'dashboard' : 'catalogo';
    cambiarSeccion(inicial);
});

function rolActual() {
    const u = getUsuario();
    return (u && u.rol) || 'USUARIO';
}

function esStaff() {
    const rol = rolActual();
    return rol === 'ADMIN' || rol === 'BIBLIOTECARIO';
}

function esAdmin() {
    return rolActual() === 'ADMIN';
}

function inyectarIconos() {
    document.querySelectorAll('[data-icono]').forEach((el) => {
        const nombre = el.getAttribute('data-icono');
        const svg = icono(nombre);
        if (svg) el.innerHTML = svg;
    });
    // El titulo de navegacion usa spans con data-icono; los items-meni ya contienen svg.
    document.querySelectorAll('.item-menu [data-icono]').forEach((el) => {
        el.setAttribute('aria-hidden', 'true');
    });
}

function configurarSegunRol() {
    const rol = rolActual();
    document.querySelectorAll('.item-menu').forEach((item) => {
        const roles = (item.dataset.roles || '').split(',');
        if (!roles.includes(rol)) item.classList.add('oculto');
    });
    if (!esStaff()) {
        document.querySelectorAll('.solo-staff').forEach((el) => el.classList.add('oculto'));
    }
}

function wireEventosGlobales() {
    document.querySelectorAll('.item-menu').forEach((item) => {
        item.addEventListener('click', () => cambiarSeccion(item.dataset.seccion));
    });

    document.getElementById('btn-cerrar-sesion').addEventListener('click', () => {
        cerrarSesion();
        window.location.href = 'index.html';
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const abierto = document.querySelector('.modal.abierto');
            if (abierto) {
                if (abierto.id === 'modal-confirmar') resolverConfirmacion(false);
                else cerrarModal(abierto.id);
            }
            return;
        }
        if (e.key === 'Tab') {
            const abierto = document.querySelector('.modal.abierto');
            if (!abierto) return;
            const enfocables = [...abierto.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])')]
                .filter((el) => el.offsetParent !== null && !el.disabled);
            if (!enfocables.length) return;
            const primero = enfocables[0];
            const ultimo = enfocables[enfocables.length - 1];
            if (e.shiftKey && document.activeElement === primero) {
                e.preventDefault();
                ultimo.focus();
            } else if (!e.shiftKey && document.activeElement === ultimo) {
                e.preventDefault();
                primero.focus();
            }
        }
    });

    document.querySelectorAll('.modal').forEach((mod) => {
        mod.addEventListener('mousedown', (e) => {
            if (e.target !== mod) return;
            if (mod.id === 'modal-confirmar') resolverConfirmacion(false);
            else cerrarModal(mod.id);
        });
    });
}

const CARGADORES = {
    dashboard: 'cargarDashboard',
    catalogo: 'cargarLibros',
    lectores: 'cargarLectores',
    prestamos: 'cargarPrestamos',
    reservas: 'cargarReservas',
    reportes: 'cargarReportes',
    auditoria: 'cargarAuditoria',
    usuarios: 'cargarUsuarios',
    misprestamos: 'cargarMisPrestamos',
    misreservas: 'cargarMisReservas',
};

function cambiarSeccion(seccion) {
    document.querySelectorAll('.seccion').forEach((s) => s.classList.remove('activa'));
    document.querySelectorAll('.item-menu').forEach((b) => {
        const activo = b.dataset.seccion === seccion;
        b.classList.toggle('activo', activo);
        if (activo) b.setAttribute('aria-current', 'true');
        else b.removeAttribute('aria-current');
    });
    const destino = document.getElementById('seccion-' + seccion);
    if (destino) destino.classList.add('activa');

    const fn = CARGADORES[seccion];
    if (fn && typeof window[fn] === 'function') window[fn]();
}

// --- Modales (compartidos por todos los modulos) ---
let focoAnterior = null;

function abrirModal(id) {
    const modal = document.getElementById(id);
    if (!modal || modal.classList.contains('abierto')) return;
    if (focoAnterior === null) focoAnterior = document.activeElement;
    modal.classList.add('abierto');
    document.body.classList.add('sin-scroll');
    const primerCampo = modal.querySelector('input:not([type="hidden"]), select, textarea') || modal.querySelector('button');
    if (primerCampo) primerCampo.focus();
}

function cerrarModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('abierto');
    if (!document.querySelector('.modal.abierto')) {
        document.body.classList.remove('sin-scroll');
        if (focoAnterior && focoAnterior.focus) focoAnterior.focus();
        focoAnterior = null;
    }
}

function formatearFecha(iso) {
    if (!iso) return '';
    const f = new Date(iso);
    if (isNaN(f.getTime())) return iso;
    return f.toLocaleDateString('es-MX');
}

function formatearFechaHora(iso) {
    if (!iso) return '';
    const f = new Date(iso);
    if (isNaN(f.getTime())) return iso;
    return f.toLocaleString('es-MX', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}
