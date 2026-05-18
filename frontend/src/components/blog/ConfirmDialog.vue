<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { _confirmState } from '@/composables/useConfirm'

function onConfirm() {
  _confirmState.resolve?.(true)
  _confirmState.visible = false
  _confirmState.resolve = null
}

function onCancel() {
  _confirmState.resolve?.(false)
  _confirmState.visible = false
  _confirmState.resolve = null
}

function onOverlayClick(e: MouseEvent) {
  if (e.target === e.currentTarget) onCancel()
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && _confirmState.visible) onCancel()
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <Transition name="confirm">
      <div v-if="_confirmState.visible" class="confirm-overlay" @click="onOverlayClick">
        <div class="confirm-card">
          <div class="confirm-title">{{ _confirmState.title }}</div>
          <div class="confirm-message">{{ _confirmState.message }}</div>
          <div class="confirm-actions">
            <button class="confirm-btn confirm-btn-cancel" @click="onCancel">取消</button>
            <button class="confirm-btn confirm-btn-confirm" @click="onConfirm">确定</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5000;
}

.confirm-card {
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 28px 32px 20px;
  max-width: 380px;
  width: 90vw;
}

[data-theme='dark'] .confirm-card {
  background: #2a2a2a;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.confirm-title {
  font-family: var(--font-heading);
  font-size: 1.1em;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 12px;
}

.confirm-message {
  font-size: 0.95em;
  color: var(--text-main);
  line-height: 1.6;
  margin-bottom: 24px;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.confirm-btn {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  font-size: 0.9em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.confirm-btn-cancel {
  background: #f0f0f0;
  color: #333;
}

.confirm-btn-cancel:hover {
  background: #e0e0e0;
}

[data-theme='dark'] .confirm-btn-cancel {
  background: #3a3a3a;
  color: #ccc;
}

[data-theme='dark'] .confirm-btn-cancel:hover {
  background: #4a4a4a;
}

.confirm-btn-confirm {
  background: #1a1a1a;
  color: #fff;
}

.confirm-btn-confirm:hover {
  background: #333;
}

[data-theme='dark'] .confirm-btn-confirm {
  background: var(--accent);
  color: #fff;
}

[data-theme='dark'] .confirm-btn-confirm:hover {
  background: var(--accent-strong);
}

/* ---- animations ---- */
.confirm-enter-active {
  transition: opacity 0.2s ease;
}
.confirm-leave-active {
  transition: opacity 0.15s ease;
}

.confirm-enter-active .confirm-card {
  animation: confirm-card-in 0.2s ease;
}
.confirm-leave-active .confirm-card {
  animation: confirm-card-out 0.15s ease;
}

.confirm-enter-from,
.confirm-leave-to {
  opacity: 0;
}

@keyframes confirm-card-in {
  from {
    transform: scale(0.95);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes confirm-card-out {
  from {
    transform: scale(1);
    opacity: 1;
  }
  to {
    transform: scale(0.95);
    opacity: 0;
  }
}

/* ---- mobile ---- */
@media (max-width: 900px) {
  .confirm-card {
    max-width: calc(100vw - 24px);
    padding: 24px 20px 16px;
  }

  .confirm-btn {
    flex: 1;
    padding: 10px 0;
    text-align: center;
  }
}
</style>
