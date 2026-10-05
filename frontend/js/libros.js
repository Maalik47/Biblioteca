async function cargarLibros() {
    const tbody = document.getElementById('tabla-libros');
    tbody.innerHTML = filaCarga(9);
    document.getElementById('vacio-libros').style.display = 'none';
    try {
        const resp = await apiRequest('/libros');
        renderLibros(resp.data || []);
    } catch (err) {
        mostrarToast(err.message, 'error');
        tbody.innerHTML = '';
    }
}

function renderLibros(lista) {
    const tbody = document.getElementById('tabla-libros');
    tbody.innerHTML = '';
    document.getElementById('vacio-libros').style.display = lista.length ? 'none' : 'block';

    lista.forEach((l) => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td data-label="ID">${l.id}</td>
            <td data-label="Título"><strong>${l.titulo}</strong></td>
            <td data-label="Autor">${l.autor}</td>
            <td data-label="ISBN">${l.isbn || '-'}</td>
            <td data-label="Categoría">${l.categoria || '-'}</td>
            <td data-label="Año">${l.anioPublicacion || '-'}</td>
            <td data-label="Cantidad">${l.cantidad}</td>
            <td data-label="Disponibles"><span class="insignia ${l.disponibles > 0 ? 'insignia-verde' : 'insignia-roja'}">${l.disponibles}</span></td>
            <td data-label="Acciones" class="acciones-celda">
                <div class="acciones">
                    <button class="boton boton-secundario boton-mini" onclick="editarLibro(${l.id})">${icono('editar')}<span>Editar</span></button>
                    <button class="boton boton-peligro boton-mini" onclick="eliminarLibro(${l.id})">${icono('eliminar')}<span>Eliminar</span></button>
                </div>
            </td>
        `;
        tbody.appendChild(fila);
    });
}

function abrirModalLibro(id) {
    document.getElementById('form-libro').reset();
    document.getElementById('titulo-modal-libro').textContent = 'Nuevo libro';

    if (id) {
        editarLibro(id);
    } else {
        document.getElementById('libro-id').value = '';
        document.getElementById('libro-cantidad').value = '1';
        abrirModal('modal-libro');
    }
}

async function editarLibro(id) {
    try {
        const resp = await apiRequest('/libros/' + id);
        const l = resp.data;
        document.getElementById('libro-id').value = l.id;
        document.getElementById('libro-titulo').value = l.titulo;
        document.getElementById('libro-autor').value = l.autor;
        document.getElementById('libro-isbn').value = l.isbn || '';
        document.getElementById('libro-categoria').value = l.categoria || '';
        document.getElementById('libro-anio').value = l.anioPublicacion || '';
        document.getElementById('libro-cantidad').value = l.cantidad;
        document.getElementById('titulo-modal-libro').textContent = 'Editar libro';
        abrirModal('modal-libro');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarLibro(id) {
    if (!confirm('¿Seguro que deseas eliminar el libro seleccionado?')) return;
    try {
        await apiRequest('/libros/' + id, 'DELETE');
        mostrarToast('Libro eliminado');
        cargarLibros();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

let formularioLibroListo = false;

document.addEventListener('DOMContentLoaded', () => {
    if (formularioLibroListo) return;
    formularioLibroListo = true;

    const form = document.getElementById('form-libro');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const id = document.getElementById('libro-id').value;
        const libro = {
            titulo: document.getElementById('libro-titulo').value.trim(),
            autor: document.getElementById('libro-autor').value.trim(),
            isbn: document.getElementById('libro-isbn').value.trim(),
            categoria: document.getElementById('libro-categoria').value.trim(),
            anioPublicacion: parseInt(document.getElementById('libro-anio').value, 10) || null,
            cantidad: parseInt(document.getElementById('libro-cantidad').value, 10) || 1,
        };

        try {
            if (id) {
                await apiRequest('/libros/' + id, 'PUT', libro);
                mostrarToast('Libro actualizado');
            } else {
                await apiRequest('/libros', 'POST', libro);
                mostrarToast('Libro creado');
            }
            cerrarModal('modal-libro');
            cargarLibros();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});