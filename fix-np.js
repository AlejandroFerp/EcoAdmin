const fs = require('fs');

const file = 'src/main/resources/templates/dashboard.html';
let content = fs.readFileSync(file, 'utf-8');

// The file currently has \\'/documents?trasladoId=' + a.trasladoId + '\\'
// We want to replace it with verPdf(' + a.trasladoId + ', \\'notificacion\\')

content = content.replace(/onclick="window\.location\.href=.*documents\?trasladoId.*"/g, `onclick="verPdf(' + a.trasladoId + ', \\'notificacion\\')"`);

fs.writeFileSync(file, content, 'utf-8');
console.log('Fixed NP click');
