<template>
  <div class="branding-footer" :class="{ 'branding-footer--dark': variant === 'dark', 'branding-footer--light': variant === 'light' }">
    <span class="branding-prefix breathing-text">powered by</span>
    <a href="https://github.com/DearJIAN" target="_blank" rel="noopener" class="branding-link branding-dearjian breathing-glow" aria-label="DearJIAN on GitHub">
      DearJIAN
    </a>
    <span class="branding-sep pulse-dot">|</span>
    <a href="https://github.com/DearJIAN/SmartLearningSpace_powered_by_DearJIAN" target="_blank" rel="noopener" class="branding-link branding-project breathing-glow-delay" aria-label="SmartLearningSpace on GitHub">
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
  transform: scale(1.08);
  filter: brightness(1.2);
}

/* Dark variant links */
.branding-footer--dark .branding-dearjian {
  background: linear-gradient(90deg, #a78bfa, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #a78bfa;
  text-shadow: 0 0 10px rgba(167, 139, 250, 0.3);
}

.branding-footer--dark .branding-project {
  background: linear-gradient(90deg, #34d399, #6ee7b7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #34d399;
  text-shadow: 0 0 10px rgba(52, 211, 153, 0.3);
}

/* Light variant links */
.branding-footer--light .branding-dearjian {
  background: linear-gradient(90deg, #6366f1, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #6366f1;
  text-shadow: 0 0 10px rgba(99, 102, 241, 0.3);
}

.branding-footer--light .branding-project {
  background: linear-gradient(90deg, #10b981, #34d399);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: #10b981;
  text-shadow: 0 0 10px rgba(16, 185, 129, 0.3);
}

/* ============================================
   Breathing Animation Effects
   ============================================ */

/* Text breathing - opacity pulse */
@keyframes breathing-text {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.9; }
}

.breathing-text {
  animation: breathing-text 2s ease-in-out infinite;
}

/* Glow breathing - text shadow pulse */
@keyframes breathing-glow {
  0%, 100% {
    opacity: 0.7;
    filter: brightness(1) drop-shadow(0 0 2px currentColor);
  }
  50% {
    opacity: 1;
    filter: brightness(1.3) drop-shadow(0 0 8px currentColor);
  }
}

.breathing-glow {
  animation: breathing-glow 2.5s ease-in-out infinite;
}

.breathing-glow-delay {
  animation: breathing-glow 2.5s ease-in-out infinite;
  animation-delay: 0.5s;
}

/* Separator pulse */
@keyframes pulse-dot {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.2); }
}

.pulse-dot {
  animation: pulse-dot 1.5s ease-in-out infinite;
}

/* Hover pause animation */
.branding-footer:hover .breathing-text,
.branding-footer:hover .breathing-glow,
.branding-footer:hover .breathing-glow-delay,
.branding-footer:hover .pulse-dot {
  animation-play-state: paused;
}
</style>
