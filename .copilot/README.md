# .copilot — Skills y Agentes de Calidad de Código

Esta carpeta contiene los skills y configuraciones de agentes para revisión y validación de código. Están diseñados para funcionar con **GitHub Copilot Chat** en VS Code.

---

## Contenido

```
.copilot/
├── skills/
│   ├── code-validator/     Validador determinístico de calidad de código
│   ├── judgment-day/       Revisión adversarial con dos jueces en paralelo
│   ├── branch-pr/          Workflow de creación de Pull Requests
│   └── _shared/            Protocolo de resolución de skills (uso interno)
├── knowledge/
│   ├── general/            Principios generales de programación
│   ├── stack/              Patrones específicos por lenguaje (Python, etc.)
│   └── style/              Convenciones de commits y ramas
└── agents/
    └── tech-review.md      Instrucciones del agente Tech Review
```

---

## Instalación en casa (otra máquina)

### Opción A — Instalación global (recomendada)

Copia la carpeta `.copilot/` a tu directorio de usuario para que los skills estén disponibles en todos tus proyectos:

```powershell
# Desde la raíz del repositorio clonado
Copy-Item -Recurse .copilot\skills\*   "$env:USERPROFILE\.copilot\skills\"
Copy-Item -Recurse .copilot\knowledge\ "$env:USERPROFILE\.copilot\knowledge\"
```

Luego edita `~/.copilot/skills/code-validator/SKILL.md` y reemplaza la línea:
```
Base: {KNOWLEDGE_BASE_ROOT}
```
con la ruta absoluta de tu máquina:
```
Base: C:/Users/<tu-usuario>/.copilot/knowledge/
```

### Opción B — Uso directo desde el workspace

Si vas a trabajar solo en este proyecto, los skills se pueden invocar directamente desde esta carpeta. El agente leerá los archivos de knowledge desde `.copilot/knowledge/` dentro del workspace.

---

## Cómo usar cada skill

### `code-validator`
Invócalo antes de entregar cualquier implementación no trivial (>10 líneas):
> "valida este código" / "validate this code" / "review code quality"

### `judgment-day`
Para revisión adversarial en profundidad antes de un merge importante:
> "judgment day" / "juzgar este código" / "dual review" / "que lo juzguen"

### `branch-pr`
Para preparar una rama y abrir un PR siguiendo el workflow del proyecto:
> "crea el PR" / "prepara el PR" / "open a pull request"

### Tech Review Agent
El agente `Tech Review` se activa automáticamente cuando el orquestador lo invoca para revisar código, diffs, regresiones o problemas de diseño.

---

## Agregar nuevas reglas al knowledge base

Dile al AI en cualquier momento:
> "agrega esta regla a tu knowledge base: [descripción de la regla]"

El skill `code-validator` se encargará de clasificarla y escribirla en el archivo correcto bajo `knowledge/`.
