<script setup lang="ts">
import { ref } from 'vue'

// ============ Types ============
interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

// ============ State ============
const inputText = ref('')
const resultText = ref('')
const isLoading = ref(false)
const isSpeaking = ref(false)
const error = ref('')
const toasts = ref<Toast[]>([])
let toastId = 0

// ============ Toast ============
function showToast(message: string, type: Toast['type'] = 'info') {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 3000)
}

// ============ Translation ============
async function doTranslate() {
  const text = inputText.value.trim()
  if (!text) {
    error.value = '请输入要翻译的文本'
    showToast('请输入要翻译的文本', 'info')
    return
  }

  isLoading.value = true
  error.value = ''
  resultText.value = ''

  try {
    const response = await fetch('/api/translate/text', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text }),
    })

    const data = await response.json()

    if (data.code !== 0) {
      throw new Error(data.message || '翻译失败')
    }

    resultText.value = data.data
    showToast('翻译成功', 'success')
  } catch (e) {
    const message = e instanceof Error ? e.message : '翻译请求失败'
    error.value = message
    showToast(message, 'error')
  } finally {
    isLoading.value = false
  }
}

// ============ Text-to-Speech ============
function speak() {
  const text = inputText.value.trim()
  if (!text) return

  if (!window.speechSynthesis) {
    showToast('当前浏览器不支持语音合成', 'error')
    return
  }

  if (isSpeaking.value) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    return
  }

  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'en-US'
  utterance.rate = 0.9
  utterance.pitch = 1.0
  utterance.onstart = () => { isSpeaking.value = true }
  utterance.onend = () => { isSpeaking.value = false }
  utterance.onerror = () => { isSpeaking.value = false }
  window.speechSynthesis.speak(utterance)
}

// ============ Copy ============
function copyResult() {
  if (!resultText.value) {
    showToast('没有可复制的内容', 'info')
    return
  }
  navigator.clipboard.writeText(resultText.value).then(() => {
    showToast('已复制到剪贴板', 'success')
  }).catch(() => {
    showToast('复制失败，请手动选择文本复制', 'error')
  })
}

function clearInput() {
  inputText.value = ''
  resultText.value = ''
  error.value = ''
  showToast('已清空', 'info')
}
</script>

<template>
  <div class="translate-tool">
    <!-- Main Content -->
    <div class="main-split">
      <!-- Left Panel: Input -->
      <div class="left-panel">
        <div class="panel-label">原文</div>
        <div class="editor-wrap">
          <textarea
            v-model="inputText"
            class="editor"
            placeholder="输入英文或中文单词、短语..."
            spellcheck="false"
          ></textarea>
        </div>
        <div class="status-bar">
          <span class="stat">{{ inputText.length }} 字符</span>
          <span class="stat-divider" />
          <span class="stat">{{ inputText ? (inputText.match(/\S+/g) || []).length : 0 }} 词</span>
        </div>
        <div class="action-bar">
          <button
            class="btn-toolbar primary"
            :disabled="isLoading || !inputText.trim()"
            @click="doTranslate"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
              <path d="M12 2a10 10 0 1 0 10 10"/><path d="M2 12h18"/><path d="M12 2c1.7 2 3 4.5 3.5 7"/><path d="M12 2c-1.7 2-3 4.5-3.5 7"/>
              <path d="M8.5 12c.5 2.5 1.8 5 3.5 7m3.5-7c-.5 2.5-1.8 5-3.5 7"/>
            </svg>
            {{ isLoading ? '翻译中...' : '翻译' }}
          </button>
          <button
            class="btn-toolbar speak"
            :disabled="!inputText.trim()"
            @click="speak"
          >
            <svg v-if="!isSpeaking" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
              <path d="M11 5 6 9H2v6h4l5 4V5z"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14"/><path d="M15.54 8.46a5 5 0 0 1 0 7.07"/>
            </svg>
            <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
              <rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/>
            </svg>
            {{ isSpeaking ? '停止' : '朗读原文' }}
          </button>
          <span class="action-spacer" />
          <button
            class="btn-toolbar"
            :disabled="!inputText && !resultText"
            @click="clearInput"
          >
            清空
          </button>
        </div>

        <div v-if="error" class="error-bar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/>
          </svg>
          <span>{{ error }}</span>
        </div>
      </div>

      <!-- Right Panel: Output -->
      <div class="right-panel">
        <div class="panel-label">
          译文
          <button
            v-if="resultText"
            class="btn-copy-icon"
            title="复制译文"
            @click="copyResult"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
          </button>
        </div>

        <div class="output-wrap">
          <!-- Loading -->
          <div v-if="isLoading" class="output-loading">
            <div class="spinner"></div>
            <p>正在翻译...</p>
          </div>

          <!-- Result -->
          <div v-else-if="resultText" class="output-content">
            <pre class="result-text">{{ resultText }}</pre>
          </div>

          <!-- Empty State -->
          <div v-else class="output-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.3">
              <path d="M12 2a10 10 0 1 0 10 10"/><path d="M2 12h18"/><path d="M12 2c1.7 2 3 4.5 3.5 7"/><path d="M12 2c-1.7 2-3 4.5-3.5 7"/>
              <path d="M8.5 12c.5 2.5 1.8 5 3.5 7m3.5-7c-.5 2.5-1.8 5-3.5 7"/>
            </svg>
            <p>在左侧输入文本后<br/>点击"翻译"查看结果</p>
          </div>
        </div>

        <div class="status-bar">
          <span v-if="resultText" class="stat">{{ resultText.length }} 字符</span>
          <span v-if="resultText" class="stat-divider" />
          <span v-if="resultText" class="stat">{{ (resultText.match(/\S+/g) || []).length }} 词</span>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          :class="['toast', `toast-${t.type}`]"
        >
          {{ t.message }}
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<style scoped>
/* ===================== Design Tokens ===================== */
.translate-tool {
  --t-bg: #f8fafc;
  --t-surface: #ffffff;
  --t-border: #e2e8f0;
  --t-text: #1e293b;
  --t-text-secondary: #64748b;
  --t-accent: #3b82f6;
  --t-accent-hover: #2563eb;
  --t-success: #10b981;
  --t-error: #ef4444;
  --t-info: #3b82f6;
  --t-editor-bg: #f5f0e1;
  --t-editor-text: #4a3f35;
  --t-radius: 12px;
  --t-radius-sm: 8px;
  --t-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);

  min-height: 600px;
  display: flex;
  flex-direction: column;
}

