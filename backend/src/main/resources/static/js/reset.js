const resetForm = document.getElementById('resetForm');
const resetMessage = document.getElementById('resetMessage');

const params = new URLSearchParams(window.location.search);
const token = params.get('token');

if (resetForm && token) {
  resetForm.token.value = token;
}

resetForm?.addEventListener('submit', async event => {
  event.preventDefault();
  resetMessage.textContent = '';

  const payload = {
    token: resetForm.token.value,
    nuevaPassword: resetForm.password.value,
  };

  const response = await fetch('/api/auth/password/reset', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    resetMessage.textContent =
      error?.message || 'No se pudo actualizar la contraseña';
    return;
  }

  resetMessage.textContent =
    'Contraseña actualizada. Ya puedes iniciar sesión.';
  resetForm.reset();
});
