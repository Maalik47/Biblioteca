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

    apiRequest('/auth/me')
        .then(() => {})
        .catch(() => {
            // apiRequest ya redirige si el token caducó (401)
        });

    const items = document.querySelectorAll('.item-menu');
    items.forEach((item) => {
        item.addEventListener('click', () => cambiarSeccion(item.dataset.seccion));
    });

    document.getElementById('btn-cerrar-sesion').addEventListener('click', () => {
        cerrarSesion();
        window.location.href = 'index.html';
    });

    // Cerrar modal con Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const abierto = document.querySelector('.modal.abierto');
            if (abierto) cerrarModal(abierto.id);
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

    // Cerrar modal al hacer clic fuera de la caja
    document.querySelectorAll('.modal').forEach((mod) => {
        mod.addEventListener('mousedown', (e) => {
            if (e.target === mod) cerrarModal(mod.id);
        });
    });

    cambiarSeccion('usuarios');
});

let focoAnterior = null;

function cambiarSeccion(seccion) {
    document.querySelectorAll('.seccion').forEach((s) => s.classList.remove('activa'));
    document.querySelectorAll('.item-menu').forEach((b) => {
        const activo = b.dataset.seccion === seccion;
        b.classList.toggle('activo', activo);
        if (activo) b.setAttribute('aria-current', 'true');
        else b.removeAttribute('aria-current');
    });
    document.getElementById('seccion-' + seccion).classList.add('activa');

    if (seccion === 'usuarios' && typeof cargarUsuarios === 'function') cargarUsuarios();
    if (seccion === 'libros' && typeof cargarLibros === 'function') cargarLibros();
    if (seccion === 'prestamos' && typeof cargarPrestamos === 'function') cargarPrestamos();
}

function abrirModal(id) {
    const modal = document.getElementById(id);
    if (modal.classList.contains('abierto')) return;
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