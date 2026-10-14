// ==========================================================================
// Catalogo de libros: listado paginado, busqueda, filtros y ejemplares.
// ==========================================================================

let tablaLibros = null;
let libroActualId = null;
let categoriasCargadas = false;

function cargarLibros() {
    if (!tablaLibros) {
        tablaLibros = new TablaPaginada({
            tbody: 'tabla-libros',
            vacio: 'vacio-libros',
            paginacion: 'pag-libros',
            buscador: 'buscar-libros',
            colspan: 8,
            size: 10,
            filtros: [
                { id: 'filtro-categoria', param: 'categoria' },
                { id: 'filtro-estado-libro', param: 'estado' },
                { id: 'filtro-disponibles', param: 'soloDisponibles', valor: 'true' },
            ],
            cargar: (params) => apiRequest('/libros/buscar?' + params.toString()),
            render: (l) => filaLibro(l),
        });
    }
    cargarCategorias().then(() => tablaLibros.cargar(0));
}

async function cargarCategorias() {
    if (categoriasCargadas) return;
    try {
        const resp = await apiRequest('/libros/categorias');
        const cats = resp.data || [];
        const sel = document.getElementById('filtro-categoria');
        const data = document.getElementById('lista-categorias');
        sel.innerHTML = '<option value="">Todas las categorías</option>';
        data.innerHTML = '';
        cats.forEach((c) => {
            const op = document.createElement('option');
            op.value = c;
            op.textContent = c;
            sel.appendChild(op);
            const op2 = document.createElement('option');
            op2.value = c;
            data.appendChild(op2);
        });
        categoriasCargadas = true;
    } catch (err) {
        // silencioso: el filtro simplemente queda vacio
    }
}

function filaLibro(l) {
    const disponibles = l.disponibles > 0;
    const estado = l.estado === 'INACTIVO'
        ? '<span class="insignia insignia-gris">Inactivo</span>'
        : '<span class="insignia insignia-verde">Activo</span>';
    const acciones = esStaff()
        ? '<div class="acciones">' +
          '<button class="boton boton-secundario boton-mini" onclick="verEjemplares(' + l.id + ')">' + icono('ver') + '<span>Ejemplares</span></button>' +
          '<button class="boton boton-secundario boton-mini" onclick="editarLibro(' + l.id + ')">' + icono('editar') + '<span>Editar</span></button>' +
          '<button class="boton boton-peligro boton-mini" onclick="eliminarLibro(' + l.id + ')">' + icono('eliminar') + '<span>Eliminar</span></button>' +
          '</div>'
        : '<div class="acciones"><button class="boton boton-secundario boton-mini" onclick="verEjemplares(' + l.id + ')">' + icono('ver') + '<span>Ver</span></button></div>';

    return (
        '<td data-label="Título"><strong>' + esc(l.titulo) + '</strong>' + (l.isbn ? '<div class="secundario">ISBN ' + esc(l.isbn) + '</div>' : '') + '</td>' +
        '<td data-label="Autor">' + esc(l.autor || '-') + '</td>' +
        '<td data-label="Categoría">' + esc(l.categoria || '-') + '</td>' +
        '<td data-label="Año">' + esc(l.anioPublicacion || '-') + '</td>' +
        '<td data-label="Editorial">' + esc(l.editorial || '-') + '</td>' +
        '<td data-label="Disponibles"><span class="insignia ' + (disponibles ? 'insignia-verde' : 'insignia-roja') + '">' + l.disponibles + ' / ' + l.cantidad + '</span></td>' +
        '<td data-label="Estado">' + estado + '</td>' +
        '<td data-label="Acciones" class="acciones-celda">' + acciones + '</td>'
    );
}

function abrirModalLibro(id) {
    document.getElementById('form-libro').reset();
    document.getElementById('titulo-modal-libro').textContent = 'Nuevo libro';
    document.getElementById('libro-cantidad').value = '1';
    document.getElementById('libro-estado').value = 'ACTIVO';
    if (id) {
        editarLibro(id);
    } else {
        document.getElementById('libro-id').value = '';
        abrirModal('modal-libro');
    }
}

