// ==========================================================================
// Reservas: libros sin ejemplares disponibles.
// ==========================================================================

function cargarReservas() {
    const tbody = document.getElementById('tabla-reservas');
    tbody.innerHTML = filaCarga(8);
    document.getElementById('vacio-reservas').style.display = 'none';
    const estado = document.getElementById('filtro-estado-reserva').value;
    const qs = estado ? '?estado=' + encodeURIComponent(estado) : '';
    apiRequest('/reservas' + qs)
        .then((resp) => {
            const lista = resp.data || [];
            tbody.innerHTML = '';
            document.getElementById('vacio-reservas').style.display = lista.length ? 'none' : 'block';
            lista.forEach((r) => tbody.appendChild(filaReserva(r)));
        })
        .catch((err) => {
            mostrarToast(err.message, 'error');
            tbody.innerHTML = '';
        });
}

function filaReserva(r) {
    const ins = insigniaReserva(r);
    const finalizada = r.estado === 'CUMPLIDA' || r.estado === 'CANCELADA';
    const botones = [];
    if (!finalizada) {
        botones.push('<button class="boton boton-primario boton-mini" onclick="cumplirReserva(' + r.id + ')">' + icono('check') + '<span>Convertir en préstamo</span></button>');
        botones.push('<button class="boton boton-peligro boton-mini" onclick="cancelarReserva(' + r.id + ')">' + icono('equis') + '<span>Cancelar</span></button>');
    }

    const tr = document.createElement('tr');
    tr.innerHTML =
        '<td data-label="ID">' + r.id + '</td>' +
        '<td data-label="Lector"><strong>' + esc(r.usuarioNombre) + '</strong>' + (r.usuarioCodigo ? '<div class="secundario">' + esc(r.usuarioCodigo) + '</div>' : '') + '</td>' +
        '<td data-label="Libro">' + esc(r.libroTitulo) + '</td>' +
        '<td data-label="Solicitada">' + esc(formatearFechaHora(r.fechaSolicitud)) + '</td>' +
        '<td data-label="Expira">' + esc(r.fechaExpiracion ? formatearFecha(r.fechaExpiracion) : '-') + '</td>' +
        '<td data-label="Posición">' + (r.posicion || '-') + '</td>' +
        '<td data-label="Estado"><span class="insignia ' + ins.clase + '">' + ins.texto + '</span></td>' +
        '<td data-label="Acciones" class="acciones-celda"><div class="acciones">' + (botones.join('') || '—') + '</div></td>';
    return tr;
}

async function cargarSelectoresReserva() {
    const [lectoresResp, librosResp] = await Promise.all([
        esStaff() ? apiRequest('/lectores?size=200') : Promise.resolve({ data: { contenido: [] } }),
        apiRequest('/libros'),
    ]);
    const lectores = (lectoresResp.data && lectoresResp.data.contenido) || [];
    const libros = librosResp.data || [];

    llenarSelect('reserva-lector', lectores, {
        placeholder: '-- Seleccionar lector --',
        valor: (l) => l.id,
        etiqueta: (l) => (l.codigoLector ? l.codigoLector + ' · ' : '') + l.nombre + ' ' + (l.apellido || ''),
        deshabilitar: (l) => !l.activo,
    });

    llenarSelect('reserva-libro', libros, {
        placeholder: '-- Seleccionar libro --',
        valor: (l) => l.id,
        etiqueta: (l) => l.titulo + ' (' + l.disponibles + ' disponibles)',
    });
}

function abrirModalReserva() {
    document.getElementById('form-reserva').reset();
    const campoLector = document.getElementById('campo-reserva-lector');
    campoLector.classList.toggle('oculto', !esStaff());
    cargarSelectoresReserva()
        .then(() => abrirModal('modal-reserva'))
        .catch((err) => mostrarToast(err.message, 'error'));
}

async function cancelarReserva(id) {
    const ok = await confirmar('¿Cancelar esta reserva?', { titulo: 'Cancelar reserva', textoBoton: 'Cancelar reserva', peligro: true });
    if (!ok) return;
    try {
        await apiRequest('/reservas/' + id + '/cancelar', 'PUT');
        mostrarToast('Reserva cancelada');
        cargarReservas();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function cumplirReserva(id) {
    try {
        await apiRequest('/reservas/' + id + '/cumplir', 'PUT');
        mostrarToast('Reserva convertida en préstamo');
        cargarReservas();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

function cargarMisReservas() {
    const tbody = document.getElementById('tabla-misreservas');
    tbody.innerHTML = filaCarga(6);
    apiRequest('/reservas/mias')
        .then((resp) => {
            const lista = resp.data || [];
            tbody.innerHTML = '';
            document.getElementById('vacio-misreservas').style.display = lista.length ? 'none' : 'block';
            lista.forEach((r) => {
                const ins = insigniaReserva(r);
                const finalizada = r.estado === 'CUMPLIDA' || r.estado === 'CANCELADA';
                const tr = document.createElement('tr');
                tr.innerHTML =
                    '<td data-label="Libro">' + esc(r.libroTitulo) + '</td>' +
                    '<td data-label="Solicitada">' + esc(formatearFechaHora(r.fechaSolicitud)) + '</td>' +
                    '<td data-label="Expira">' + esc(r.fechaExpiracion ? formatearFecha(r.fechaExpiracion) : '-') + '</td>' +
                    '<td data-label="Posición">' + (r.posicion || '-') + '</td>' +
                    '<td data-label="Estado"><span class="insignia ' + ins.clase + '">' + ins.texto + '</span></td>' +
                    '<td data-label="Acciones" class="acciones-celda">' +
                    (finalizada ? '—' : '<button class="boton boton-peligro boton-mini" onclick="cancelarMiReserva(' + r.id + ')">' + icono('equis') + '<span>Cancelar</span></button>') +
                    '</td>';
                tbody.appendChild(tr);
            });
        })
        .catch((err) => {
            mostrarToast(err.message, 'error');
            tbody.innerHTML = '';
        });
}

async function cancelarMiReserva(id) {
    const ok = await confirmar('¿Cancelar esta reserva?', { titulo: 'Cancelar reserva', textoBoton: 'Cancelar reserva', peligro: true });
    if (!ok) return;
    try {
        await apiRequest('/reservas/' + id + '/cancelar', 'PUT');
        mostrarToast('Reserva cancelada');
        cargarMisReservas();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const sel = document.getElementById('filtro-estado-reserva');
    if (sel) sel.addEventListener('change', cargarReservas);

    const form = document.getElementById('form-reserva');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const libroId = parseInt(document.getElementById('reserva-libro').value, 10) || null;
        const usuarioId = esStaff() ? parseInt(document.getElementById('reserva-lector').value, 10) || null : null;
        if (!libroId) {
            mostrarToast('Debes seleccionar un libro.', 'error');
            return;
        }
        if (esStaff() && !usuarioId) {
            mostrarToast('Debes seleccionar un lector.', 'error');
            return;
        }
        try {
            await apiRequest('/reservas', 'POST', { libroId: libroId, usuarioId: usuarioId });
            mostrarToast('Reserva registrada');
            cerrarModal('modal-reserva');
            if (esStaff()) cargarReservas();
            else cargarMisReservas();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});