/* ===================== Split Layout ===================== */
.main-split {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 0;
}

/* ===================== Left Panel ===================== */
.left-panel {
  display: flex;
  flex-direction: column;
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius);
  overflow: hidden;
}

.panel-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--t-border);
  background: #f1f5f9;
  font-size: 0.84em;
  font-weight: 600;
  color: var(--t-text);
  flex-shrink: 0;
}

.editor-wrap {
  flex: 1;
  position: relative;
  min-height: 200px;
}

.editor {
  width: 100%;
  height: 100%;
  min-height: 200px;
  padding: 16px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.7;
  tab-size: 2;
  background: var(--t-editor-bg);
  color: var(--t-editor-text);
  caret-color: #b8a58a;
}

.editor::placeholder {
  color: #b8a58a;
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-top: 1px solid var(--t-border);
  background: #f1f5f9;
  font-size: 0.78em;
  color: var(--t-text-secondary);
  flex-shrink: 0;
}

.stat-divider {
  width: 1px;
  height: 12px;
  background: var(--t-border);
}

.action-bar {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid var(--t-border);
  background: #f1f5f9;
  flex-shrink: 0;
}

.btn-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--t-border);
  border-radius: 8px;
  background: var(--t-surface);
  color: var(--t-text-secondary);
  font-size: 0.88em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.12s;
  font-family: inherit;
  white-space: nowrap;
}

.btn-toolbar:hover:not(:disabled) {
  border-color: var(--t-accent);
  color: var(--t-accent);
}

.btn-toolbar:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-toolbar.primary {
  background: var(--t-accent);
  color: #fff;
  border-color: var(--t-accent);
}

.btn-toolbar.primary:hover:not(:disabled) {
  background: var(--t-accent-hover);
}

.btn-toolbar.speak:hover:not(:disabled) {
  border-color: var(--t-success);
  color: var(--t-success);
}

.action-spacer {
  flex: 1;
}

.error-bar {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid #fecaca;
  background: #fef2f2;
  font-size: 0.84em;
  color: #dc2626;
  line-height: 1.5;
  flex-shrink: 0;
  word-break: break-word;
}

.error-bar svg {
  flex-shrink: 0;
  margin-top: 1px;
}

/* ===================== Right Panel ===================== */
.right-panel {
  display: flex;
  flex-direction: column;
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius);
  overflow: hidden;
}

.btn-copy-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border: 1px solid var(--t-border);
  border-radius: 4px;
  background: var(--t-surface);
  color: var(--t-text-secondary);
  cursor: pointer;
  transition: all 0.12s;
  font-family: inherit;
}

.btn-copy-icon:hover {
  border-color: var(--t-success);
  color: var(--t-success);
}

.output-wrap {
  flex: 1;
  overflow: auto;
  position: relative;
  min-height: 200px;
}

.output-content {
  padding: 20px;
}

.result-text {
  margin: 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.8;
  color: var(--t-text);
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Loading */
.output-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: var(--t-text-secondary);
  font-size: 0.92em;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--t-border);
  border-top-color: var(--t-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Empty State */
.output-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  padding: 40px 20px;
  text-align: center;
  color: var(--t-text-secondary);
  font-size: 0.92em;
  background: var(--t-bg);
  line-height: 1.6;
}

/* ===================== Toast ===================== */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 9999;
  pointer-events: none;
}

.toast {
  padding: 10px 20px;
  border-radius: var(--t-radius-sm);
  font-size: 0.88em;
  font-weight: 500;
  color: #fff;
  pointer-events: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.toast-success {
  background: var(--t-success);
}

.toast-error {
  background: var(--t-error);
}

.toast-info {
  background: var(--t-info);
}

.toast-enter-active {
  transition: all 0.25s ease;
}

.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* ===================== Responsive ===================== */
@media (max-width: 900px) {
  .main-split {
    grid-template-columns: 1fr;
  }

  .left-panel,
  .right-panel {
    min-height: 250px;
  }
}
</style>
