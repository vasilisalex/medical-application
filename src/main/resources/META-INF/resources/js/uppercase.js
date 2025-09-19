// Apply uppercase to inputs/textareas with class "to-upper" both visually and in value
document.addEventListener('DOMContentLoaded', () => {
  const upperize = (el) => {
    if (!el || typeof el.value !== 'string') return;
    const start = el.selectionStart;
    const end = el.selectionEnd;
    const up = el.value.toLocaleUpperCase('el-GR');
    if (el.value !== up) {
      el.value = up;
      try { if (start != null && end != null) el.setSelectionRange(start, end); } catch {}
    }
  };

  const attach = (el) => {
    el.classList.add('to-upper'); // ensure visual
    el.addEventListener('input', () => upperize(el));
    el.addEventListener('change', () => upperize(el));
    el.addEventListener('blur', () => upperize(el));
  };

  document.querySelectorAll('input.to-upper, textarea.to-upper').forEach(attach);

  // Safety: ensure on submit all to-upper fields are uppercased
  document.querySelectorAll('form').forEach(f => {
    f.addEventListener('submit', () => {
      f.querySelectorAll('input.to-upper, textarea.to-upper').forEach(upperize);
    });
  });
});

