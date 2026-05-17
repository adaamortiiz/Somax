const registerForm = document.getElementById('registerForm');
const registerMessage = document.getElementById('registerMessage');

registerForm?.addEventListener('submit', async event => {
  event.preventDefault();
  registerMessage.textContent = '';

  const payload = {
    nombre: registerForm.nombre.value,
    email: registerForm.email.value,
    telefono: registerForm.telefono.value,
    password: registerForm.password.value,
  };

  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    registerMessage.textContent =
      error?.message || 'No se pudo crear la cuenta';
    return;
  }

  registerMessage.textContent =
    'Registro completado. Espera a que el administrador active tu cuenta.';
  registerForm.reset();
});
