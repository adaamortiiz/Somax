const forgotForm = document.getElementById('forgotForm');
const forgotMessage = document.getElementById('forgotMessage');

forgotForm?.addEventListener('submit', async event => {
  event.preventDefault();
  forgotMessage.textContent = '';

  const payload = { email: forgotForm.email.value };
  const response = await fetch('/api/auth/password/reset-request', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    forgotMessage.textContent = 'No se pudo enviar el enlace de recuperación';
    return;
  }

  forgotMessage.textContent =
    'Si el correo existe, recibirás un enlace de recuperación.';
  forgotForm.reset();
});
