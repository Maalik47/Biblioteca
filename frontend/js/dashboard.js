// ==========================================================================
// Dashboard: resumen real calculado por el backend (/api/dashboard).
// ==========================================================================

async function cargarDashboard() {
    const stats = document.getElementById('stats-dashboard');
    if (!stats) return;
    stats.innerHTML = tarjetasCarga(4);
    try {
        const resp = await apiRequest('/dashboard');
        const d = resp.data || {};
        renderStatsDashboard(d);
        renderBarras('barras-categorias', d.librosPorCategoria || []);
        renderActividad(d.ultimaActividad || []);
        renderUltimosPrestamos(d.ultimosPrestamos || []);
    } catch (err) {
        mostrarToast(err.message, 'error');
        stats.innerHTML = '';
    }
}

function tarjetasCarga(n) {
    let html = '';
    for (let i = 0; i < n; i++) {
        html += '<div class="tarjeta-stat"><div class="stat-icono"></div><div><div class="valor">&hellip;</div><div class="etiqueta">&nbsp;</div></div></div>';
    }
    return html;
}

function tarjetaStat(valor, etiqueta, iconoNombre, variante) {
    return (
        '<div class="tarjeta-stat ' + (variante || '') + '">' +
        '<div class="stat-icono" aria-hidden="true">' + icono(iconoNombre) + '</div>' +
        '<div><div class="valor">' + esc(valor) + '</div>' +
        '<div class="etiqueta">' + esc(etiqueta) + '</div></div>' +
        '</div>'
    );
}

function renderStatsDashboard(d) {
    const stats = document.getElementById('stats-dashboard');
    stats.innerHTML =
        tarjetaStat(d.totalLibros || 0, 'Títulos en catálogo', 'libro') +
        tarjetaStat((d.ejemplaresDisponibles || 0) + ' / ' + (d.totalEjemplares || 0), 'Ejemplares disponibles', 'circulo') +
        tarjetaStat(d.totalLectores || 0, 'Lectores registrados', 'lector') +
        tarjetaStat(d.prestamosActivos || 0, 'Préstamos activos', 'prestamo', 'aviso') +
        tarjetaStat(d.prestamosVencidos || 0, 'Préstamos vencidos', 'reloj', 'alerta') +
        tarjetaStat(d.reservasPendientes || 0, 'Reservas pendientes', 'reserva');
}

function renderBarras(id, lista) {
    const cont = document.getElementById(id);
    if (!cont) return;
    if (!lista.length) {
        cont.innerHTML = '<p class="texto-tenue">Sin datos para mostrar.</p>';
        return;
    }
    const max = Math.max.apply(null, lista.map((x) => x.valor)) || 1;
    cont.innerHTML = lista
        .map((x) => {
            const pct = Math.round((x.valor / max) * 100);
            return (
                '<div class="barra-fila">' +
                '<span title="' + esc(x.nombre) + '">' + esc(x.nombre) + '</span>' +
                '<div class="barra-pista"><div class="barra-relleno" style="width:' + pct + '%"></div></div>' +
                '<span>' + esc(x.valor) + '</span>' +
                '</div>'
            );
        })
        .join('');
}

function renderActividad(lista) {
    const cont = document.getElementById('lista-actividad');
    if (!cont) return;
    if (!lista.length) {
        cont.innerHTML = '<li class="texto-tenue">Sin actividad reciente.</li>';
        return;
    }
    cont.innerHTML = lista
        .map(
            (a) =>
                '<li class="actividad-item">' +
                '<span class="accion-chip">' + esc(a.accion || '-') + '</span>' +
                '<span class="detalle">' + esc(a.detalle || a.entidad || '') + '</span>' +
                '<span class="fecha">' + esc(formatearFechaHora(a.fecha)) + '</span>' +
                '</li>'
        )
        .join('');
}

function renderUltimosPrestamos(lista) {
    const tbody = document.getElementById('tabla-ultimos-prestamos');
    if (!tbody) return;
    tbody.innerHTML = '';
    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="texto-tenue">Sin préstamos registrados.</td></tr>';
        return;
    }
    lista.forEach((p) => {
        const ins = insigniaPrestamo(p);
        const tr = document.createElement('tr');
        tr.innerHTML =
            '<td data-label="Lector">' + esc(p.usuarioNombre) + '</td>' +
            '<td data-label="Libro">' + esc(p.libroTitulo) + '</td>' +
            '<td data-label="Préstamo">' + esc(formatearFecha(p.fechaPrestamo)) + '</td>' +
            '<td data-label="Límite">' + esc(formatearFecha(p.fechaLimite)) + '</td>' +
            '<td data-label="Estado"><span class="insignia ' + ins.clase + '">' + ins.texto + '</span></td>';
        tbody.appendChild(tr);
    });
}
