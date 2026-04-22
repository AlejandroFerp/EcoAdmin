const fs = require('fs');

function refactorCrud(file) {
    if (!fs.existsSync(file)) return;
    let content = fs.readFileSync(file, 'utf-8');

    // Regex to match the entire modalForm structure
    // We need to capture:
    // 1. size (e.g. sm, md) -> modal-shell--([a-z]+)
    // 2. title -> <h3[^>]*>([^<]+)</h3>
    // 3. body inner HTML -> <div class="modal-body[^>]*>([\s\S]*?)</div>\s*<div th:replace="~\{layouts/fragments :: modalFooter
    // 4. save action -> modalFooter\('[^']+',\s*'[^']+',\s*'([^']+)'
    
    const regex = /<div id="modalForm"[\s\S]*?modal-shell--([a-z]+)[\s\S]*?<h3[^>]*>([^<]+)<\/h3>[\s\S]*?<div class="modal-body[^>]*>([\s\S]*?)<\/div>\s*<div th:replace="~\{layouts\/fragments :: modalFooter\([^,]+,\s*'[^']+',\s*'([^']+)'\)\}"[^>]*><\/div>\s*<\/div>\s*<\/div>\s*<\/div>/;
    
    const match = content.match(regex);
    if (match) {
        const fullMatch = match[0];
        const size = match[1];
        const title = match[2];
        const bodyInner = match[3];
        const action = match[4];

        const replacement = `<div th:replace="~{layouts/fragments :: crudModal('modalForm', '${size}', '${title}', '${action}', ~{::modalBody})}">\n    <th:block th:fragment="modalBody">\n        ${bodyInner.trim()}\n    </th:block>\n</div>`;
        
        content = content.replace(fullMatch, replacement);
        fs.writeFileSync(file, content, 'utf-8');
        console.log(`Refactored ${file}`);
    } else {
        console.log(`No match found in ${file}`);
    }
}

const files = [
    'src/main/resources/templates/users.html',
    'src/main/resources/templates/centers.html',
    'src/main/resources/templates/waste.html',
    'src/main/resources/templates/addresses.html',
    'src/main/resources/templates/documents.html'
];

files.forEach(refactorCrud);
