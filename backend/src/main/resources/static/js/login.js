const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');

loginForm?.addEventListener('submit', async event => {
  event.preventDefault();
  loginError.textContent = '';

  const payload = {
    email: loginForm.email.value,
    password: loginForm.password.value,
  };

  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    loginError.textContent = error?.message || 'No se pudo iniciar sesión';
    return;
  }

  const data = await response.json();
  setToken(data.token);
  if (data.rol === 'ADMIN') {
    window.location.href = '/admin';
  } else if (data.rol === 'STAFF') {
    window.location.href = '/staff/horarios';
  } else {
    window.location.href = '/dashboard';
  }
});
