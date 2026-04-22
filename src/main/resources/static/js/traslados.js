var pdfLabels = { 'carta-porte': 'Carta de Porte', 'notificacion': 'Notificacion', 'certificado': 'Certificado' };
var mDetalle;
var mPdf;
var detalleIdActual = null;

document.addEventListener('DOMContentLoaded', function() {
    mDetalle = { 
        show: function() { document.getElementById('modalDetalle').classList.remove('hidden'); }, 
        hide: function() { document.getElementById('modalDetalle').classList.add('hidden'); } 
    }; 
    mPdf = { 
        show: function() { document.getElementById('modalPdf').classList.remove('hidden'); }, 
        hide: function() { document.getElementById('modalPdf').classList.add('hidden'); } 
    }; 

    if (typeof registerIframePreviewModal === 'function') {
        registerIframePreviewModal('modalPdf', {
            iframeId: 'pdfFrame',
            loaderId: 'pdfLoader',
            errorBoxId: 'pdfError',
            errorMessageSelector: '#pdfErrorMsg',
            defaultErrorMessage: 'No se pudo cargar el PDF.',
            timeoutMs: 6000
        });
    }
});

async function abrirDetalle(id) {
    detalleIdActual = id;
    document.getElementById('detalleTitulo').textContent = 'Traslado #' + id;
    cambiarTabDetalle('datos');
    mDetalle && mDetalle.show();
    // cargar datos
    try {
        var t = await fetch('/api/traslados/' + id).then(function (r) { return r.json(); });
        var prod = (t.centroProductor && t.centroProductor.nombre) || '—';
        var gest = (t.centroGestor && t.centroGestor.nombre) || '—';
        var res = (t.residuo && (t.residuo.descripcion || t.residuo.codigoLER)) || '—';
        var transp = (t.transportista && t.transportista.nombre) || '—';
        document.getElementById('detalleInfo').innerHTML =
            '<div><dt class="text-xs text-slate-400 mb-0.5">Estado</dt><dd>' + (window.estadoBadge ? window.estadoBadge(t.estado) : esc(t.estado)) + '</dd></div>'
            + '<div><dt class="text-xs text-slate-400 mb-0.5">Residuo</dt><dd class="font-medium">' + esc(res) + '</dd></div>'
            + '<div><dt class="text-xs text-slate-400 mb-0.5">Productor</dt><dd>' + esc(prod) + '</dd></div>'
            + '<div><dt class="text-xs text-slate-400 mb-0.5">Gestor</dt><dd>' + esc(gest) + '</dd></div>'
            + '<div><dt class="text-xs text-slate-400 mb-0.5">Transportista</dt><dd>' + esc(transp) + '</dd></div>'
            + (t.observaciones ? '<div class="col-span-2"><dt class="text-xs text-slate-400 mb-0.5">Observaciones</dt><dd class="text-slate-600">' + esc(t.observaciones) + '</dd></div>' : '');
    } catch (e) {
        document.getElementById('detalleInfo').innerHTML = '<div class="col-span-2 text-red-400">Error al cargar.</div>';
    }
}

function cambiarTabDetalle(tab) {
    var panels = { datos: 'panelDatos', documentos: 'panelDocumentos', historial: 'panelHistorial' };
    var btns = { datos: 'tabDatos', documentos: 'tabDocumentos', historial: 'tabHistorial' };
    Object.keys(panels).forEach(function (k) {
        var panel = document.getElementById(panels[k]);
        var btn = document.getElementById(btns[k]);
        if (k === tab) {
            panel.classList.remove('hidden');
            btn.classList.add('modal-tab--active');
        } else {
            panel.classList.add('hidden');
            btn.classList.remove('modal-tab--active');
        }
    });
    if (tab === 'historial' && detalleIdActual) {
        cargarHistorialDetalle(detalleIdActual);
    }
}

async function cargarHistorialDetalle(id) {
    var panel = document.getElementById('panelHistorial');
    panel.innerHTML = '<p class="text-slate-400">Cargando...</p>';
    try {
        var eventos = await fetch('/api/traslados/' + id + '/historial').then(function (r) { return r.json(); });
        if (!eventos || !eventos.length) { panel.innerHTML = '<p class="text-slate-400">Sin eventos.</p>'; return; }
        var colors = { PENDIENTE: '#f59e0b', EN_TRANSITO: '#3b82f6', ENTREGADO: '#6366f1', COMPLETADO: '#10b981', CANCELADO: '#f87171' };
        panel.innerHTML = '<ol class="relative border-l border-slate-200 ml-3 space-y-4">'
            + eventos.map(function (e, i) {
                var estadoNuevo = e.estadoNuevo || e.estado || '';
                var estadoAnt = e.estadoAnterior || '';
                var color = colors[estadoNuevo] || '#94a3b8';
                var fecha = e.fecha || e.fechaEvento;
                var usr = (e.usuario && (e.usuario.nombre || e.usuario.email)) || '';
                var badgeFn = window.estadoBadge || function(es) { return esc(es); };
                var transicion = estadoAnt
                    ? '<span class="text-[11px] text-slate-400">' + esc(estadoAnt) + '</span><span class="text-slate-300">→</span>' + badgeFn(estadoNuevo)
                    : badgeFn(estadoNuevo);
                return '<li class="ml-6">'
                    + '<span class="absolute -left-[7px] flex h-3.5 w-3.5 items-center justify-center rounded-full ring-2 ring-white" style="background:' + color + '"></span>'
                    + '<div class="bg-slate-50 border border-slate-100 rounded-xl px-4 py-3">'
                    + '<div class="flex items-center justify-between gap-2 flex-wrap">'
                    + '<div class="flex items-center gap-1.5 flex-wrap">' + transicion + '</div>'
                    + '<time class="text-[11px] text-slate-400">' + (fecha ? new Date(fecha).toLocaleString('es-ES') : '') + '</time>'
                    + '</div>'
                    + '<p class="text-sm text-slate-600 mt-2">' + (e.comentario ? esc(e.comentario) : '<span class="italic text-slate-400">Sin comentario</span>') + '</p>'
                    + (usr ? '<p class="text-[11px] text-slate-400 mt-1">por ' + esc(usr) + '</p>' : '')
                    + '</div></li>';
            }).join('')
            + '</ol>';
    } catch (e) {
        panel.innerHTML = '<p class="text-red-400">Error al cargar historial.</p>';
    }
}

function verPdfDetalle(tipo) {
    if (!detalleIdActual) return;
    mDetalle && mDetalle.hide();
    setTimeout(function () { verPdf(detalleIdActual, tipo); }, 150);
}

/**
 * @brief Abre el modal de documentos y carga el visor nativo de PDF en un iframe.
 * @param id Identificador del traslado.
 * @param tipo Tipo de documento solicitado.
 */
function verPdf(id, tipo) {
    var label = pdfLabels[tipo] || tipo;
    document.getElementById('pdfTitulo').textContent = label + ' \u2014 Traslado #' + id;

    if (typeof openIframePreviewModal !== 'function') {
        window.open('/api/traslados/' + id + '/pdf/' + tipo + '?inline=true', '_blank', 'noopener');
        return;
    }

    openIframePreviewModal('modalPdf', '/api/traslados/' + id + '/pdf/' + tipo + '?inline=true', {
        timeoutMessage: 'El PDF tardó demasiado en cargar. Puedes abrirlo o descargarlo en una pestaña nueva.'
    });
}
