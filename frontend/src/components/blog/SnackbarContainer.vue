<script setup lang="ts">
import { _snackbarItems } from '@/composables/useSnackbar'

const typeIcon: Record<string, string> = {
  success: '✓',
  info: 'ℹ',
  warning: '⚠',
  error: '✗',
}
</script>

<template>
  <Teleport to="body">
    <TransitionGroup name="snackbar" tag="div" class="snackbar-container">
      <div
        v-for="item in _snackbarItems"
        :key="item.id"
        class="snackbar-item"
        :class="'snackbar-' + item.type"
      >
        <span class="snackbar-icon" :class="'icon-' + item.type">{{ typeIcon[item.type] }}</span>
        <span class="snackbar-text">{{ item.message }}</span>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<style scoped>
.snackbar-container {
  position: fixed;
  top: 24px;
  right: 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 6000;
  pointer-events: none;
}

.snackbar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  min-width: 200px;
  max-width: 360px;
  border-radius: 10px;
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  font-size: 0.9em;
  color: #333;
  pointer-events: auto;
  border-left: 4px solid #999;
}

[data-theme='dark'] .snackbar-item {
  background: #2a2a2a;
  color: #d0d0d0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

.snackbar-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95em;
  font-weight: 700;
}

/* ---- type colors ---- */
.snackbar-success { border-left-color: #3a9d4a; }
.icon-success { color: #3a9d4a; }
[data-theme='dark'] .snackbar-success { border-left-color: #4caf50; }
[data-theme='dark'] .icon-success { color: #4caf50; }

.snackbar-info { border-left-color: #00bcd4; }
.icon-info { color: #00bcd4; }
[data-theme='dark'] .snackbar-info { border-left-color: #26c6da; }
[data-theme='dark'] .icon-info { color: #26c6da; }

.snackbar-warning { border-left-color: #ff9800; }
.icon-warning { color: #ff9800; }
[data-theme='dark'] .snackbar-warning { border-left-color: #ffa726; }
[data-theme='dark'] .icon-warning { color: #ffa726; }

.snackbar-error { border-left-color: #ff4c4c; }
.icon-error { color: #ff4c4c; }
[data-theme='dark'] .snackbar-error { border-left-color: #ef5350; }
[data-theme='dark'] .icon-error { color: #ef5350; }

/* ---- animations ---- */
.snackbar-enter-active {
  transition: all 0.3s ease;
}
.snackbar-leave-active {
  transition: all 0.2s ease;
}
.snackbar-enter-from {
  transform: translateX(120%);
  opacity: 0;
}
.snackbar-leave-to {
  opacity: 0;
  transform: translateX(60px);
}
.snackbar-move {
  transition: transform 0.2s ease;
}

/* ---- mobile ---- */
@media (max-width: 900px) {
  .snackbar-container {
    top: 12px;
    right: auto;
    left: 50%;
    transform: translateX(-50%);
    width: calc(100vw - 24px);
  }

  .snackbar-item {
    max-width: none;
    width: 100%;
    padding: 10px 16px;
  }

  .snackbar-enter-from {
    transform: translateY(-100%);
    opacity: 0;
  }
  .snackbar-leave-to {
    opacity: 0;
    transform: translateY(-20px);
  }
}
</style>
