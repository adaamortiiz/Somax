const API_BASE = '';
const TOKEN_KEY = 'somax_token';

function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function getTokenPayload() {
  const token = getToken();
  if (!token || !token.includes('.')) {
    return null;
  }
  try {
    const payload = token.split('.')[1];
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(normalized)
        .split('')
        .map(c => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function getRoleFromToken() {
  const payload = getTokenPayload();
  return payload?.role || null;
}

function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = '/login';
  }
}

async function authFetch(url, options = {}) {
  const token = getToken();
  const headers = options.headers || {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (!headers['Content-Type'] && options.body) {
    headers['Content-Type'] = 'application/json';
  }
  const response = await fetch(API_BASE + url, { ...options, headers });
  if (response.status === 401 || response.status === 403) {
    clearToken();
  }
  return response;
}

function formatDateTime(value) {
  const date = new Date(value);
  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

function logout() {
  clearToken();
  window.location.href = '/login';
}

function ensureFontAwesome() {
  if (document.getElementById('somax-fontawesome')) {
    return;
  }
  const link = document.createElement('link');
  link.id = 'somax-fontawesome';
  link.rel = 'stylesheet';
  link.href =
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css';
  document.head.appendChild(link);
}

function enhanceSidebarIcons() {
  const iconMap = {
    '/dashboard': 'fa-solid fa-chart-line',
    '/app/calendar': 'fa-regular fa-calendar-days',
    '/app/reservas': 'fa-solid fa-ticket',
    '/app/clases': 'fa-solid fa-dumbbell',
    '/app/chat': 'fa-solid fa-robot',
    '/app/notificaciones': 'fa-regular fa-bell',
    '/staff/horarios': 'fa-solid fa-user-clock',
    '/admin': 'fa-solid fa-shield-halved',
    '/admin/usuarios': 'fa-solid fa-users',
    '/admin/clases': 'fa-solid fa-layer-group',
    '/admin/horarios': 'fa-regular fa-clock',
    '/admin/reportes': 'fa-solid fa-file-lines',
  };

  document.querySelectorAll('.sidebar a[href]').forEach(link => {
    if (link.dataset.enhanced === 'true') {
      return;
    }
    const href = link.getAttribute('href');
    const iconClass =
      iconMap[href] ||
      (link.textContent.includes('Cerrar')
        ? 'fa-solid fa-right-from-bracket'
        : 'fa-solid fa-angle-right');
    link.innerHTML = `<i class="${iconClass}"></i>${link.textContent.trim()}`;
    link.dataset.enhanced = 'true';
  });
}

function enhanceBranding() {
  const logo = document.getElementById('somax-logo');
  if (logo) {
    logo.classList.add('somax-logo');
  }
  document.querySelectorAll('.navbar .btn, .main-panel .btn').forEach(btn => {
    btn.classList.add('btn-modern');
  });
}

function enforceRouteByRole() {
  const path = window.location.pathname;
  const isPrivatePage =
    path === '/dashboard' ||
    path.startsWith('/app/') ||
    path.startsWith('/admin') ||
    path.startsWith('/staff');

  const role = getRoleFromToken();
  if (isPrivatePage && !role) {
    window.location.href = '/login';
    return;
  }

  if (role === 'ADMIN') {
    if (path === '/dashboard' || path.startsWith('/app/') || path.startsWith('/staff')) {
      window.location.href = '/admin';
    }
    return;
  }

  if (role === 'STAFF') {
    if (path === '/dashboard' || path.startsWith('/app/') || path.startsWith('/admin')) {
      window.location.href = '/staff/horarios';
    }
    return;
  }

  if (role === 'USER') {
    if (path.startsWith('/admin') || path.startsWith('/staff')) {
      window.location.href = '/dashboard';
    }
  }
}

document.addEventListener('DOMContentLoaded', function () {
  ensureFontAwesome();
  enforceRouteByRole();
  enhanceBranding();
  enhanceSidebarIcons();

  // Mobile sidebar toggle for dashboard-shell layouts
  document.addEventListener('click', event => {
    const shell = document.querySelector('.dashboard-shell');
    if (!shell) {
      return;
    }
    if (event.target.closest('[data-sidebar-toggle]')) {
      shell.classList.toggle('sidebar-open');
      return;
    }
    if (event.target.closest('.sidebar-backdrop')) {
      shell.classList.remove('sidebar-open');
    }
  });
});
