const fs = require('fs');
const file = 'src/main/resources/templates/dashboard.html';
let content = fs.readFileSync(file, 'utf-8');

// The file currently has \\'/shipments?id=...\\' and \\'/documents?trasladoId=...\\'
// We need to replace it with \\'/shipments...\\' where there is only one backslash.
content = content.replace(/\\\\'\/shipments\?id=/g, "\\'/shipments?id=");
content = content.replace(/\\\\'"/g, "\\'\"");
content = content.replace(/\\\\'\/documents\?trasladoId=/g, "\\'/documents?trasladoId=");

fs.writeFileSync(file, content, 'utf-8');
console.log('Fixed JS syntax errors in dashboard');
