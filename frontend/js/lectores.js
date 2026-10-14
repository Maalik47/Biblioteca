// ==========================================================================
// Lectores: personas registradas en la biblioteca (con o sin acceso al panel).
// ==========================================================================

let tablaLectores = null;

function cargarLectores() {
    if (!tablaLectores) {
        tablaLectores = new TablaPaginada({
            tbody: 'tabla-lectores',
            vacio: 'vacio-lectores',
            paginacion: 'pag-lectores',
            buscador: 'buscar-lectores',
            colspan: 8,
            size: 10,
            filtros: [
                { id: 'filtro-tipo-lector', param: 'tipoLector' },
                { id: 'filtro-acceso', param: 'soloAcceso', valor: 'true' },
            ],
            cargar: (params) => apiRequest('/lectores?' + params.toString()),
            render: (u) => filaLector(u),
        });
    }
    tablaLectores.cargar(0);
}

function filaLector(u) {
    const insTipo = {
        GENERAL: 'insignia-gris',
        ESTUDIANTE: 'insignia-indigo',
        DOCENTE: 'insignia-verde',
        INVESTIGADOR: 'insignia-ambar',
    };
    const contacto = [u.email, u.telefono].filter(Boolean).join(' · ') || '-';
    const acceso = u.puedeAcceder
        ? '<span class="insignia insignia-verde">Sí</span>'
        : '<span class="insignia insignia-gris">No</span>';
    const estado = u.activo
        ? '<span class="insignia insignia-verde">Activo</span>'
        : '<span class="insignia insignia-roja">Inactivo</span>';

    return (
        '<td data-label="Código">' + esc(u.codigoLector || '-') + '</td>' +
        '<td data-label="Nombre"><strong>' + esc(u.nombre + ' ' + (u.apellido || '')) + '</strong>' + (u.restriccion ? '<div class="secundario">' + esc(u.restriccion) + '</div>' : '') + '</td>' +
        '<td data-label="Contacto"><div class="secundario">' + esc(contacto) + '</div></td>' +
        '<td data-label="Identificación">' + esc(u.identificacion || '-') + '</td>' +
        '<td data-label="Tipo"><span class="insignia ' + (insTipo[u.tipoLector] || 'insignia-gris') + '">' + esc(u.tipoLector || '-') + '</span></td>' +
        '<td data-label="Acceso">' + acceso + '</td>' +
        '<td data-label="Estado">' + estado + '</td>' +
        '<td data-label="Acciones" class="acciones-celda"><div class="acciones">' +
        '<button class="boton boton-secundario boton-mini" onclick="editarLector(' + u.id + ')">' + icono('editar') + '<span>Editar</span></button>' +
        '<button class="boton boton-peligro boton-mini" onclick="eliminarLector(' + u.id + ')">' + icono('eliminar') + '<span>Eliminar</span></button>' +
        '</div></td>'
    );
}

function toggleCamposAcceso(mostrar) {
    document.getElementById('campos-acceso').style.display = mostrar ? 'grid' : 'none';
}

function abrirModalLector(id) {
    document.getElementById('form-lector').reset();
    document.getElementById('titulo-modal-lector').textContent = 'Nuevo lector';
    document.getElementById('nota-lector-password').textContent = '';
    document.getElementById('lector-tipo').value = 'GENERAL';
    document.getElementById('lector-activo').value = 'true';
    document.getElementById('lector-acceso').checked = false;
    toggleCamposAcceso(false);
    if (id) {
        editarLector(id);
    } else {
        document.getElementById('lector-id').value = '';
        abrirModal('modal-lector');
    }
}

async function editarLector(id) {
    try {
        const resp = await apiRequest('/lectores/' + id);
        const u = resp.data;
        document.getElementById('lector-id').value = u.id;
        document.getElementById('lector-nombre').value = u.nombre || '';
        document.getElementById('lector-apellido').value = u.apellido || '';
        document.getElementById('lector-email').value = u.email || '';
        document.getElementById('lector-identificacion').value = u.identificacion || '';
        document.getElementById('lector-telefono').value = u.telefono || '';
        document.getElementById('lector-tipo').value = u.tipoLector || 'GENERAL';
        document.getElementById('lector-restriccion').value = u.restriccion || '';
        document.getElementById('lector-activo').value = String(u.activo);
        document.getElementById('lector-acceso').checked = !!u.puedeAcceder;
        document.getElementById('lector-username').value = u.username || '';
        document.getElementById('lector-password').value = '';
        toggleCamposAcceso(!!u.puedeAcceder);
        document.getElementById('nota-lector-password').textContent = 'Edición: deja la contraseña en blanco para mantener la actual.';
        document.getElementById('titulo-modal-lector').textContent = 'Editar lector';
        abrirModal('modal-lector');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarLector(id) {
    const ok = await confirmar('¿Deseas eliminar este lector?', { titulo: 'Eliminar lector', textoBoton: 'Eliminar', peligro: true });
    if (!ok) return;
    try {
        await apiRequest('/lectores/' + id, 'DELETE');
        mostrarToast('Lector eliminado');
        tablaLectores.cargar(tablaLectores.page);
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const chk = document.getElementById('lector-acceso');
    if (chk) chk.addEventListener('change', () => toggleCamposAcceso(chk.checked));

    const form = document.getElementById('form-lector');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('lector-id').value;
        const puedeAcceder = document.getElementById('lector-acceso').checked;
        const lector = {
            nombre: document.getElementById('lector-nombre').value.trim(),
            apellido: document.getElementById('lector-apellido').value.trim(),
            email: document.getElementById('lector-email').value.trim(),
            identificacion: document.getElementById('lector-identificacion').value.trim(),
            telefono: document.getElementById('lector-telefono').value.trim(),
            tipoLector: document.getElementById('lector-tipo').value,
            restriccion: document.getElementById('lector-restriccion').value.trim(),
            activo: document.getElementById('lector-activo').value === 'true',
            puedeAcceder: puedeAcceder,
            rol: 'USUARIO',
            username: puedeAcceder ? document.getElementById('lector-username').value.trim() : null,
            password: puedeAcceder ? document.getElementById('lector-password').value : null,
        };
        try {
            if (id) {
                await apiRequest('/lectores/' + id, 'PUT', lector);
                mostrarToast('Lector actualizado');
            } else {
                await apiRequest('/lectores', 'POST', lector);
                mostrarToast('Lector creado');
            }
            cerrarModal('modal-lector');
            cargarLectores();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});
