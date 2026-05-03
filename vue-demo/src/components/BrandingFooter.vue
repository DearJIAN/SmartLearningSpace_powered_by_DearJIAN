<template>
  <div class="branding-footer" :class="{ 'branding-footer--dark': variant === 'dark', 'branding-footer--light': variant === 'light' }">
    <span class="branding-prefix">Powered by</span>
    <a href="https://github.com/DearJIAN" target="_blank" rel="noopener" class="branding-link branding-dearjian" aria-label="DearJIAN on GitHub">
      DearJIAN
    </a>
    <span class="branding-sep">|</span>
    <a href="https://github.com/DearJIAN/SmartLearningSpace_powered_by_DearJIAN" target="_blank" rel="noopener" class="branding-link branding-project" aria-label="SmartLearningSpace on GitHub">
      SmartLearningSpace
    </a>
  </div>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'dark',
    validator: (v) => ['dark', 'light'].includes(v)
  }
})
</script>

<style scoped>
.branding-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 0 4px;
  font-size: 12px;
  line-height: 1;
  user-select: none;
}

.branding-prefix {
  font-size: 11px;
  letter-spacing: 0.3px;
  opacity: 0.6;
}

.branding-sep {
  opacity: 0.35;
  font-size: 10px;
}

/* Dark variant (for dark/gradient backgrounds — login page) */
.branding-footer--dark {
  color: rgba(255, 255, 255, 0.5);
}
.branding-footer--dark .branding-prefix { color: rgba(255, 255, 255, 0.5); }

/* Light variant (for light/white backgrounds — main app) */
.branding-footer--light {
  color: rgba(99, 102, 241, 0.5);
}
.branding-footer--light .branding-prefix { color: rgba(99, 102, 241, 0.5); }

/* Link styling */
.branding-link {
  font-weight: 600;
  text-decoration: none;
  transition: transform 0.2s ease, filter 0.2s ease, opacity 0.2s ease;
  cursor: pointer;
}

.branding-link:hover {
  transform: scale(1.06);
  filter: brightness(1.15);
}

/* Dark variant links */
.branding-footer--dark .branding-dearjian {
  background: linear-gradient(90deg, #a78bfa, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #a78bfa;
}

.branding-footer--dark .branding-project {
  background: linear-gradient(90deg, #34d399, #6ee7b7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #34d399;
}

/* Light variant links */
.branding-footer--light .branding-dearjian {
  background: linear-gradient(90deg, #6366f1, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #6366f1;
}

.branding-footer--light .branding-project {
  background: linear-gradient(90deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #10b981;
}
</style>
