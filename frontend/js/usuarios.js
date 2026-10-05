async function cargarUsuarios() {
    const tbody = document.getElementById('tabla-usuarios');
    tbody.innerHTML = filaCarga(7);
    document.getElementById('vacio-usuarios').style.display = 'none';
    try {
        const resp = await apiRequest('/usuarios');
        renderUsuarios(resp.data || []);
    } catch (err) {
        mostrarToast(err.message, 'error');
        tbody.innerHTML = '';
    }
}

function renderUsuarios(lista) {
    const tbody = document.getElementById('tabla-usuarios');
    tbody.innerHTML = '';
    document.getElementById('vacio-usuarios').style.display = lista.length ? 'none' : 'block';

    lista.forEach((u) => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td data-label="ID">${u.id}</td>
            <td data-label="Usuario"><strong>${u.username}</strong></td>
            <td data-label="Nombre">${u.nombre} ${u.apellido || ''}</td>
            <td data-label="Email">${u.email}</td>
            <td data-label="Rol"><span class="insignia ${u.rol === 'ADMIN' ? 'insignia-indigo' : 'insignia-gris'}">${u.rol}</span></td>
            <td data-label="Estado"><span class="insignia ${u.activo ? 'insignia-verde' : 'insignia-roja'}">${u.activo ? 'Activo' : 'Inactivo'}</span></td>
            <td data-label="Acciones" class="acciones-celda">
                <div class="acciones">
                    <button class="boton boton-secundario boton-mini" onclick="editarUsuario(${u.id})">${icono('editar')}<span>Editar</span></button>
                    <button class="boton boton-peligro boton-mini" onclick="eliminarUsuario(${u.id})">${icono('eliminar')}<span>Eliminar</span></button>
                </div>
            </td>
        `;
        tbody.appendChild(fila);
    });
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
        document.getElementById('usuario-username').value = u.username;
        document.getElementById('usuario-password').value = '';
        document.getElementById('usuario-nombre').value = u.nombre;
        document.getElementById('usuario-apellido').value = u.apellido || '';
        document.getElementById('usuario-email').value = u.email;
        document.getElementById('usuario-rol').value = u.rol;
        document.getElementById('usuario-activo').value = String(u.activo);
        document.getElementById('titulo-modal-usuario').textContent = 'Editar usuario';
        document.getElementById('nota-usuario-password').textContent = 'Edición: deja la contraseña en blanco para mantener la actual.';
        abrirModal('modal-usuario');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarUsuario(id) {
    const u = (await apiRequest('/usuarios/' + id).catch(() => null))?.data;
    const nombre = u ? (u.username || '') : '';
    if (!confirm('¿Seguro que deseas eliminar el usuario ' + nombre + '?')) return;

    try {
        await apiRequest('/usuarios/' + id, 'DELETE');
        mostrarToast('Usuario eliminado');
        cargarUsuarios();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

let formularioUsuarioListo = false;

document.addEventListener('DOMContentLoaded', () => {
    if (formularioUsuarioListo) return;
    formularioUsuarioListo = true;

    const form = document.getElementById('form-usuario');
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