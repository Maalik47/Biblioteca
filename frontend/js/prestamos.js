async function cargarPrestamos() {
    const tbody = document.getElementById('tabla-prestamos');
    tbody.innerHTML = filaCarga(8);
    document.getElementById('vacio-prestamos').style.display = 'none';
    try {
        const [resp, usuariosResp, librosResp] = await Promise.all([
            apiRequest('/prestamos'),
            apiRequest('/usuarios'),
            apiRequest('/libros'),
        ]);
        llenarSelectores(usuariosResp.data || [], librosResp.data || []);
        renderPrestamos(resp.data || []);
    } catch (err) {
        mostrarToast(err.message, 'error');
        tbody.innerHTML = '';
    }
}

function llenarSelectores(usuarios, libros) {
    const selUsuario = document.getElementById('prestamo-usuario');
    selUsuario.innerHTML = '<option value="">-- Seleccionar usuario --</option>';
    usuarios
        .filter((u) => u.activo)
        .forEach((u) => {
            const op = document.createElement('option');
            op.value = u.id;
            op.textContent = u.username + ' (' + u.nombre + ' ' + (u.apellido || '') + ')';
            selUsuario.appendChild(op);
        });

    const selLibro = document.getElementById('prestamo-libro');
    selLibro.innerHTML = '<option value="">-- Seleccionar libro --</option>';
    libros.forEach((l) => {
        const op = document.createElement('option');
        op.value = l.id;
        op.textContent = l.titulo + ' (disponibles: ' + l.disponibles + ')';
        if (l.disponibles <= 0) op.disabled = true;
        selLibro.appendChild(op);
    });
}

function renderPrestamos(lista) {
    const tbody = document.getElementById('tabla-prestamos');
    tbody.innerHTML = '';
    document.getElementById('vacio-prestamos').style.display = lista.length ? 'none' : 'block';

    lista.forEach((p) => {
        const devuelto = p.estado === 'DEVUELTO';
        const claseEstado = devuelto ? 'insignia-verde' : 'insignia-ambar';
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td data-label="ID">${p.id}</td>
            <td data-label="Usuario"><strong>${p.usuarioNombre}</strong></td>
            <td data-label="Libro">${p.libroTitulo}</td>
            <td data-label="Préstamo">${formatearFecha(p.fechaPrestamo)}</td>
            <td data-label="Límite">${formatearFecha(p.fechaLimite)}</td>
            <td data-label="Devolución">${p.fechaDevolucion ? formatearFecha(p.fechaDevolucion) : '-'}</td>
            <td data-label="Estado"><span class="insignia ${claseEstado}">${p.estado}</span></td>
            <td data-label="Acciones" class="acciones-celda">
                <div class="acciones">
                    ${devuelto ? '' : `<button class="boton boton-primario boton-mini" onclick="devolverPrestamo(${p.id})">${icono('devolver')}<span>Devolver</span></button>`}
                    <button class="boton boton-secundario boton-mini" onclick="editarPrestamo(${p.id})">${icono('editar')}<span>Editar</span></button>
                    <button class="boton boton-peligro boton-mini" onclick="eliminarPrestamo(${p.id})">${icono('eliminar')}<span>Eliminar</span></button>
                </div>
            </td>
        `;
        tbody.appendChild(fila);
    });
}

function abrirModalPrestamo(id) {
    document.getElementById('form-prestamo').reset();

    const hoy = new Date();
    document.getElementById('prestamo-fecha').value = hoy.toISOString().split('T')[0];
    const limite = new Date(hoy);
    limite.setDate(limite.getDate() + 15);
    document.getElementById('prestamo-limite').value = limite.toISOString().split('T')[0];

    document.getElementById('campo-estado').style.display = 'block';

    if (id) {
        editarPrestamo(id);
    } else {
        document.getElementById('prestamo-id').value = '';
        document.getElementById('prestamo-estado').value = 'PENDIENTE';
        document.getElementById('titulo-modal-prestamo').textContent = 'Nuevo préstamo';
        abrirModal('modal-prestamo');
    }
}

async function editarPrestamo(id) {
    try {
        const resp = await apiRequest('/prestamos/' + id);
        const p = resp.data;
        document.getElementById('prestamo-id').value = p.id;
        document.getElementById('prestamo-usuario').value = p.usuarioId;
        document.getElementById('prestamo-libro').value = p.libroId;
        document.getElementById('prestamo-fecha').value = p.fechaPrestamo;
        document.getElementById('prestamo-limite').value = p.fechaLimite;
        document.getElementById('prestamo-estado').value = p.estado;
        document.getElementById('titulo-modal-prestamo').textContent = 'Editar préstamo';
        document.getElementById('campo-estado').style.display = 'block';
        abrirModal('modal-prestamo');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarPrestamo(id) {
    if (!confirm('¿Seguro que deseas eliminar este préstamo?')) return;
    try {
        await apiRequest('/prestamos/' + id, 'DELETE');
        mostrarToast('Préstamo eliminado');
        cargarPrestamos();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function devolverPrestamo(id) {
    try {
        await apiRequest('/prestamos/' + id + '/devolver', 'PUT');
        mostrarToast('Préstamo devuelto');
        cargarPrestamos();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

let formularioPrestamoListo = false;

document.addEventListener('DOMContentLoaded', () => {
    if (formularioPrestamoListo) return;
    formularioPrestamoListo = true;

    const form = document.getElementById('form-prestamo');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const id = document.getElementById('prestamo-id').value;
        const data = {
            usuarioId: parseInt(document.getElementById('prestamo-usuario').value, 10),
            libroId: parseInt(document.getElementById('prestamo-libro').value, 10),
            fechaPrestamo: document.getElementById('prestamo-fecha').value || null,
            fechaLimite: document.getElementById('prestamo-limite').value || null,
            estado: id ? document.getElementById('prestamo-estado').value : null,
        };

        if (!data.usuarioId || !data.libroId) {
            mostrarToast('Debes seleccionar usuario y libro.', 'error');
            return;
        }

        try {
            if (id) {
                await apiRequest('/prestamos/' + id, 'PUT', data);
                mostrarToast('Préstamo actualizado');
            } else {
                await apiRequest('/prestamos', 'POST', data);
                mostrarToast('Préstamo creado');
            }
            cerrarModal('modal-prestamo');
            cargarPrestamos();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});