async function editarLibro(id) {
    try {
        const resp = await apiRequest('/libros/' + id);
        const l = resp.data;
        document.getElementById('libro-id').value = l.id;
        document.getElementById('libro-titulo').value = l.titulo || '';
        document.getElementById('libro-autor').value = l.autor || '';
        document.getElementById('libro-isbn').value = l.isbn || '';
        document.getElementById('libro-categoria').value = l.categoria || '';
        document.getElementById('libro-anio').value = l.anioPublicacion || '';
        document.getElementById('libro-editorial').value = l.editorial || '';
        document.getElementById('libro-ubicacion').value = l.ubicacion || '';
        document.getElementById('libro-cantidad').value = l.cantidad || 1;
        document.getElementById('libro-portada').value = l.portadaUrl || '';
        document.getElementById('libro-estado').value = l.estado || 'ACTIVO';
        document.getElementById('libro-descripcion').value = l.descripcion || '';
        document.getElementById('titulo-modal-libro').textContent = 'Editar libro';
        abrirModal('modal-libro');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function eliminarLibro(id) {
    const ok = await confirmar('¿Deseas eliminar este libro y sus ejemplares? Esta acción no se puede deshacer.', {
        titulo: 'Eliminar libro',
        textoBoton: 'Eliminar',
        peligro: true,
    });
    if (!ok) return;
    try {
        await apiRequest('/libros/' + id, 'DELETE');
        mostrarToast('Libro eliminado');
        tablaLibros.cargar(tablaLibros.page);
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

// --- Ejemplares ---
function verEjemplares(id) {
    libroActualId = id;
    document.getElementById('titulo-modal-ejemplares').textContent = 'Ejemplares del libro';
    document.getElementById('ejemplar-ubicacion').value = '';
    const toolbar = document.querySelector('#modal-ejemplares .toolbar-tabla');
    if (toolbar) toolbar.classList.toggle('oculto', !esStaff());
    cargarEjemplares();
    abrirModal('modal-ejemplares');
}

async function cargarEjemplares() {
    const tbody = document.getElementById('tabla-ejemplares');
    tbody.innerHTML = filaCarga(4);
    document.getElementById('vacio-ejemplares').style.display = 'none';
    try {
        const resp = await apiRequest('/libros/' + libroActualId + '/ejemplares');
        const lista = resp.data || [];
        tbody.innerHTML = '';
        document.getElementById('vacio-ejemplares').style.display = lista.length ? 'none' : 'block';
        lista.forEach((e) => {
            const opciones = ['DISPONIBLE', 'PRESTADO', 'RESERVADO', 'PERDIDO', 'BAJA']
                .map((s) => '<option value="' + s + '" ' + (e.estado === s ? 'selected' : '') + '>' + s + '</option>')
                .join('');
            const acciones = esStaff()
                ? '<select onchange="cambiarEstadoEjemplar(' + e.id + ', this.value)" aria-label="Cambiar estado">' + opciones + '</select>' +
                  '<button class="boton boton-peligro boton-mini" onclick="eliminarEjemplar(' + e.id + ')">' + icono('eliminar') + '<span>Eliminar</span></button>'
                : '-';
            const tr = document.createElement('tr');
            tr.innerHTML =
                '<td data-label="Código"><strong>' + esc(e.codigo) + '</strong></td>' +
                '<td data-label="Ubicación">' + esc(e.ubicacion || '-') + '</td>' +
                '<td data-label="Estado">' + dotEjemplar(e.estado) + '</td>' +
                '<td data-label="Acciones" class="acciones-celda"><div class="acciones">' + acciones + '</div></td>';
            tbody.appendChild(tr);
        });
    } catch (err) {
        mostrarToast(err.message, 'error');
        tbody.innerHTML = '';
    }
}

async function agregarEjemplar() {
    try {
        const ubicacion = document.getElementById('ejemplar-ubicacion').value.trim();
        await apiRequest('/libros/' + libroActualId + '/ejemplares?ubicacion=' + encodeURIComponent(ubicacion), 'POST');
        mostrarToast('Ejemplar agregado');
        document.getElementById('ejemplar-ubicacion').value = '';
        cargarEjemplares();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

async function cambiarEstadoEjemplar(id, estado) {
    try {
        await apiRequest('/ejemplares/' + id + '/estado?estado=' + estado, 'PUT');
        mostrarToast('Estado del ejemplar actualizado');
        cargarEjemplares();
    } catch (err) {
        mostrarToast(err.message, 'error');
        cargarEjemplares();
    }
}

async function eliminarEjemplar(id) {
    const ok = await confirmar('¿Eliminar este ejemplar?', { titulo: 'Eliminar ejemplar', textoBoton: 'Eliminar', peligro: true });
    if (!ok) return;
    try {
        await apiRequest('/ejemplares/' + id, 'DELETE');
        mostrarToast('Ejemplar eliminado');
        cargarEjemplares();
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

// --- Formulario libro ---
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-libro');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('libro-id').value;
        const libro = {
            titulo: document.getElementById('libro-titulo').value.trim(),
            autor: document.getElementById('libro-autor').value.trim(),
            isbn: document.getElementById('libro-isbn').value.trim(),
            categoria: document.getElementById('libro-categoria').value.trim(),
            anioPublicacion: parseInt(document.getElementById('libro-anio').value, 10) || null,
            editorial: document.getElementById('libro-editorial').value.trim(),
            ubicacion: document.getElementById('libro-ubicacion').value.trim(),
            cantidad: parseInt(document.getElementById('libro-cantidad').value, 10) || 1,
            portadaUrl: document.getElementById('libro-portada').value.trim(),
            estado: document.getElementById('libro-estado').value,
            descripcion: document.getElementById('libro-descripcion').value.trim(),
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
            categoriasCargadas = false;
            cargarLibros();
        } catch (err) {
            mostrarToast(err.message, 'error');
        }
    });
});
