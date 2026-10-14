// ==========================================================================
// Prestamos: listado paginado, registro, devolucion y renovacion.
// ==========================================================================

let tablaPrestamos = null;

function cargarPrestamos() {
    if (!tablaPrestamos) {
        tablaPrestamos = new TablaPaginada({
            tbody: 'tabla-prestamos',
            vacio: 'vacio-prestamos',
            paginacion: 'pag-prestamos',
            buscador: 'buscar-prestamos',
            colspan: 9,
            size: 10,
            filtros: [{ id: 'filtro-estado-prestamo', param: 'estado' }],
            cargar: (params) => apiRequest('/prestamos/buscar?' + params.toString()),
            render: (p) => filaPrestamo(p),
        });
    }
    tablaPrestamos.cargar(0);
}

function filaPrestamo(p) {
    const ins = insigniaPrestamo(p);
    const activo = p.estado !== 'DEVUELTO';
    const botones = [];
    if (activo) {
        botones.push('<button class="boton boton-primario boton-mini" onclick="devolverPrestamo(' + p.id + ')">' + icono('devolver') + '<span>Devolver</span></button>');
        botones.push('<button class="boton boton-secundario boton-mini" onclick="renovarPrestamo(' + p.id + ')">' + icono('renovar') + '<span>Renovar</span></button>');
    }
    botones.push('<button class="boton boton-secundario boton-mini" onclick="editarPrestamo(' + p.id + ')">' + icono('editar') + '<span>Editar</span></button>');
    botones.push('<button class="boton boton-peligro boton-mini" onclick="eliminarPrestamo(' + p.id + ')">' + icono('eliminar') + '<span>Eliminar</span></button>');

    const retraso = p.vencido && p.diasRetraso > 0 ? '<div class="secundario">+' + p.diasRetraso + ' días</div>' : '';

    return (
        '<td data-label="ID">' + p.id + '</td>' +
        '<td data-label="Lector"><strong>' + esc(p.usuarioNombre) + '</strong>' + (p.usuarioCodigo ? '<div class="secundario">' + esc(p.usuarioCodigo) + '</div>' : '') + '</td>' +
        '<td data-label="Libro">' + esc(p.libroTitulo) + '</td>' +
        '<td data-label="Ejemplar">' + esc(p.ejemplarCodigo || '-') + '</td>' +
        '<td data-label="Préstamo">' + esc(formatearFecha(p.fechaPrestamo)) + '</td>' +
        '<td data-label="Límite">' + esc(formatearFecha(p.fechaLimite)) + retraso + '</td>' +
        '<td data-label="Estado"><span class="insignia ' + ins.clase + '">' + ins.texto + '</span></td>' +
        '<td data-label="Renov.">' + (p.renovaciones || 0) + '</td>' +
        '<td data-label="Acciones" class="acciones-celda"><div class="acciones">' + botones.join('') + '</div></td>'
    );
}

async function cargarSelectoresPrestamo() {
    const [lectoresResp, librosResp] = await Promise.all([
        apiRequest('/lectores?size=200'),
        apiRequest('/libros'),
    ]);
    const lectores = (lectoresResp.data && lectoresResp.data.contenido) || [];
    const libros = librosResp.data || [];

    llenarSelect('prestamo-lector', lectores, {
        placeholder: '-- Seleccionar lector --',
        valor: (l) => l.id,
        etiqueta: (l) => (l.codigoLector ? l.codigoLector + ' · ' : '') + l.nombre + ' ' + (l.apellido || ''),
        deshabilitar: (l) => !l.activo,
    });

    llenarSelect('prestamo-libro', libros, {
        placeholder: '-- Seleccionar libro --',
        valor: (l) => l.id,
        etiqueta: (l) => l.titulo + ' (disponibles: ' + l.disponibles + ')',
        deshabilitar: (l) => l.disponibles <= 0,
    });
}

function abrirModalPrestamo(id) {
    const form = document.getElementById('form-prestamo');
    form.reset();
    const hoy = new Date();
    document.getElementById('prestamo-fecha').value = hoy.toISOString().split('T')[0];
    const limite = new Date(hoy);
    limite.setDate(limite.getDate() + 15);
    document.getElementById('prestamo-limite').value = limite.toISOString().split('T')[0];
    document.getElementById('prestamo-observaciones').value = '';

    cargarSelectoresPrestamo()
        .then(() => {
            document.getElementById('titulo-modal-prestamo').textContent = id ? 'Editar préstamo' : 'Nuevo préstamo';
            if (id) {
                editarPrestamo(id);
            } else {
                document.getElementById('prestamo-id').value = '';
                abrirModal('modal-prestamo');
            }
        })
        .catch((err) => mostrarToast(err.message, 'error'));
}

