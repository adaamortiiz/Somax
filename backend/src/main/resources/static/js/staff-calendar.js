requireAuth();

const calendarEl = document.getElementById('staffCalendar');
const staffScheduleList = document.getElementById('staffScheduleList');
const staffAttendanceList = document.getElementById('staffAttendanceList');
const userName = document.getElementById('userName');

async function loadProfile() {
  const response = await authFetch('/api/users/me');
  if (!response.ok) return null;
  const profile = await response.json();
  if (userName) userName.textContent = profile.nombre;
  return profile;
}

async function loadStaffSchedules() {
  const response = await authFetch('/api/staff/horarios');
  if (!response.ok) return [];
  return await response.json();
}

function renderList(schedules) {
  if (!staffScheduleList) return;
  staffScheduleList.innerHTML = schedules
    .map(
      item => `
      <div class="calendar-item">
        <div>
          <h6 class="mb-1">${item.claseNombre}</h6>
          <small class="text-muted">${formatDateTime(item.fechaHoraInicio)} · ${item.sala}</small>
        </div>
        <div class="d-flex align-items-center gap-3">
          <button class="btn btn-sm btn-outline-light" data-action="asistencia" data-id="${item.id}">
            Ver asistencia
          </button>
        </div>
      </div>
    `
    )
    .join('');
}

async function loadAsistencia(horarioId) {
  const response = await authFetch(
    `/api/reservas/staff/horarios/${horarioId}/asistencia`
  );
  if (!response.ok) return null;
  return await response.json();
}

function attendanceBadgeHtml(isConfirmed) {
  if (isConfirmed) {
    return '<i class="fa-solid fa-circle-check"></i> Asistencia confirmada';
  }
  return '<i class="fa-solid fa-circle-question"></i> Pendiente';
}

function attendanceButtonHtml(isConfirmed) {
  if (isConfirmed) {
    return '<i class="fa-solid fa-rotate me-1"></i>Marcar como pendiente';
  }
  return '<i class="fa-solid fa-check me-1"></i>Confirmar asistencia';
}

function renderAsistencia(asistencia) {
  if (!staffAttendanceList) return;
  if (!asistencia || asistencia.length === 0) {
    staffAttendanceList.innerHTML =
      '<p class="text-muted mb-0">Sin asistentes registrados.</p>';
    return;
  }

  staffAttendanceList.innerHTML = asistencia
    .map(
      item => `
      <div class="attendance-row border-bottom border-secondary-subtle py-2">
        <div>
          <div class="d-flex align-items-center gap-2 flex-wrap">
            <strong>${item.nombre}</strong>
            <span class="attendance-badge ${
              item.asistenciaConfirmada
                ? 'attendance-badge--confirmed'
                : 'attendance-badge--pending'
            }">${attendanceBadgeHtml(item.asistenciaConfirmada)}</span>
          </div>
          <div class="text-muted small">${item.email}</div>
        </div>
        <div class="d-flex align-items-center gap-2 flex-wrap justify-content-end">
          <span class="tag">
            <i class="fa-solid fa-phone me-1"></i>${
              item.telefono || 'Sin teléfono'
            }
          </span>
          <button
            class="btn btn-sm attendance-toggle ${
              item.asistenciaConfirmada
                ? 'attendance-toggle--confirmed'
                : 'attendance-toggle--pending'
            }"
            data-action="toggle-asistencia"
            data-id="${item.reservaId}"
            data-value="${item.asistenciaConfirmada}"
            type="button"
          >${attendanceButtonHtml(item.asistenciaConfirmada)}</button>
        </div>
      </div>
    `
    )
    .join('');
}

async function init() {
  const profile = await loadProfile();
  const schedules = await loadStaffSchedules();
  renderList(schedules);

  if (calendarEl) {
    const isMobile = window.matchMedia('(max-width: 768px)').matches;
    const calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: isMobile ? 'listWeek' : 'dayGridMonth',
      height: 'auto',
      locale: 'es',
      firstDay: 1,
      buttonText: {
        today: 'Hoy',
        month: 'Mes',
        week: 'Semana',
        day: 'Día',
        list: 'Lista',
      },
      headerToolbar: {
        left: isMobile ? 'prev,next' : 'prev,next today',
        center: 'title',
        right: isMobile
          ? 'listWeek,dayGridMonth'
          : 'dayGridMonth,timeGridWeek,timeGridDay',
      },
      nowIndicator: true,
      eventTimeFormat: { hour: '2-digit', minute: '2-digit', hour12: false },
      dayMaxEventRows: isMobile ? 2 : true,
      events: schedules.map(item => ({
        id: String(item.id),
        title: `${item.claseNombre} · ${item.sala}`,
        start: item.fechaHoraInicio,
        allDay: false,
        backgroundColor: '#2f80ff',
        borderColor: '#2f80ff',
        textColor: '#0b0d10',
        extendedProps: { item, role: profile?.rol },
      })),
      eventClick: async info => {
        const item = info.event.extendedProps?.item;
        if (!item) return;
        const asistencia = await loadAsistencia(item.id);
        renderAsistencia(asistencia);
      },
    });
    calendar.render();
  }

  staffScheduleList?.addEventListener('click', async event => {
    const button = event.target.closest("button[data-action='asistencia']");
    if (!button) return;
    const horarioId = button.dataset.id;
    const asistencia = await loadAsistencia(horarioId);
    renderAsistencia(asistencia);
  });

  staffAttendanceList?.addEventListener('click', async event => {
    const btn = event.target.closest("button[data-action='toggle-asistencia']");
    if (!btn) return;
    const reservaId = btn.dataset.id;
    const current = btn.dataset.value === 'true';

    const response = await authFetch(
      `/api/reservas/staff/reservas/${reservaId}/asistencia`,
      {
        method: 'PATCH',
        body: JSON.stringify({ asistenciaConfirmada: !current }),
      }
    );
    if (!response.ok) return;
    const updated = await response.json();

    btn.dataset.value = String(updated.asistenciaConfirmada);
    btn.classList.toggle('attendance-toggle--confirmed', updated.asistenciaConfirmada);
    btn.classList.toggle('attendance-toggle--pending', !updated.asistenciaConfirmada);
    btn.innerHTML = attendanceButtonHtml(updated.asistenciaConfirmada);

    const row = btn.closest('.attendance-row');
    const badge = row?.querySelector('.attendance-badge');
    if (badge) {
      badge.classList.toggle('attendance-badge--confirmed', updated.asistenciaConfirmada);
      badge.classList.toggle('attendance-badge--pending', !updated.asistenciaConfirmada);
      badge.innerHTML = attendanceBadgeHtml(updated.asistenciaConfirmada);
    }
  });

  setInterval(async () => {
    const fresh = await loadStaffSchedules();
    renderList(fresh);
  }, 20000);
}

document.addEventListener('DOMContentLoaded', init);

