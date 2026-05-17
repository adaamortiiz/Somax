requireAuth();

const statsContainer = document.getElementById('adminStats');
const usersTable = document.getElementById('usersTable');
const classForm = document.getElementById('classForm');
const scheduleForm = document.getElementById('scheduleForm');
const classSelect = document.getElementById('scheduleClase');
const monitorSelect = document.getElementById('scheduleMonitor');
const exportCsv = document.getElementById('exportCsv');
const exportPdf = document.getElementById('exportPdf');
const classListAdmin = document.getElementById('classListAdmin');
const scheduleListAdmin = document.getElementById('scheduleListAdmin');
const classFormMessage = document.getElementById('classFormMessage');
const scheduleFormMessage = document.getElementById('scheduleFormMessage');

async function loadDashboard() {
  if (!statsContainer) {
    return;
  }
  const response = await authFetch('/api/admin/dashboard');
  if (!response.ok) {
    return;
  }
  const data = await response.json();
  statsContainer.innerHTML = `
        <div class="row g-3">
            <div class="col-md-3"><div class="stat-card"><h6>Total usuarios</h6><h3>${data.totalUsuarios}</h3></div></div>
            <div class="col-md-3"><div class="stat-card"><h6>Usuarios activos</h6><h3>${data.usuariosActivos}</h3></div></div>
            <div class="col-md-3"><div class="stat-card"><h6>Clases</h6><h3>${data.totalClases}</h3></div></div>
            <div class="col-md-3"><div class="stat-card"><h6>Ocupacion media</h6><h3>${data.ocupacionMedia.toFixed(1)}%</h3></div></div>
        </div>
    `;
}

async function loadUsers() {
  if (!usersTable && !monitorSelect) {
    return;
  }
  const response = await authFetch('/api/admin/users');
  if (!response.ok) {
    return;
  }
  const users = await response.json();
  if (usersTable) {
    usersTable.innerHTML = users
      .map(user => {
        const roleOptions = ['USER', 'STAFF', 'ADMIN']
          .map(
            role =>
              `<option value="${role}" ${user.rol === role ? 'selected' : ''}>${role}</option>`
          )
          .join('');
        return `
            <tr>
                <td style="min-width:180px">
                  <input class="form-control form-control-sm" value="${user.nombre}" data-field="nombre" data-id="${user.id}" />
                </td>
                <td style="min-width:220px">
                  <input type="email" class="form-control form-control-sm" value="${user.email}" data-field="email" data-id="${user.id}" />
                </td>
                <td style="min-width:160px">
                  <input type="tel" inputmode="numeric" pattern="\\d*" class="form-control form-control-sm" value="${user.telefono || ''}" data-field="telefono" data-id="${user.id}" placeholder="Solo números" />
                </td>
                <td>
                  <select class="form-select form-select-sm w-auto" data-field="rol" data-id="${user.id}">
                    ${roleOptions}
                  </select>
                </td>
                <td>
                  <div class="form-check form-switch m-0">
                    <input class="form-check-input" type="checkbox" role="switch" data-field="activo" data-id="${user.id}" ${user.activo ? 'checked' : ''}>
                  </div>
                </td>
                <td style="min-width:210px">
                  <div class="d-flex gap-2 flex-wrap">
                    <button class="btn btn-sm btn-accent" data-action="save" data-id="${user.id}">
                      Guardar cambios
                    </button>
                    <button class="btn btn-sm btn-outline-light" data-action="password" data-id="${user.id}">
                      Cambiar contraseña
                    </button>
                  </div>
                </td>
            </tr>
        `;
      })
      .join('');
  }

  const staff = users.filter(user => user.rol === 'STAFF');
  if (monitorSelect) {
    monitorSelect.innerHTML =
      '<option value="">Sin monitor</option>' +
      staff
        .map(user => `<option value="${user.id}">${user.nombre}</option>`)
        .join('');
  }
}