async function editarPrestamo(id) {
    try {
        const resp = await apiRequest('/prestamos/' + id);
        const p = resp.data;
        document.getElementById('prestamo-id').value = p.id;
        document.getElementById('prestamo-lector').value = p.usuarioId;
        document.getElementById('prestamo-libro').value = p.libroId;
        document.getElementById('prestamo-fecha').value = p.fechaPrestamo || '';
        document.getElementById('prestamo-limite').value = p.fechaLimite || '';
        document.getElementById('prestamo-observaciones').value = p.observaciones || '';
        document.getElementById('titulo-modal-prestamo').textContent = 'Editar préstamo';
        abrirModal('modal-prestamo');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarPrestamo(id) {
    const ok = await confirmar('¿Eliminar este registro de préstamo?', { titulo: 'Eliminar préstamo', textoBoton: 'Eliminar', peligro: true });
    if (!ok) return;
    try {
        await apiRequest('/prestamos/' + id, 'DELETE');
        mostrarToast('Préstamo eliminado');
        tablaPrestamos.cargar(tablaPrestamos.page);
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function devolverPrestamo(id) {
    const ok = await confirmar('¿Registrar la devolución de este préstamo?', { titulo: 'Devolver préstamo', textoBoton: 'Devolver' });
    if (!ok) return;
    try {
        await apiRequest('/prestamos/' + id + '/devolver', 'PUT');
        mostrarToast('Préstamo devuelto');
        tablaPrestamos.cargar(tablaPrestamos.page);
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function renovarPrestamo(id) {
    try {
        await apiRequest('/prestamos/' + id + '/renovar', 'PUT');
        mostrarToast('Préstamo renovado');
        tablaPrestamos.cargar(tablaPrestamos.page);
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

function cargarMisPrestamos() {
    const tbody = document.getElementById('tabla-misprestamos');
    tbody.innerHTML = filaCarga(6);
    apiRequest('/prestamos/mios')
        .then((resp) => {
            const lista = resp.data || [];
            tbody.innerHTML = '';
            document.getElementById('vacio-misprestamos').style.display = lista.length ? 'none' : 'block';
            lista.forEach((p) => {
                const ins = insigniaPrestamo(p);
                const tr = document.createElement('tr');
                tr.innerHTML =
                    '<td data-label="Libro">' + esc(p.libroTitulo) + '</td>' +
                    '<td data-label="Ejemplar">' + esc(p.ejemplarCodigo || '-') + '</td>' +
                    '<td data-label="Préstamo">' + esc(formatearFecha(p.fechaPrestamo)) + '</td>' +
                    '<td data-label="Límite">' + esc(formatearFecha(p.fechaLimite)) + '</td>' +
                    '<td data-label="Devolución">' + esc(p.fechaDevolucion ? formatearFecha(p.fechaDevolucion) : '-') + '</td>' +
                    '<td data-label="Estado"><span class="insignia ' + ins.clase + '">' + ins.texto + '</span></td>';
                tbody.appendChild(tr);
            });
        })
        .catch((err) => {
            mostrarToast(err.message, 'error');
            tbody.innerHTML = '';
        });
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-prestamo');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('prestamo-id').value;
        const data = {
            usuarioId: parseInt(document.getElementById('prestamo-lector').value, 10) || null,
            libroId: parseInt(document.getElementById('prestamo-libro').value, 10) || null,
            fechaPrestamo: document.getElementById('prestamo-fecha').value || null,
            fechaLimite: document.getElementById('prestamo-limite').value || null,
            observaciones: document.getElementById('prestamo-observaciones').value.trim(),
        };
        if (!data.usuarioId || !data.libroId) {
            mostrarToast('Debes seleccionar un lector y un libro.', 'error');
            return;
        }
        try {
            if (id) {
                await apiRequest('/prestamos/' + id, 'PUT', data);
                mostrarToast('Préstamo actualizado');
            } else {
                await apiRequest('/prestamos', 'POST', data);
                mostrarToast('Préstamo registrado');
            }
            cerrarModal('modal-prestamo');
            cargarPrestamos();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});
