const fs = require('fs');

function refactorFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    
    // Replace modals HTML
    // We look for <!-- MODAL DETALLE and end with the end of modalPdf
    const modalStart = content.indexOf('<!-- MODAL DETALLE (tabs: Datos | Documentos | Historial) -->');
    let modalEnd = content.indexOf('<!-- MODAL QR -->');
    if (modalEnd === -1) {
        // dashboard doesn't have modal QR
        modalEnd = content.indexOf('</main>', modalStart);
    }
    
    if (modalStart !== -1 && modalEnd !== -1) {
        const replacement = '<!-- MODALES COMPARTIDOS PARA TRASLADOS -->\n        <div th:replace="~{layouts/modals_traslado :: modales}"></div>\n';
        content = content.substring(0, modalStart) + replacement + content.substring(modalEnd);
    }

    // Replace JS block in dashboard
    if (file.includes('dashboard')) {
        const jsStart = content.indexOf('var pdfLabels =');
        const jsEnd = content.indexOf('var ESTADO_CONFIG =');
        if (jsStart !== -1 && jsEnd !== -1) {
            content = content.substring(0, jsStart) + content.substring(jsEnd);
        }
        
        // Inject script tag for traslados.js right after pageScripts block
        if (!content.includes('traslados.js')) {
            content = content.replace('<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>',
            '<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>\n<script src="/js/traslados.js"></script>');
        }
    }
    
    // Replace JS block in shipments
    if (file.includes('shipments')) {
        const jsStart = content.indexOf('var pdfLabels =');
        const jsEnd = content.indexOf('var ESTADO_CONFIG =');
        if (jsStart !== -1 && jsEnd !== -1) {
            content = content.substring(0, jsStart) + content.substring(jsEnd);
        }
        
        // Inject script tag
        if (!content.includes('traslados.js')) {
            content = content.replace('<script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"></script>',
            '<script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"></script>\n<script src="/js/traslados.js"></script>');
        }
    }

    fs.writeFileSync(file, content, 'utf-8');
    console.log(`Refactored ${file}`);
}

refactorFile('src/main/resources/templates/dashboard.html');
refactorFile('src/main/resources/templates/shipments.html');