async function loadClasses() {
  if (!classSelect && !classListAdmin) {
    return;
  }
  const response = await authFetch('/api/public/clases');
  if (!response.ok) {
    return;
  }
  const classes = await response.json();
  if (classSelect) {
    classSelect.innerHTML = classes
      .map(clase => `<option value="${clase.id}">${clase.nombre}</option>`)
      .join('');
  }
  if (classListAdmin) {
    classListAdmin.innerHTML = classes
      .map(c => {
        const levels = ['BASICO', 'INTERMEDIO', 'AVANZADO']
          .map(l => `<option value="${l}" ${c.nivel === l ? 'selected' : ''}>${l}</option>`)
          .join('');
        return `
        <tr>
          <td style="min-width:180px">
            <input class="form-control form-control-sm" data-class-field="nombre" data-id="${c.id}" value="${c.nombre}" />
          </td>
          <td style="min-width:160px">
            <select class="form-select form-select-sm" data-class-field="nivel" data-id="${c.id}">
              ${levels}
            </select>
          </td>
          <td style="min-width:120px">
            <input type="number" min="1" class="form-control form-control-sm" data-class-field="aforoMaximo" data-id="${c.id}" value="${c.aforoMaximo}" />
          </td>
          <td style="min-width:110px">
            <select class="form-select form-select-sm" data-class-field="privada" data-id="${c.id}">
              <option value="false" ${!c.privada ? 'selected' : ''}>No</option>
              <option value="true" ${c.privada ? 'selected' : ''}>Sí</option>
            </select>
          </td>
          <td style="min-width:220px">
            <input class="form-control form-control-sm" data-class-field="imagenUrl" data-id="${c.id}" value="${c.imagenUrl || ''}" placeholder="Imagen URL" />
          </td>
          <td style="min-width:260px">
            <input class="form-control form-control-sm" data-class-field="descripcion" data-id="${c.id}" value="${(c.descripcion || '').replace(/\"/g,'&quot;')}" />
          </td>
          <td style="min-width:160px">
            <button class="btn btn-sm btn-accent" data-action="save-class" data-id="${c.id}">Guardar</button>
          </td>
        </tr>
      `;
      })
      .join('');
  }
}

async function loadAdminSchedules() {
  if (!scheduleListAdmin) {
    return;
  }
  const response = await authFetch('/api/horarios');
  if (!response.ok) {
    return;
  }
  const schedules = await response.json();
  scheduleListAdmin.innerHTML = schedules
    .map(h => {
      return `
      <tr>
        <td style="min-width:160px">${h.claseNombre}</td>
        <td style="display:none">
          <input type="hidden" data-h-field="claseId" data-id="${h.id}" value="${h.claseId}" />
        </td>
        <td style="min-width:200px">
          <input type="datetime-local" class="form-control form-control-sm" data-h-field="fechaHoraInicio" data-id="${h.id}" value="${new Date(h.fechaHoraInicio).toISOString().slice(0,16)}" />
        </td>
        <td style="min-width:140px">
          <input class="form-control form-control-sm" data-h-field="sala" data-id="${h.id}" value="${h.sala}" />
        </td>
        <td style="min-width:220px">
          <select class="form-select form-select-sm" data-h-field="monitorId" data-id="${h.id}">
            ${monitorSelect ? monitorSelect.innerHTML : '<option value=\"\">Sin monitor</option>'}
          </select>
        </td>
        <td style="min-width:120px">
          <input type="number" min="1" class="form-control form-control-sm" data-h-field="duracion" data-id="${h.id}" value="${h.duracion}" />
        </td>
        <td style="min-width:140px">
          <input type="number" min="1" class="form-control form-control-sm" data-h-field="aforoMaximo" data-id="${h.id}" value="${h.aforoMaximo ?? ''}" placeholder="(Clase)" />
        </td>
        <td style="min-width:140px">
          <button class="btn btn-sm btn-accent" data-action="save-horario" data-id="${h.id}">Guardar</button>
        </td>
      </tr>
    `;
    })
    .join('');

  // Set selected monitor values after DOM render (because monitor list is global)
  schedules.forEach(h => {
    const select = scheduleListAdmin.querySelector(`select[data-h-field='monitorId'][data-id='${h.id}']`);
    if (select) {
      select.value = h.monitorId ? String(h.monitorId) : '';
    }
  });
}

