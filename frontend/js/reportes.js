// ==========================================================================
// Reportes: resumen estadistico y exportes CSV.
// ==========================================================================

async function cargarReportes() {
    const stats = document.getElementById('reportes-stats');
    if (!stats) return;
    stats.innerHTML = tarjetasCarga(2);
    try {
        const resp = await apiRequest('/reportes/resumen');
        const d = resp.data || {};
        const estados = d.prestamosPorEstado || {};
        stats.innerHTML =
            tarjetaStat(estados.activos || 0, 'Préstamos activos', 'prestamo', 'aviso') +
            tarjetaStat(estados.devueltos || 0, 'Préstamos devueltos', 'check');

        renderRanking(d.librosMasPrestados || []);
        renderBarras('barras-reporte', d.librosPorCategoria || []);
    } catch (err) {
        mostrarToast(err.message, 'error');
        stats.innerHTML = '';
    }
}

function renderRanking(lista) {
    const tbody = document.getElementById('tabla-ranking');
    tbody.innerHTML = '';
    document.getElementById('vacio-ranking').style.display = lista.length ? 'none' : 'block';
    lista.forEach((item, i) => {
        const tr = document.createElement('tr');
        tr.innerHTML =
            '<td data-label="#">' + (i + 1) + '</td>' +
            '<td data-label="Libro"><strong>' + esc(item.nombre) + '</strong></td>' +
            '<td data-label="Préstamos"><span class="insignia insignia-indigo">' + item.valor + '</span></td>';
        tbody.appendChild(tr);
    });
}
