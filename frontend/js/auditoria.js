// ==========================================================================
// Auditoria: historial paginado de acciones del sistema.
// ==========================================================================

let tablaAuditoria = null;

function cargarAuditoria() {
    if (!tablaAuditoria) {
        tablaAuditoria = new TablaPaginada({
            tbody: 'tabla-auditoria',
            vacio: 'vacio-auditoria',
            paginacion: 'pag-auditoria',
            buscador: 'buscar-auditoria',
            colspan: 6,
            size: 10,
            filtros: [{ id: 'filtro-accion-auditoria', param: 'accion' }],
            cargar: (params) => apiRequest('/auditoria?' + params.toString()),
            render: (a) => filaAuditoria(a),
        });
    }
    tablaAuditoria.cargar(0);
}

function filaAuditoria(a) {
    return (
        '<td data-label="Fecha">' + esc(formatearFechaHora(a.fecha)) + '</td>' +
        '<td data-label="Usuario">' + esc(a.usuario || 'sistema') + '</td>' +
        '<td data-label="Acción"><span class="accion-chip">' + esc(a.accion || '-') + '</span></td>' +
        '<td data-label="Entidad">' + esc(a.entidad || '-') + '</td>' +
        '<td data-label="ID">' + esc(a.entidadId !== null && a.entidadId !== undefined ? a.entidadId : '-') + '</td>' +
        '<td data-label="Detalle"><div class="secundario">' + esc(a.detalle || '-') + '</div></td>'
    );
}
