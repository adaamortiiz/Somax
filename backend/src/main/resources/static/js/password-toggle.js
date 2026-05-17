function wirePasswordToggles() {
  document.querySelectorAll('[data-password-toggle]').forEach(btn => {
    if (btn.dataset.wired === 'true') return;
    btn.dataset.wired = 'true';
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-password-toggle');
      const input = document.getElementById(targetId);
      if (!input) return;
      const isPassword = input.getAttribute('type') === 'password';
      input.setAttribute('type', isPassword ? 'text' : 'password');
      btn.innerHTML = isPassword
        ? '<i class="fa-regular fa-eye-slash"></i>'
        : '<i class="fa-regular fa-eye"></i>';
    });
  });
}

document.addEventListener('DOMContentLoaded', wirePasswordToggles);

