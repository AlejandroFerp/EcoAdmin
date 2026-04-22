module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        eco: {
          primary: '#3b82f6',
          'primary-hover': '#2563eb',
          'primary-dark': '#1d4ed8',
          danger: '#ef4444',
          'danger-hover': '#dc2626',
          bg: '#f1f5f9',
          surface: '#ffffff',
          border: '#e2e8f0',
          'border-light': '#f1f5f9',
          'text-strong': '#0f172a',
          text: '#1e293b',
          'text-muted': '#475569',
          'text-subtle': '#94a3b8',
        }
      },
      borderRadius: {
        'eco-sm': '0.375rem',
        'eco': '0.625rem',
        'eco-lg': '0.75rem',
        'eco-xl': '1rem',
        'eco-2xl': '1.25rem',
      },
      boxShadow: {
        'eco-card': '0 1px 2px rgba(15,23,42,.05), 0 1px 3px rgba(15,23,42,.04)',
        'eco-modal': '0 20px 60px rgba(15,23,42,.18), 0 4px 16px rgba(15,23,42,.08)',
        'eco-btn': '0 1px 2px rgba(59,130,246,.18), 0 4px 12px -2px rgba(59,130,246,.28)',
      }
    },
  },
  plugins: [
    require('daisyui'),
  ],
  daisyui: {
    themes: ["light"],
    logs: false,
  }
}
