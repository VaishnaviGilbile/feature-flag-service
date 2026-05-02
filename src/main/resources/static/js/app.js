const API = '/api/v1/flags';
let flags = [];
let editMode = false;

async function loadFlags() {
    try {
        const res = await fetch(API);
        flags = await res.json();
        renderTable();
        renderStats();
    } catch (e) {
        document.getElementById('flags-body').innerHTML =
            '<tr><td colspan="5" class="empty-row">Could not connect to API</td></tr>';
    }
}

function renderStats() {
    const enabled = flags.filter(f => f.enabled).length;
    const avg = flags.length
        ? Math.round(flags.reduce((s, f) => s + f.rolloutPercentage, 0) / flags.length)
        : 0;
    document.getElementById('stat-total').textContent    = flags.length;
    document.getElementById('stat-enabled').textContent  = enabled;
    document.getElementById('stat-disabled').textContent = flags.length - enabled;
    document.getElementById('stat-rollout').textContent  = avg + '%';
}

function envTagClass(env) {
    const e = env.toLowerCase();
    if (e === 'production' || e === 'prod') return 'prod';
    if (e === 'staging')                    return 'staging';
    if (e === 'dev' || e === 'development') return 'dev';
    return 'default';
}

function renderTable() {
    const tbody = document.getElementById('flags-body');
    if (!flags.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-row">No flags yet — create one!</td></tr>';
        return;
    }
    tbody.innerHTML = flags.map(f => `
    <tr>
      <td>
        <div class="flag-key">${f.key}</div>
        <div class="flag-name">${f.name}</div>
      </td>
      <td>
        <label class="toggle">
          <input type="checkbox" ${f.enabled ? 'checked' : ''}
            onchange="toggleFlag('${f.key}', this.checked)"/>
          <span class="slider"></span>
        </label>
      </td>
      <td>
        <div class="rollout-wrap">
          <div class="rollout-track">
            <div class="rollout-fill" style="width:${f.rolloutPercentage}%"></div>
          </div>
          <span class="rollout-pct">${f.rolloutPercentage}%</span>
        </div>
      </td>
      <td>
        ${f.environments
        ? f.environments.split(',').map(e =>
            `<span class="tag ${envTagClass(e.trim())}">${e.trim()}</span>`
        ).join('')
        : '<span style="color:var(--muted);font-size:11px">all</span>'}
      </td>
      <td>
        <div class="actions">
          <button class="btn btn-ghost btn-sm" onclick='openEdit(${JSON.stringify(f)})'>Edit</button>
          <button class="btn btn-del btn-sm" onclick="deleteFlag('${f.key}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

async function toggleFlag(key, enabled) {
    await fetch(`${API}/${key}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ enabled })
    });
    showToast(enabled ? `${key} enabled` : `${key} disabled`);
    loadFlags();
}

function openCreate() {
    editMode = false;
    document.getElementById('modal-title').textContent = 'New Flag';
    document.getElementById('f-key').value = '';
    document.getElementById('f-key').disabled = false;
    document.getElementById('f-name').value = '';
    document.getElementById('f-desc').value = '';
    document.getElementById('f-rollout').value = 100;
    document.getElementById('f-envs').value = '';
    document.getElementById('f-allowlist').value = '';
    document.getElementById('f-enabled').checked = false;
    document.getElementById('modal').classList.add('open');
}

function openEdit(f) {
    editMode = true;
    document.getElementById('modal-title').textContent = 'Edit Flag';
    document.getElementById('f-key').value = f.key;
    document.getElementById('f-key').disabled = true;
    document.getElementById('edit-key').value = f.key;
    document.getElementById('f-name').value = f.name || '';
    document.getElementById('f-desc').value = f.description || '';
    document.getElementById('f-rollout').value = f.rolloutPercentage;
    document.getElementById('f-envs').value = f.environments || '';
    document.getElementById('f-allowlist').value = f.allowlist || '';
    document.getElementById('f-enabled').checked = f.enabled;
    document.getElementById('modal').classList.add('open');
}

function closeModal() {
    document.getElementById('modal').classList.remove('open');
}

async function saveFlag() {
    const key = editMode
        ? document.getElementById('edit-key').value
        : document.getElementById('f-key').value;

    const body = {
        key,
        name: document.getElementById('f-name').value,
        description: document.getElementById('f-desc').value,
        rolloutPercentage: parseInt(document.getElementById('f-rollout').value),
        environments: document.getElementById('f-envs').value || null,
        allowlist: document.getElementById('f-allowlist').value || null,
        enabled: document.getElementById('f-enabled').checked,
    };

    try {
        const res = await fetch(editMode ? `${API}/${key}` : API, {
            method: editMode ? 'PATCH' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) { const e = await res.json(); showToast(e.message, true); return; }
        closeModal();
        showToast(editMode ? 'Flag updated' : 'Flag created');
        loadFlags();
    } catch (e) {
        showToast('Request failed', true);
    }
}

async function deleteFlag(key) {
    if (!confirm(`Delete "${key}"?`)) return;
    await fetch(`${API}/${key}`, { method: 'DELETE' });
    showToast(`${key} deleted`);
    loadFlags();
}

async function evaluateFlag() {
    const key = document.getElementById('eval-key').value.trim();
    const userId = document.getElementById('eval-user').value.trim();
    const environment = document.getElementById('eval-env').value.trim();

    if (!key || !userId) { showToast('Flag key and User ID required', true); return; }

    const res = await fetch(`${API}/${key}/evaluate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, environment: environment || undefined })
    });
    const data = await res.json();
    const el = document.getElementById('eval-result');
    el.style.display = 'block';
    el.className = `eval-result ${data.enabled ? 'on' : 'off'}`;
    el.innerHTML = [
        `FLAG &nbsp;: ${data.flagKey}`,
        `USER &nbsp;: ${data.userId}`,
        `ENV &nbsp; : ${data.environment || 'any'}`,
        `──────────────────────────`,
        `RESULT: ${data.enabled ? '✓ ENABLED' : '✗ DISABLED'}`,
        `REASON: ${data.reason}`
    ].join('<br>');
}

function showToast(msg, isError = false) {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.style.background = isError ? '#dc2626' : '#2563eb';
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 2500);
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('modal').addEventListener('click', e => {
        if (e.target === document.getElementById('modal')) closeModal();
    });
    loadFlags();
});