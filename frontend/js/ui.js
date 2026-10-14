// ==========================================================================
// Utilidades de interfaz reutilizables (Depende de api.js: getToken, API_URL,
// icono, filaCarga, mostrarToast). Se carga antes que los modulos.
// ==========================================================================

// --- Iconos adicionales (extiende la funcion icono() de api.js) ---
(function () {
    const extra = {
        dashboard: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>',
        libro: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>',
        lector: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
        prestamo: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>',
        reserva: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>',
        reporte: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>',
        auditoria: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
        usuario: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
        salir: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
        buscar: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>',
        ver: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>',
        renovar: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>',
        descargar: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>',
        campana: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>',
        reloj: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
        ubicacion: '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>',
        circulo: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
    };
    const base = icono;
    icono = function (nombre) {
        return extra[nombre] || base(nombre);
    };
})();

// --- Texto seguro para interpolar en HTML ---
function esc(valor) {
    if (valor === null || valor === undefined) return '';
    return String(valor)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// --- Retraso de ejecucion (para buscadores) ---
function debounce(fn, ms) {
    let t;
    return function () {
        const args = arguments;
        const ctx = this;
        clearTimeout(t);
        t = setTimeout(function () {
            fn.apply(ctx, args);
        }, ms);
    };
}

// --- Modal de confirmacion (devuelve Promise<boolean>) ---
let _resolverConfirmacion = null;

function confirmar(mensaje, opciones) {
    opciones = opciones || {};
    const modal = document.getElementById('modal-confirmar');
    document.getElementById('confirmar-titulo').textContent = opciones.titulo || 'Confirmar accion';
    document.getElementById('confirmar-mensaje').textContent = mensaje;
    const btnAceptar = document.getElementById('confirmar-aceptar');
    btnAceptar.textContent = opciones.textoBoton || 'Confirmar';
    btnAceptar.className = 'boton ' + (opciones.peligro ? 'boton-peligro' : 'boton-primario');
    if (opciones.peligro) btnAceptar.insertAdjacentHTML('afterbegin', icono('eliminar'));
    return new Promise(function (resolve) {
        _resolverConfirmacion = resolve;
        abrirModal('modal-confirmar');
    });
}

function resolverConfirmacion(valor) {
    const r = _resolverConfirmacion;
    _resolverConfirmacion = null;
    cerrarModal('modal-confirmar');
    if (r) r(valor);
}

// --- Rellenar un <select> ---
function llenarSelect(idOrEl, items, opciones) {
    opciones = opciones || {};
    const sel = typeof idOrEl === 'string' ? document.getElementById(idOrEl) : idOrEl;
    if (!sel) return;
    const placeholder = opciones.placeholder;
    sel.innerHTML = placeholder
        ? '<option value="">' + esc(placeholder) + '</option>'
        : '';
    items.forEach(function (it) {
        const op = document.createElement('option');
        op.value = opciones.valor ? opciones.valor(it) : it.id;
        op.textContent = opciones.etiqueta ? opciones.etiqueta(it) : it.nombre;
        if (opciones.deshabilitar && opciones.deshabilitar(it)) op.disabled = true;
        sel.appendChild(op);
    });
}

// --- Insignias de estado ---
function insigniaPrestamo(p) {
    if (p.estado === 'DEVUELTO') return { texto: 'Devuelto', clase: 'insignia-verde' };
    if (p.vencido) return { texto: 'Vencido', clase: 'insignia-roja' };
    return { texto: 'Activo', clase: 'insignia-ambar' };
}

function insigniaReserva(r) {
    const mapa = {
        PENDIENTE: { texto: 'Pendiente', clase: 'insignia-ambar' },
        DISPONIBLE: { texto: 'Disponible', clase: 'insignia-verde' },
        CUMPLIDA: { texto: 'Cumplida', clase: 'insignia-indigo' },
        CANCELADA: { texto: 'Cancelada', clase: 'insignia-gris' },
    };
    return mapa[r.estado] || { texto: r.estado, clase: 'insignia-gris' };
}

function dotEjemplar(estado) {
    const mapa = {
        DISPONIBLE: { texto: 'Disponible', clase: 'ok' },
        PRESTADO: { texto: 'Prestado', clase: 'aviso' },
        RESERVADO: { texto: 'Reservado', clase: 'info' },
        PERDIDO: { texto: 'Perdido', clase: 'mal' },
        BAJA: { texto: 'Baja', clase: 'mal' },
    };
    const s = mapa[estado] || { texto: estado, clase: '' };
    return '<span class="estado-dot ' + s.clase + '">' + esc(s.texto) + '</span>';
}

// --- Descarga de archivos autenticada (CSV) ---
async function descargarArchivo(path, nombre) {
    const token = getToken();
    try {
        const res = await fetch(API_URL + path, {
            headers: token ? { Authorization: 'Bearer ' + token } : {},
        });
        if (!res.ok) throw new Error('No se pudo descargar el archivo (' + res.status + ').');
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nombre;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        mostrarToast('Descarga iniciada');
    } catch (err) {
        mostrarToast(err.message, 'error');
    }
}

// ==========================================================================
// Tabla paginada reutilizable: busqueda, filtros y paginacion.
// ==========================================================================
class TablaPaginada {
    constructor(opciones) {
        this.o = opciones;
        this.page = 0;
        this.size = opciones.size || 10;
        this.q = '';
        this.montado = false;
        this.cargando = false;
    }

    montar() {
        if (this.montado) return;
        this.montado = true;
        this.tbody = document.getElementById(this.o.tbody);

        if (this.o.buscador) {
            const inp = document.getElementById(this.o.buscador);
            if (inp) {
                inp.addEventListener(
                    'input',
                    debounce(() => {
                        this.q = inp.value.trim();
                        this.cargar(0);
                    }, 320)
                );
            }
        }

        (this.o.filtros || []).forEach((f) => {
            const el = document.getElementById(f.id);
            if (el) {
                el.addEventListener('change', () => this.cargar(0));
            }
        });
    }

    leerFiltros() {
        const p = {};
        (this.o.filtros || []).forEach((f) => {
            const el = document.getElementById(f.id);
            if (!el) return;
            let val;
            if (el.type === 'checkbox') {
                val = el.checked ? f.valor || 'true' : '';
            } else {
                val = el.value;
            }
            if (val !== '' && val !== null && val !== undefined) p[f.param] = val;
        });
        return p;
    }

    async cargar(page) {
        this.montar();
        if (this.cargando) return;
        this.cargando = true;
        if (typeof page === 'number') this.page = page;

        this.tbody.innerHTML = filaCarga(this.o.colspan || 6);
        const vacio = this.o.vacio ? document.getElementById(this.o.vacio) : null;
        if (vacio) vacio.style.display = 'none';

        const params = new URLSearchParams();
        if (this.q) params.set('q', this.q);
        const filtros = this.leerFiltros();
        Object.keys(filtros).forEach((k) => params.set(k, filtros[k]));
        params.set('page', this.page);
        params.set('size', this.size);

        try {
            const resp = await this.o.cargar(params);
            const data = resp && resp.contenido !== undefined ? resp : (resp && resp.data) || {};
            this.render(data);
        } catch (err) {
            mostrarToast(err.message, 'error');
            this.tbody.innerHTML = '';
        } finally {
            this.cargando = false;
        }
    }

    render(data) {
        const lista = data.contenido || [];
        this.tbody.innerHTML = '';
        const vacio = this.o.vacio ? document.getElementById(this.o.vacio) : null;
        if (vacio) vacio.style.display = lista.length ? 'none' : 'block';

        lista.forEach((item, i) => {
            const tr = document.createElement('tr');
            tr.innerHTML = this.o.render(item, i, data);
            this.tbody.appendChild(tr);
        });

        this.renderPaginacion(data);
    }

    renderPaginacion(data) {
        if (!this.o.paginacion) return;
        const cont = document.getElementById(this.o.paginacion);
        if (!cont) return;
        const totalPag = data.totalPaginas || 0;
        const total = data.totalElementos || 0;
        const pagina = data.pagina || 0;
        const desde = total === 0 ? 0 : pagina * this.size + 1;
        const hasta = Math.min((pagina + 1) * this.size, total);

        cont.innerHTML =
            '<span class="pag-info">' +
            (total === 0 ? 'Sin resultados' : desde + '-' + hasta + ' de ' + total) +
            '</span>' +
            '<div class="pag-controles">' +
            '<button class="pag-boton" data-pag="0" ' + (pagina <= 0 ? 'disabled' : '') + ' aria-label="Primera pagina">&laquo;</button>' +
            '<button class="pag-boton" data-pag="' + (pagina - 1) + '" ' + (pagina <= 0 ? 'disabled' : '') + ' aria-label="Pagina anterior">&lsaquo;</button>' +
            '<button class="pag-boton" disabled>' + (pagina + 1) + ' / ' + Math.max(totalPag, 1) + '</button>' +
            '<button class="pag-boton" data-pag="' + (pagina + 1) + '" ' + (pagina >= totalPag - 1 ? 'disabled' : '') + ' aria-label="Pagina siguiente">&rsaquo;</button>' +
            '<button class="pag-boton" data-pag="' + (totalPag - 1) + '" ' + (pagina >= totalPag - 1 ? 'disabled' : '') + ' aria-label="Ultima pagina">&raquo;</button>' +
            '</div>';

        cont.querySelectorAll('button[data-pag]').forEach((b) => {
            b.addEventListener('click', () => this.cargar(parseInt(b.dataset.pag, 10)));
        });
    }
}
