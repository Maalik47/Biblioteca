// ==========================================================================
// Usuarios del sistema: cuentas con acceso al panel (puedeAcceder = true).
// ==========================================================================

let tablaUsuarios = null;

function cargarUsuarios() {
    if (!tablaUsuarios) {
        tablaUsuarios = new TablaPaginada({
            tbody: 'tabla-usuarios',
            vacio: 'vacio-usuarios',
            paginacion: 'pag-usuarios',
            buscador: 'buscar-usuarios',
            colspan: 6,
            size: 10,
            filtros: [{ id: 'filtro-rol', param: 'rol' }],
            cargar: (params) => {
                params.set('soloAcceso', 'true');
                return apiRequest('/usuarios/buscar?' + params.toString());
            },
            render: (u) => filaUsuario(u),
        });
    }
    tablaUsuarios.cargar(0);
}

function filaUsuario(u) {
    const insRol = {
        ADMIN: 'insignia-indigo',
        BIBLIOTECARIO: 'insignia-verde',
        USUARIO: 'insignia-gris',
    };
    const estado = u.activo
        ? '<span class="insignia insignia-verde">Activo</span>'
        : '<span class="insignia insignia-roja">Inactivo</span>';

    return (
        '<td data-label="Usuario"><strong>' + esc(u.username) + '</strong></td>' +
        '<td data-label="Nombre">' + esc(u.nombre + ' ' + (u.apellido || '')) + '</td>' +
        '<td data-label="Email">' + esc(u.email || '-') + '</td>' +
        '<td data-label="Rol"><span class="insignia ' + (insRol[u.rol] || 'insignia-gris') + '">' + esc(u.rol) + '</span></td>' +
        '<td data-label="Estado">' + estado + '</td>' +
        '<td data-label="Acciones" class="acciones-celda"><div class="acciones">' +
        '<button class="boton boton-secundario boton-mini" onclick="editarUsuario(' + u.id + ')">' + icono('editar') + '<span>Editar</span></button>' +
        '<button class="boton boton-peligro boton-mini" onclick="eliminarUsuario(' + u.id + ')">' + icono('eliminar') + '<span>Eliminar</span></button>' +
        '</div></td>'
    );
}

function abrirModalUsuario(id) {
    document.getElementById('form-usuario').reset();
    document.getElementById('nota-usuario-password').textContent = '';
    document.getElementById('titulo-modal-usuario').textContent = 'Nuevo usuario';
    if (id) {
        editarUsuario(id);
    } else {
        document.getElementById('usuario-id').value = '';
        document.getElementById('usuario-rol').value = 'USUARIO';
        document.getElementById('usuario-activo').value = 'true';
        abrirModal('modal-usuario');
    }
}

async function editarUsuario(id) {
    try {
        const resp = await apiRequest('/usuarios/' + id);
        const u = resp.data;
        document.getElementById('usuario-id').value = u.id;
        document.getElementById('usuario-username').value = u.username || '';
        document.getElementById('usuario-password').value = '';
        document.getElementById('usuario-nombre').value = u.nombre || '';
        document.getElementById('usuario-apellido').value = u.apellido || '';
        document.getElementById('usuario-email').value = u.email || '';
        document.getElementById('usuario-rol').value = u.rol || 'USUARIO';
        document.getElementById('usuario-activo').value = String(u.activo);
        document.getElementById('titulo-modal-usuario').textContent = 'Editar usuario';
        document.getElementById('nota-usuario-password').textContent = 'Edición: deja la contraseña en blanco para mantener la actual.';
        abrirModal('modal-usuario');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarUsuario(id) {
    const ok = await confirmar('¿Eliminar esta cuenta de acceso al sistema?', { titulo: 'Eliminar usuario', textoBoton: 'Eliminar', peligro: true });
    if (!ok) return;
    try {
        await apiRequest('/usuarios/' + id, 'DELETE');
        mostrarToast('Usuario eliminado');
        tablaUsuarios.cargar(tablaUsuarios.page);
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-usuario');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('usuario-id').value;
        const usuario = {
            username: document.getElementById('usuario-username').value.trim(),
            password: document.getElementById('usuario-password').value,
            nombre: document.getElementById('usuario-nombre').value.trim(),
            apellido: document.getElementById('usuario-apellido').value.trim(),
            email: document.getElementById('usuario-email').value.trim(),
            rol: document.getElementById('usuario-rol').value,
            activo: document.getElementById('usuario-activo').value === 'true',
        };
        try {
            if (id) {
                await apiRequest('/usuarios/' + id, 'PUT', usuario);
                mostrarToast('Usuario actualizado');
            } else {
                await apiRequest('/usuarios', 'POST', usuario);
                mostrarToast('Usuario creado');
            }
            cerrarModal('modal-usuario');
            cargarUsuarios();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});
