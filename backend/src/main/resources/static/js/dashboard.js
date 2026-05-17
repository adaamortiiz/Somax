requireAuth();

const scheduleList = document.getElementById('scheduleList');
const reservationList = document.getElementById('reservationList');
const notificationList = document.getElementById('notificationList');
const chatForm = document.getElementById('chatForm');
const chatInput = document.getElementById('chatInput');
const chatMessages = document.getElementById('chatMessages');
const classList = document.getElementById('classList');
const staffScheduleList = document.getElementById('staffScheduleList');
const staffAttendanceList = document.getElementById('staffAttendanceList');
const userName = document.getElementById('userName');
const adminLink = document.getElementById('adminLink');
const staffLink = document.getElementById('staffLink');

function setActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar a[href]').forEach(link => {
    const href = link.getAttribute('href');
    if (!href || href === '#') {
      return;
    }
    if (path === href) {
      link.classList.add('active');
    } else {
      link.classList.remove('active');
    }
  });
}

function addSmoothHoverAnimation() {
  document.querySelectorAll('.glass-card, .calendar-item').forEach(element => {
    element.addEventListener('mouseenter', () => {
      element.style.transform = 'translateY(-2px)';
      element.style.transition = 'transform .2s ease';
    });
    element.addEventListener('mouseleave', () => {
      element.style.transform = '';
    });
  });
}

async function loadProfile() {
  if (!userName) {
    return;
  }
  const response = await authFetch('/api/users/me');
  if (!response.ok) {
    return;
  }
  const profile = await response.json();
  userName.textContent = profile.nombre;
  if (adminLink && profile.rol === 'ADMIN') {
    adminLink.classList.remove('d-none');
  }
  if (staffLink && (profile.rol === 'STAFF' || profile.rol === 'ADMIN')) {
    staffLink.classList.remove('d-none');
  }
}

function renderAforoPill(estado) {
  if (estado === 'COMPLETO') {
    return 'state-full';
  }
  if (estado === 'POCAS_PLAZAS') {
    return 'state-few';
  }
  return 'state-available';
}

async function loadSchedules() {
  if (!scheduleList) {
    return;
  }
  const [schedulesRes, reservasRes] = await Promise.all([
    authFetch('/api/horarios'),
    authFetch('/api/reservas'),
  ]);
  if (!schedulesRes.ok) {
    return;
  }
  const schedules = await schedulesRes.json();
  const reservas = reservasRes.ok ? await reservasRes.json() : [];
  const reservadoPorHorario = new Set(reservas.map(r => String(r.horarioId)));
  scheduleList.innerHTML = schedules
    .map(item => {
      const reservado = reservadoPorHorario.has(String(item.id));
      const actionLabel =
        item.estadoAforo === 'COMPLETO' ? 'Lista espera' : 'Reservar';
      return `
            <div class="calendar-item">
                <div>
                    <h6 class="mb-1">${item.claseNombre}</h6>
                    <small class="text-muted">${formatDateTime(item.fechaHoraInicio)} · ${item.sala}</small>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <span class="state-pill ${renderAforoPill(item.estadoAforo)}">${item.estadoAforo}</span>
                    ${
                      reservado
                        ? '<span class="tag">Reservada</span>'
                        : `<button class="btn btn-sm btn-accent" data-action="reserva" data-id="${item.id}">${actionLabel}</button>`
                    }
                </div>
            </div>
        `;
    })
    .join('');
}

async function loadReservations() {
  if (!reservationList) {
    return;
  }
  const response = await authFetch('/api/reservas');
  if (!response.ok) {
    return;
  }
  const reservas = await response.json();
  reservationList.innerHTML = reservas
    .map(reserva => {
      return `
            <div class="d-flex justify-content-between align-items-center border-bottom border-secondary-subtle py-2">
                <div>
                    <strong>${reserva.claseNombre}</strong>
                    <div class="text-muted small">${formatDateTime(reserva.fechaHoraInicio)}</div>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="tag">${reserva.estado}</span>
                    <button class="btn btn-sm btn-outline-light" data-action="cancelar" data-id="${reserva.id}">Cancelar</button>
                </div>
            </div>
        `;
    })
    .join('');
}

async function loadNotifications() {
  if (!notificationList) {
    return;
  }
  const response = await authFetch('/api/notificaciones');
  if (!response.ok) {
    return;
  }
  const notificaciones = await response.json();

  const titleMap = {
    RESERVA_CONFIRMADA: 'Reserva confirmada',
    LISTA_ESPERA: 'Lista de espera',
    PROMOCION_LISTA_ESPERA: 'Plaza disponible',
    RESERVA_CANCELADA: 'Reserva cancelada',
    RECUPERACION_PASSWORD: 'Recuperación de contraseña',
    SISTEMA: 'Aviso del sistema',
  };
  const iconMap = {
    RESERVA_CONFIRMADA: 'fa-solid fa-circle-check',
    LISTA_ESPERA: 'fa-solid fa-hourglass-half',
    PROMOCION_LISTA_ESPERA: 'fa-solid fa-bell',
    RESERVA_CANCELADA: 'fa-regular fa-circle-xmark',
    RECUPERACION_PASSWORD: 'fa-solid fa-key',
    SISTEMA: 'fa-solid fa-circle-info',
  };

  notificationList.innerHTML = notificaciones
    .map(n => {
      const title = titleMap[n.tipo] || n.tipo;
      const icon = iconMap[n.tipo] || 'fa-regular fa-bell';
      return `
            <div class="chat-bubble">
                <strong><i class="${icon} me-2"></i>${title}</strong>
                <div class="text-muted small">${formatDateTime(n.fechaEnvio)}</div>
                <div>${n.mensaje}</div>
            </div>
        `;
    })
    .join('');
}

