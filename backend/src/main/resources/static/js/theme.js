(function () {
  const lightLogo = '/img/logo-sin-bg-negro.png';
  const darkLogo = '/img/logo-blanco-sin-bg.png';

  function updateToggleLabel(theme) {
    const toggles = document.querySelectorAll('#theme-toggle');
    toggles.forEach(toggle => {
      toggle.classList.add('theme-toggle');
      toggle.innerHTML =
        theme === 'light'
          ? '<i class="fa-solid fa-moon"></i>'
          : '<i class="fa-solid fa-sun"></i>';
      toggle.setAttribute('aria-label', 'Cambiar tema');
    });
  }

  function updateLogos(theme) {
    document.querySelectorAll('img').forEach(img => {
      if (!img.src) {
        return;
      }
      if (
        img.src.includes('logo-blanco-sin-bg.png') ||
        img.src.includes('logo-sin-bg-negro.png')
      ) {
        img.src = theme === 'light' ? lightLogo : darkLogo;
      }
    });
  }

  function applyTheme(theme) {
    if (theme === 'light') {
      document.body.classList.add('light');
    } else {
      document.body.classList.remove('light');
    }
    updateLogos(theme);
    updateToggleLabel(theme);
    localStorage.setItem('somax-theme', theme);
    document.dispatchEvent(
      new CustomEvent('somax:theme-changed', { detail: { theme } })
    );
  }

  document.addEventListener('DOMContentLoaded', function () {
    const saved = localStorage.getItem('somax-theme') || 'dark';
    applyTheme(saved);
    const toggles = [];
    const t1 = document.getElementById('theme-toggle');
    if (t1) toggles.push(t1);
    toggles.forEach(function (toggle) {
      toggle.addEventListener('click', function () {
        const current = document.body.classList.contains('light')
          ? 'light'
          : 'dark';
        applyTheme(current === 'light' ? 'dark' : 'light');
      });
    });
  });
})();
