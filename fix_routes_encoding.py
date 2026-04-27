import sys
import os

path = 'src/main/resources/templates/routes.html'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

replacements = {
    'Gestin': 'Gestión',
    'gestin': 'gestión',
    'Aadir': 'Añadir',
    'aadir': 'añadir',
    'Edtalas': 'Edítalas',
    'Frmula': 'Fórmula',
    'frmula': 'fórmula',
    'Direccin': 'Dirección',
    'accin': 'acción',
    'pgina': 'página',
    'Eliminar': '¿Eliminar',
    'Maana': 'Mañana',
    '1': '1—',
    '><': '>—<',
    "''": "'—'",
    '><': '>—<',
    'w  Peso': 'w · Peso',
    'L  Distancia': 'L · Distancia',
    "' ? '": "' → '",
    " ? ": " → ",
    "": "—"
}

for k, v in replacements.items():
    text = text.replace(k, v)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
print("File routes.html fixed.")