scheduleList?.addEventListener('click', async event => {
  const button = event.target.closest("button[data-action='reserva']");
  if (!button) {
    return;
  }

  const horarioId = button.dataset.id;
  const response = await authFetch('/api/reservas', {
    method: 'POST',
    body: JSON.stringify({ horarioId }),
  });

  if (response.ok) {
    await loadSchedules();
    await loadReservations();
    await loadNotifications();
  }
});

reservationList?.addEventListener('click', async event => {
  const button = event.target.closest("button[data-action='cancelar']");
  if (!button) {
    return;
  }

  const reservaId = button.dataset.id;
  const response = await authFetch(`/api/reservas/${reservaId}`, {
    method: 'DELETE',
  });
  if (response.ok) {
    await loadSchedules();
    await loadReservations();
    await loadNotifications();
  }
});

chatForm?.addEventListener('submit', async event => {
  event.preventDefault();
  if (!chatMessages) {
    return;
  }
  const mensaje = chatInput.value.trim();
  if (!mensaje) {
    return;
  }

  chatMessages.innerHTML += `<div class="chat-bubble me">${mensaje}</div>`;
  chatInput.value = '';

  const response = await authFetch('/api/chat', {
    method: 'POST',
    body: JSON.stringify({ mensaje }),
  });

  if (response.ok) {
    const data = await response.json();
    chatMessages.innerHTML += `<div class="chat-bubble">${data.respuesta}</div>`;
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }
});

async function loadClasses() {
  if (!classList) {
    return;
  }
  const response = await authFetch('/api/public/clases');
  if (!response.ok) {
    return;
  }
  const classes = await response.json();
  classList.innerHTML = classes
    .map(clase => {
      const imageUrl = clase.imagenUrl && String(clase.imagenUrl).trim();
      return `
            <div class="col-md-4">
                <div class="glass-card h-100">
                    <div class="class-image mb-3">
                      ${
                        imageUrl
                          ? `<img src="${imageUrl}" alt="${clase.nombre}" loading="lazy" onerror="this.style.display='none'" />`
                          : `<div class="class-image-placeholder"><i class="fa-solid fa-dumbbell"></i></div>`
                      }
                    </div>
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="mb-0">${clase.nombre}</h5>
                        <span class="tag">${clase.nivel}</span>
                    </div>
                    <p class="text-muted small">${clase.descripcion || ''}</p>
                    <div class="text-muted small">Aforo maximo: ${clase.aforoMaximo}</div>
                </div>
            </div>
        `;
    })
    .join('');
}

async function loadStaffSchedules() {
  if (!staffScheduleList) {
    return;
  }
  const response = await authFetch('/api/staff/horarios');
  if (!response.ok) {
    return;
  }
  const schedules = await response.json();
  staffScheduleList.innerHTML = schedules
    .map(item => {
      return `
            <div class="calendar-item">
                <div>
                    <h6 class="mb-1">${item.claseNombre}</h6>
                    <small class="text-muted">${formatDateTime(item.fechaHoraInicio)} · ${item.sala}</small>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-sm btn-outline-light" data-action="asistencia" data-id="${item.id}">Ver asistencia</button>
                </div>
            </div>
        `;
    })
    .join('');
}

staffScheduleList?.addEventListener('click', async event => {
  const button = event.target.closest("button[data-action='asistencia']");
  if (!button || !staffAttendanceList) {
    return;
  }

  const horarioId = button.dataset.id;
  const response = await authFetch(
    `/api/reservas/staff/horarios/${horarioId}/asistencia`
  );
  if (!response.ok) {
    return;
  }

  const asistencia = await response.json();
  if (asistencia.length === 0) {
    staffAttendanceList.innerHTML =
      '<p class="text-muted mb-0">Sin asistentes registrados.</p>';
    return;
  }

  staffAttendanceList.innerHTML = asistencia
    .map(item => {
      return `
            <div class="d-flex justify-content-between align-items-center border-bottom border-secondary-subtle py-2">
                <div>
                    <strong>${item.nombre}</strong>
                    <div class="text-muted small">${item.email}</div>
                </div>
                <span class="tag">${item.telefono || 'Sin teléfono'}</span>
            </div>
        `;
    })
    .join('');
});

loadProfile();
loadSchedules();
loadReservations();
loadNotifications();
loadClasses();
loadStaffSchedules();
setActiveNav();
addSmoothHoverAnimation();

// Refresco simple para mantener aforo/reservas/notificaciones actualizados.
setInterval(() => {
  loadSchedules();
  loadReservations();
  loadNotifications();
  loadStaffSchedules();
}, 15000);
