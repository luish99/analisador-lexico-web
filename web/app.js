const $ = (id) => document.getElementById(id);
const source = $('source');

function updateEditor() {
  $('char-count').textContent = `${source.value.length} caracteres`;
  $('lines').textContent = Array.from({length: source.value.split('\n').length}, (_, i) => i + 1).join('\n');
}
source.addEventListener('input', updateEditor);
source.addEventListener('scroll', () => { $('lines').scrollTop = source.scrollTop; });
$('clear').addEventListener('click', () => { source.value = ''; $('file').value = ''; updateEditor(); source.focus(); });
$('file').addEventListener('change', async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  if (file.size > 1024 * 1024) { alert('Escolha um arquivo de até 1 MB.'); return; }
  source.value = await file.text();
  updateEditor();
  source.focus();
});
source.addEventListener('keydown', (event) => {
  if (event.ctrlKey && event.key === 'Enter') { event.preventDefault(); analyze(); }
});
$('analyze').addEventListener('click', analyze);

function cell(row, value, code = false) {
  const td = document.createElement('td');
  if (code) { const c = document.createElement('code'); c.textContent = value; td.append(c); }
  else td.textContent = value;
  row.append(td);
}

async function analyze() {
  const button = $('analyze');
  button.disabled = true;
  try {
    const response = await fetch('/api/analisar', {method:'POST', headers:{'Content-Type':'text/plain; charset=utf-8'}, body:source.value});
    if (!response.ok) throw new Error(await response.text());
    const result = await response.json();
    $('token-count').textContent = result.tokens.length;
    $('step-count').textContent = result.steps.length;
    $('final-state').textContent = result.error ? 'Erro' : 'Aceito';
    $('status').textContent = result.error ? 'Erro léxico' : 'Análise concluída';
    $('status').className = `status ${result.error ? 'failure' : 'success'}`;
    $('error-box').hidden = !result.error;
    if (result.error) $('error-box').textContent = `${result.error.message} em linha ${result.error.line}, coluna ${result.error.column} (símbolo: ${result.error.symbol}). A análise foi interrompida.`;
    const tokenBody = $('tokens'); tokenBody.replaceChildren();
    result.tokens.forEach((token, i) => {
      const tr = document.createElement('tr');
      cell(tr, String(i + 1)); cell(tr, token.lexeme, true); cell(tr, token.type); cell(tr, `${token.line}:${token.column}`);
      tokenBody.append(tr);
    });
    if (!result.tokens.length) tokenBody.innerHTML = '<tr><td colspan="4" class="empty">Nenhum token reconhecido.</td></tr>';
    const steps = $('steps'); steps.replaceChildren();
    result.steps.forEach((step, i) => {
      const item = document.createElement('div'); item.className = 'step';
      const number = document.createElement('div'); number.className = 'step-index'; number.textContent = String(i + 1).padStart(2, '0');
      const main = document.createElement('div'); main.className = 'step-main';
      const title = document.createElement('div');
      const symbol = document.createElement('b'); symbol.className = 'step-symbol'; symbol.textContent = step.symbol;
      title.append('Símbolo ', symbol, ` · ${step.from} → ${step.to}`);
      const meta = document.createElement('div'); meta.className = 'step-meta'; meta.textContent = `${step.action} · linha ${step.line}, coluna ${step.column}`;
      main.append(title, meta); item.append(number, main); steps.append(item);
    });
    if (!result.steps.length) steps.innerHTML = '<p class="empty">Entrada vazia.</p>';
  } catch (error) {
    $('status').textContent = 'Falha na análise'; $('status').className = 'status failure';
    $('error-box').hidden = false; $('error-box').textContent = `Não foi possível analisar: ${error.message}`;
  } finally { button.disabled = false; }
}

async function loadTransitions() {
  try {
    const response = await fetch('/api/transicoes');
    const rows = await response.json();
    $('transition-count').textContent = `${rows.length} transições`;
    const body = $('transitions'); body.replaceChildren();
    rows.forEach((transition) => {
      const tr = document.createElement('tr');
      cell(tr, transition.from, true); cell(tr, transition.symbol, true); cell(tr, transition.to, true);
      body.append(tr);
    });
  } catch { $('transition-count').textContent = 'indisponível'; }
}

source.value = '1 0 10 123\n0.5 12.34';
updateEditor();
loadTransitions();
