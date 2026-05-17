requireAuth();

const calendarEl = document.getElementById('classCalendar');
const userName = document.getElementById('userName');
const adminLink = document.getElementById('adminLink');
const staffLink = document.getElementById('staffLink');

function colorForSchedule(item, hasReserva) {
  if (hasReserva) return '#0dcaf0'; // info
  if (item.estadoAforo === 'COMPLETO') return '#dc3545'; // danger
  if (item.estadoAforo === 'POCAS_PLAZAS') return '#ffc107'; // warning
  return '#198754'; // success
}

async function loadProfile() {
  const response = await authFetch('/api/users/me');
  if (!response.ok) return null;
  const profile = await response.json();
  if (userName) userName.textContent = profile.nombre;
  if (adminLink && profile.rol === 'ADMIN') adminLink.classList.remove('d-none');
  if (staffLink && (profile.rol === 'STAFF' || profile.rol === 'ADMIN'))
    staffLink.classList.remove('d-none');
  return profile;
}

async function loadData() {
  const [schedulesRes, reservasRes] = await Promise.all([
    authFetch('/api/horarios'),
    authFetch('/api/reservas'),
  ]);

  const schedules = schedulesRes.ok ? await schedulesRes.json() : [];
  const reservas = reservasRes.ok ? await reservasRes.json() : [];

  const reservaByHorarioId = new Map();
  reservas.forEach(r => reservaByHorarioId.set(String(r.horarioId), r));

  return { schedules, reservaByHorarioId };
}

async function reserveHorario(horarioId) {
  const response = await authFetch('/api/reservas', {
    method: 'POST',
    body: JSON.stringify({ horarioId }),
  });
  return response.ok;
}

async function cancelReserva(reservaId) {
  const response = await authFetch(`/api/reservas/${reservaId}`, { method: 'DELETE' });
  return response.ok;
}

function buildEventTitle(item, hasReserva) {
  const prefix = hasReserva ? '✓ ' : '';
  return `${prefix}${item.claseNombre} · ${item.sala}`;
}

async function initCalendar() {
  if (!calendarEl) return;

  const profile = await loadProfile();
  const { schedules, reservaByHorarioId } = await loadData();

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
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
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    nowIndicator: true,
    eventTimeFormat: { hour: '2-digit', minute: '2-digit', hour12: false },
    events: schedules.map(item => {
      const reserva = reservaByHorarioId.get(String(item.id));
      const hasReserva = !!reserva;
      const color = colorForSchedule(item, hasReserva);
      return {
        id: String(item.id),
        title: buildEventTitle(item, hasReserva),
        start: item.fechaHoraInicio,
        allDay: false,
        backgroundColor: color,
        borderColor: color,
        textColor: '#0b0d10',
        extendedProps: {
          item,
          reserva,
          hasReserva,
          role: profile?.rol,
        },
      };
    }),
    eventClick: async info => {
      const { item, reserva, hasReserva, role } = info.event.extendedProps || {};
      if (!item) return;

      // Reserva solo USER (en backend ya está forzado). Aquí damos UX.
      if (role && role !== 'USER') {
        alert('Solo los usuarios pueden reservar clases.');
        return;
      }

      if (hasReserva && reserva) {
        const ok = confirm(
          `Ya tienes reserva en ${item.claseNombre} (${new Date(item.fechaHoraInicio).toLocaleString('es-ES')}).\n¿Cancelar reserva?`
        );
        if (!ok) return;
        const cancelled = await cancelReserva(reserva.id);
        if (!cancelled) return;
      } else {
        const label = item.estadoAforo === 'COMPLETO' ? 'Entrar en lista de espera' : 'Reservar';
        const ok = confirm(
          `${item.claseNombre}\n${new Date(item.fechaHoraInicio).toLocaleString('es-ES')}\nSala: ${item.sala}\n\n${label}?`
        );
        if (!ok) return;
        const booked = await reserveHorario(Number(item.id));
        if (!booked) return;
      }

      // Refresco completo de eventos tras reservar/cancelar
      const fresh = await loadData();
      const freshReservaByHorarioId = fresh.reservaByHorarioId;
      calendar.getEvents().forEach(e => e.remove());
      fresh.schedules.forEach(s => {
        const r = freshReservaByHorarioId.get(String(s.id));
        const has = !!r;
        const color = colorForSchedule(s, has);
        calendar.addEvent({
          id: String(s.id),
          title: buildEventTitle(s, has),
          start: s.fechaHoraInicio,
          backgroundColor: color,
          borderColor: color,
          textColor: '#0b0d10',
          extendedProps: { item: s, reserva: r, hasReserva: has, role: profile?.rol },
        });
      });
    },
  });

  calendar.render();

  // Refresco suave para “tiempo real”
  setInterval(async () => {
    const fresh = await loadData();
    const freshReservaByHorarioId = fresh.reservaByHorarioId;
    calendar.getEvents().forEach(e => e.remove());
    fresh.schedules.forEach(s => {
      const r = freshReservaByHorarioId.get(String(s.id));
      const has = !!r;
      const color = colorForSchedule(s, has);
      calendar.addEvent({
        id: String(s.id),
        title: buildEventTitle(s, has),
        start: s.fechaHoraInicio,
        backgroundColor: color,
        borderColor: color,
        textColor: '#0b0d10',
        extendedProps: { item: s, reserva: r, hasReserva: has, role: profile?.rol },
      });
    });
  }, 20000);
}

document.addEventListener('DOMContentLoaded', initCalendar);
