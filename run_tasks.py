import pathlib, re

# TASK 1
controller = pathlib.Path("src/main/java/com/iesdoctorbalmis/spring/controladores/ZonaPublicaController.java")
text = controller.read_text(encoding="utf-8")

# Add import if not present
import_line = "import org.springframework.web.bind.annotation.PathVariable;\n"
if "PathVariable" not in text:
    # Insert after last import line
    last_import = max(m.end() for m in re.finditer(r'^import .*;$', text, re.MULTILINE))
    text = text[:last_import] + "\n" + import_line + text[last_import:]

# Add method before closing brace of class
new_method = '''
    @GetMapping("/usuarios/{id}")
    public String usuarioPerfil(@PathVariable Long id, org.springframework.ui.Model model) {
        model.addAttribute("usuarioId", id);
        return "usuario-perfil";
    }
'''

# Find the last closing brace
last_brace = text.rfind("}")
text = text[:last_brace] + new_method + "\n}" + text[last_brace+1:]

controller.write_text(text, encoding="utf-8")
print("OK task1")

# TASK 2
template_content = r"""<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org"
    th:replace="~{layouts/main :: layout('users', 'EcoAdmin — Perfil Transportista', 'Perfil', ~{::pageContent}, ~{::pageScripts})}">
<body>

<th:block th:fragment="pageContent">
<main class="page-main" th:attr="data-uid=${usuarioId}">

  <div class="page-header">
    <div>
      <a th:href="@{/users}" class="text-sm text-slate-400 hover:text-slate-600 flex items-center gap-1 mb-1">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/></svg>
        Volver a Usuarios
      </a>
      <h2 class="page-title">Perfil del transportista</h2>
      <p id="pSubtitulo" class="page-subtitle">Cargando...</p>
    </div>
  </div>

  <!-- User info card -->
  <div class="card p-6 mb-6">
    <h3 class="font-semibold text-slate-700 mb-4">Datos del usuario</h3>
    <div class="grid grid-cols-2 gap-4 text-sm" id="userInfo">
      <p class="text-slate-400">Cargando datos...</p>
    </div>
  </div>

  <!-- Perfil transportista (solo visible si rol == TRANSPORTISTA) -->
  <div id="bloqueTransportista" class="hidden">
    <div class="card p-6 mb-6">
      <h3 class="font-semibold text-slate-700 mb-4">Datos del transportista</h3>
      <div class="space-y-4">
        <div>
          <label class="label-eco">Matricula del vehiculo</label>
          <input type="text" id="fMatricula" class="form-control-eco" placeholder="1234 ABC">
        </div>
        <div>
          <label class="label-eco">Formula de tarifa</label>
          <p class="text-xs text-slate-400 mb-2">Variables: <code class="bg-slate-100 px-1 rounded">w</code> = peso (kg), <code class="bg-slate-100 px-1 rounded">L</code> = distancia (km). Ejemplo: <code class="bg-slate-100 px-1 rounded">w * 0.5 + L * 0.1</code></p>
          <input type="text" id="fFormula" class="form-control-eco font-mono" placeholder="w * 0.5 + L * 0.1">
          <!-- Builder buttons -->
          <div class="flex flex-wrap gap-1 mt-2">
            <button type="button" class="btn-builder" onclick="insertarFormula('w')">w</button>
            <button type="button" class="btn-builder" onclick="insertarFormula('L')">L</button>
            <button type="button" class="btn-builder btn-builder--op" onclick="insertarFormula('+')">+</button>
            <button type="button" class="btn-builder btn-builder--op" onclick="insertarFormula('-')">−</button>
            <button type="button" class="btn-builder btn-builder--op" onclick="insertarFormula('*')">×</button>
            <button type="button" class="btn-builder btn-builder--op" onclick="insertarFormula('/')">÷</button>
            <button type="button" class="btn-builder" onclick="insertarFormula('(')"> ( </button>
            <button type="button" class="btn-builder" onclick="insertarFormula(')')"> ) </button>
            <button type="button" class="btn-builder" onclick="insertarFormula('.')">.</button>
            <span class="text-slate-300 self-center mx-1">|</span>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('0')">0</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('1')">1</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('2')">2</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('3')">3</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('4')">4</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('5')">5</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('6')">6</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('7')">7</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('8')">8</button>
            <button type="button" class="btn-builder btn-builder--num" onclick="insertarFormula('9')">9</button>
            <span class="text-slate-300 self-center mx-1">|</span>
            <button type="button" class="btn-builder btn-builder--del" onclick="borrarUltimoFormula()">&#9003;</button>
            <button type="button" class="btn-builder btn-builder--del" onclick="limpiarFormula()">C</button>
          </div>
          <p id="formulaError" class="text-xs text-red-500 mt-1 hidden"></p>
        </div>
        <div>
          <label class="label-eco">Unidad tarifaria</label>
          <input type="text" id="fUnidad" class="form-control-eco" placeholder="EUR/operacion">
        </div>
        <div>
          <label class="label-eco">Observaciones</label>
          <textarea id="fObservaciones" class="form-control-eco" rows="3" placeholder="Notas adicionales sobre el transportista..."></textarea>
        </div>
        <div class="flex justify-end gap-3 pt-2">
          <button type="button" onclick="guardarPerfil()" class="btn-primary">Guardar perfil</button>
        </div>
      </div>
    </div>

    <!-- Calculadora de tarifa -->
    <div class="card p-6">
      <h3 class="font-semibold text-slate-700 mb-4">Calcular tarifa</h3>
      <div class="grid grid-cols-2 gap-4 items-end">
        <div>
          <label class="label-eco">Peso (kg)</label>
          <input type="number" id="cPeso" class="form-control-eco" placeholder="100" min="0" step="0.1" oninput="calcularPreview()">
        </div>
        <div>
          <label class="label-eco">Distancia (km)</label>
          <input type="number" id="cDistancia" class="form-control-eco" placeholder="50" min="0" step="0.1" oninput="calcularPreview()">
        </div>
      </div>
      <div id="resultadoCalculo" class="mt-4 p-4 bg-slate-50 rounded-lg text-sm text-slate-500">
        Introduce peso y distancia para calcular.
      </div>
    </div>
  </div>

  <!-- No es transportista -->
  <div id="bloqueNoTransportista" class="hidden">
    <div class="card p-6 text-center text-slate-400">
      <svg class="w-12 h-12 mx-auto mb-3 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
      <p>Este usuario no tiene rol <strong>TRANSPORTISTA</strong>.</p>
      <p class="text-xs mt-1">El perfil de transportista solo esta disponible para usuarios con ese rol.</p>
    </div>
  </div>

</main>
</th:block>

<th:block th:fragment="pageScripts">
<style>
  .btn-builder {
    @apply px-2 py-1 text-sm rounded border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-mono;
  }
  .btn-builder--op { @apply bg-blue-50 border-blue-200 text-blue-700 hover:bg-blue-100; }
  .btn-builder--num { @apply bg-slate-50; }
  .btn-builder--del { @apply bg-red-50 border-red-200 text-red-600 hover:bg-red-100; }
</style>
<script>
var usuarioId = document.querySelector('.page-main').dataset.uid;
var perfilActual = null;
var calcTimer = null;

document.addEventListener('DOMContentLoaded', function() { init(); });

async function init() {
  try {
    var res = await fetch('/api/usuarios/' + usuarioId);
    if (!res.ok) { mostrarError('No se encontro el usuario.'); return; }
    var u = await res.json();
    renderUserInfo(u);

    if (u.rol === 'TRANSPORTISTA') {
      document.getElementById('bloqueTransportista').classList.remove('hidden');
      document.getElementById('bloqueNoTransportista').classList.add('hidden');
      await cargarPerfil();
    } else {
      document.getElementById('bloqueTransportista').classList.add('hidden');
      document.getElementById('bloqueNoTransportista').classList.remove('hidden');
    }
  } catch (e) {
    mostrarError('Error al cargar datos: ' + e.message);
  }
}

function renderUserInfo(u) {
  document.getElementById('pSubtitulo').textContent = (u.nombre || u.email || 'ID ' + usuarioId);
  var roles = { ADMIN: '<span class="badge badge-sm bg-red-100 text-red-700">ADMIN</span>',
                GESTOR: '<span class="badge badge-sm bg-purple-100 text-purple-700">GESTOR</span>',
                TRANSPORTISTA: '<span class="badge badge-sm bg-blue-100 text-blue-700">TRANSPORTISTA</span>',
                PRODUCTOR: '<span class="badge badge-sm bg-green-100 text-green-700">PRODUCTOR</span>' };
  document.getElementById('userInfo').innerHTML =
    '<div><p class="text-slate-400 text-xs uppercase mb-0.5">Nombre</p><p class="font-medium">' + esc(u.nombre || '—') + '</p></div>' +
    '<div><p class="text-slate-400 text-xs uppercase mb-0.5">Email</p><p>' + esc(u.email || '—') + '</p></div>' +
    '<div><p class="text-slate-400 text-xs uppercase mb-0.5">Rol</p><p>' + (roles[u.rol] || esc(u.rol || '—')) + '</p></div>' +
    '<div><p class="text-slate-400 text-xs uppercase mb-0.5">Alta</p><p>' + (u.fechaAlta ? u.fechaAlta.substring(0,10) : '—') + '</p></div>';
}

async function cargarPerfil() {
  var res = await fetch('/api/usuarios/' + usuarioId + '/perfil-transportista');
  if (res.status === 404) { perfilActual = null; return; }
  if (!res.ok) return;
  perfilActual = await res.json();
  document.getElementById('fMatricula').value = perfilActual.matricula || '';
  document.getElementById('fFormula').value = perfilActual.formulaTarifa || '';
  document.getElementById('fUnidad').value = perfilActual.unidadTarifa || '';
  document.getElementById('fObservaciones').value = perfilActual.observaciones || '';
}

async function guardarPerfil() {
  var formula = document.getElementById('fFormula').value.trim();
  if (formula) {
    var ok = await validarFormula(formula);
    if (!ok) return;
  }
  var body = {
    matricula: document.getElementById('fMatricula').value.trim(),
    formulaTarifa: formula,
    unidadTarifa: document.getElementById('fUnidad').value.trim(),
    observaciones: document.getElementById('fObservaciones').value.trim()
  };
  var res = await fetch('/api/usuarios/' + usuarioId + '/perfil-transportista', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (res.ok) {
    perfilActual = await res.json();
    if (window.Swal) Swal.fire({ icon: 'success', title: 'Guardado', timer: 1500, showConfirmButton: false });
  } else if (res.status === 403) {
    if (window.Swal) Swal.fire({ icon: 'error', title: 'Sin permiso', text: 'No tiene permiso para editar este perfil.' });
  } else {
    var err = await res.text();
    if (window.Swal) Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo guardar el perfil.' });
  }
}

async function validarFormula(f) {
  var errEl = document.getElementById('formulaError');
  var res = await fetch('/api/usuarios/' + usuarioId + '/perfil-transportista', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ matricula: '', formulaTarifa: f, unidadTarifa: '', observaciones: '' })
  });
  if (!res.ok) {
    errEl.textContent = 'Formula invalida. Revisa los operadores y variables.';
    errEl.classList.remove('hidden');
    return false;
  }
  errEl.classList.add('hidden');
  return true;
}

function insertarFormula(val) {
  var el = document.getElementById('fFormula');
  var pos = el.selectionStart;
  var before = el.value.substring(0, pos);
  var after = el.value.substring(el.selectionEnd);
  var sep = (before.length > 0 && !before.endsWith(' ') && val !== ')' && before.slice(-1) !== '(') ? ' ' : '';
  el.value = before + sep + val + after;
  el.focus();
  el.selectionStart = el.selectionEnd = pos + sep.length + val.length;
}

function borrarUltimoFormula() {
  var el = document.getElementById('fFormula');
  var pos = el.selectionStart;
  if (pos === 0) return;
  el.value = el.value.substring(0, pos - 1) + el.value.substring(el.selectionEnd);
  el.focus();
  el.selectionStart = el.selectionEnd = pos - 1;
}

function limpiarFormula() {
  document.getElementById('fFormula').value = '';
  document.getElementById('formulaError').classList.add('hidden');
}

function calcularPreview() {
  clearTimeout(calcTimer);
  calcTimer = setTimeout(async function() {
    var w = parseFloat(document.getElementById('cPeso').value);
    var L = parseFloat(document.getElementById('cDistancia').value);
    var el = document.getElementById('resultadoCalculo');
    if (isNaN(w) || isNaN(L)) {
      el.innerHTML = '<span class="text-slate-400">Introduce peso y distancia para calcular.</span>';
      return;
    }
    var formula = document.getElementById('fFormula').value.trim();
    if (!formula) {
      el.innerHTML = '<span class="text-slate-400">Define primero una formula de tarifa.</span>';
      return;
    }
    try {
      var res = await fetch('/api/transportistas/' + usuarioId + '/calcular-tarifa?w=' + w + '&L=' + L);
      if (!res.ok) {
        var e = await res.json();
        el.innerHTML = '<span class="text-red-500">' + esc(e.error || 'Error al calcular') + '</span>';
        return;
      }
      var data = await res.json();
      el.innerHTML = '<span class="text-slate-600">Formula: <code class="font-mono bg-slate-100 px-1 rounded">' + esc(data.formula) + '</code></span><br>' +
        '<span class="text-2xl font-bold text-emerald-600">' + data.resultado + ' ' + data.moneda + '</span>' +
        '<span class="text-slate-400 text-xs ml-2">con w=' + data.w + 'kg, L=' + data.L + 'km</span>';
    } catch(err) {
      el.innerHTML = '<span class="text-red-400">Error de conexion</span>';
    }
  }, 500);
}

function mostrarError(msg) {
  document.getElementById('pSubtitulo').textContent = msg;
}

function esc(s) {
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
</script>
</th:block>

</body>
</html>"""

out = pathlib.Path("src/main/resources/templates/usuario-perfil.html")
out.write_text(template_content, encoding="utf-8")
print("OK task2")
