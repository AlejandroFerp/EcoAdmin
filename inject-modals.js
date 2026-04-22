const fs = require('fs');

const shipmentsContent = fs.readFileSync('src/main/resources/templates/shipments.html', 'utf-8');

function extractSection(startTag, endTag) {
    const startIdx = shipmentsContent.indexOf(startTag);
    if (startIdx === -1) return null;
    let temp = shipmentsContent.substring(startIdx);
    const endIdx = temp.indexOf(endTag);
    if (endIdx === -1) return null;
    return temp.substring(0, endIdx + endTag.length);
}

// Extract MODAL DETALLE
const modalDetalle = extractSection('<!-- MODAL DETALLE (tabs: Datos | Documentos | Historial) -->', '</div>\r\n        </div>\r\n\r\n        <!-- MINI MODAL');
if (!modalDetalle) console.log('modalDetalle not found');

// Extract MODAL PDF
const modalPdf = extractSection('<!-- MODAL PDF -->', '</div>\r\n        </div>\r\n        <!-- MODAL DETALLE');
if (!modalPdf) console.log('modalPdf not found');

// Extract JS
const jsFunctions = extractSection('async function abrirDetalle(id) {', 'timeoutMessage: \'El PDF tardó demasiado en cargar. Puedes abrirlo o descargarlo en una pestaña nueva.\'\r\n                });\r\n            }');
if (!jsFunctions) console.log('jsFunctions not found');

if (!modalDetalle || !modalPdf || !jsFunctions) {
    console.error('Extraction failed.');
    process.exit(1);
}

// Clean up ends
let mdClean = modalDetalle.split('<!-- MINI MODAL')[0].trim();
let mpClean = modalPdf.split('<!-- MODAL DETALLE')[0].trim();

const pdfLabels = "var pdfLabels = { 'carta-porte': 'Carta de Porte', 'notificacion': 'Notificacion', 'certificado': 'Certificado' };\nvar mDetalle;\nvar mPdf;\nvar detalleIdActual = null;";

const dashboardPath = 'src/main/resources/templates/dashboard.html';
let dashboardContent = fs.readFileSync(dashboardPath, 'utf-8');

if (!dashboardContent.includes('id="modalDetalle"')) {
    dashboardContent = dashboardContent.replace('</main>', `\n${mdClean}\n\n${mpClean}\n</main>`);
}

if (!dashboardContent.includes('function abrirDetalle')) {
    dashboardContent = dashboardContent.replace('<script>', `<script>\n${pdfLabels}\n\n${jsFunctions}\n\nfunction initDashboardModales() { mDetalle = { show: function() { document.getElementById('modalDetalle').classList.remove('hidden'); }, hide: function() { document.getElementById('modalDetalle').classList.add('hidden'); } }; mPdf = { show: function() { document.getElementById('modalPdf').classList.remove('hidden'); }, hide: function() { document.getElementById('modalPdf').classList.add('hidden'); } }; }\ndocument.addEventListener('DOMContentLoaded', initDashboardModales);\n`);
}

// In case the previous replace missed, because it's a literal \' in the HTML
dashboardContent = dashboardContent.replace(/ondblclick="window\.location\.href='\/shipments\?id=' \+ t\.id \+ '"/g, `ondblclick="abrirDetalle(' + t.id + ')"`);
// Try another regex if the single quote is not escaped
dashboardContent = dashboardContent.replace(/ondblclick="window\.location\.href=\\'\/shipments\?id=' \+ t\.id \+ '\\'"/g, `ondblclick="abrirDetalle(' + t.id + ')"`);
dashboardContent = dashboardContent.replace(/ondblclick="window\.location\.href=.*shipments\?id.*"/g, `ondblclick="abrirDetalle(' + t.id + ')"`);

fs.writeFileSync(dashboardPath, dashboardContent, 'utf-8');
console.log('Successfully injected modals');