usersTable?.addEventListener('click', async event => {
  const saveButton = event.target.closest("button[data-action='save']");
  const passButton = event.target.closest("button[data-action='password']");

  const button = saveButton || passButton;
  if (!button) {
    return;
  }
  const id = button.dataset.id;
  const get = field =>
    usersTable.querySelector(`[data-field='${field}'][data-id='${id}']`);

  const nombre = get('nombre')?.value?.trim();
  const email = get('email')?.value?.trim();
  const telefono = get('telefono')?.value?.trim();
  const rol = get('rol')?.value;
  const activo = !!get('activo')?.checked;

  if (!nombre || !email || !rol) {
    return;
  }

  let password = null;
  if (passButton) {
    password = window.prompt('Nueva contraseña (mín. 8 caracteres):');
    if (!password) {
      return;
    }
  }

  if (telefono && !/^\d+$/.test(telefono)) {
    alert('El teléfono solo puede contener números.');
    return;
  }

  await authFetch(`/api/admin/users/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombre, email, telefono, rol, activo, password }),
  });

  loadUsers();
});

classForm?.addEventListener('submit', async event => {
  event.preventDefault();
  const payload = {
    nombre: classForm.nombre.value,
    descripcion: classForm.descripcion.value,
    nivel: classForm.nivel.value,
    aforoMaximo: Number(classForm.aforo.value),
    privada: classForm.privada.checked,
    imagenUrl: classForm.imagenUrl.value,
  };

  const response = await authFetch('/api/admin/clases', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  if (response.ok) {
    classForm.reset();
    loadClasses();
    loadAdminSchedules();
    if (classFormMessage) classFormMessage.textContent = 'Clase guardada.';
  } else {
    const err = await response.json().catch(() => null);
    if (classFormMessage) classFormMessage.textContent = err?.message || 'Error guardando clase.';
  }
});

scheduleForm?.addEventListener('submit', async event => {
  event.preventDefault();
  const aforoValue = scheduleForm.aforo?.value?.trim();
  const payload = {
    claseId: Number(classSelect.value),
    monitorId: monitorSelect.value ? Number(monitorSelect.value) : null,
    fechaHoraInicio: scheduleForm.fecha.value,
    duracion: Number(scheduleForm.duracion.value),
    sala: scheduleForm.sala.value,
    aforoMaximo: aforoValue ? Number(aforoValue) : null,
  };

  const response = await authFetch('/api/admin/horarios', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  if (response.ok) {
    scheduleForm.reset();
    loadAdminSchedules();
    if (scheduleFormMessage) scheduleFormMessage.textContent = 'Horario guardado.';
  } else {
    const err = await response.json().catch(() => null);
    if (scheduleFormMessage) scheduleFormMessage.textContent = err?.message || 'Error guardando horario.';
  }
});

classListAdmin?.addEventListener('click', async event => {
  const btn = event.target.closest("button[data-action='save-class']");
  if (!btn) return;
  const id = btn.dataset.id;
  const get = field => classListAdmin.querySelector(`[data-class-field='${field}'][data-id='${id}']`);
  const payload = {
    nombre: get('nombre')?.value?.trim(),
    nivel: get('nivel')?.value,
    aforoMaximo: Number(get('aforoMaximo')?.value || 0),
    privada: get('privada')?.value === 'true',
    imagenUrl: get('imagenUrl')?.value?.trim(),
    descripcion: get('descripcion')?.value?.trim(),
  };
  const response = await authFetch(`/api/admin/clases/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  if (response.ok) {
    loadClasses();
  }
});

scheduleListAdmin?.addEventListener('click', async event => {
  const btn = event.target.closest("button[data-action='save-horario']");
  if (!btn) return;
  const id = btn.dataset.id;
  const get = field => scheduleListAdmin.querySelector(`[data-h-field='${field}'][data-id='${id}']`);
  const monitorId = get('monitorId')?.value;
  const payload = {
    claseId: Number(get('claseId')?.value || 0),
    monitorId: monitorId ? Number(monitorId) : null,
    fechaHoraInicio: get('fechaHoraInicio')?.value,
    duracion: Number(get('duracion')?.value || 0),
    sala: get('sala')?.value?.trim(),
    aforoMaximo: get('aforoMaximo')?.value ? Number(get('aforoMaximo').value) : null,
  };
  // Need claseId for update: infer from schedules table? fallback: keep current select. Better: store it as data attr in rows later if needed.
  const response = await authFetch(`/api/admin/horarios/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  if (response.ok) {
    loadAdminSchedules();
  } else {
    const err = await response.json().catch(() => null);
    alert(err?.message || 'Error actualizando horario');
  }
});

exportCsv?.addEventListener('click', async () => {
  const response = await authFetch('/api/admin/report/csv');
  if (!response.ok) {
    return;
  }
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'ocupacion.csv';
  a.click();
  window.URL.revokeObjectURL(url);
});

exportPdf?.addEventListener('click', async () => {
  const response = await authFetch('/api/admin/report/pdf');
  if (!response.ok) {
    return;
  }
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'ocupacion.pdf';
  a.click();
  window.URL.revokeObjectURL(url);
});

loadDashboard();
loadUsers();
loadClasses();
loadAdminSchedules();
