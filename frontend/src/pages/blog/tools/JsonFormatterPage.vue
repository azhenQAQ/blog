<script setup lang="ts">
import { ref, computed } from 'vue'

// ============ Types ============
type ViewMode = 'pretty' | 'minified' | 'tree'

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

// ============ State ============
const inputText = ref('')
const activeView = ref<ViewMode>('pretty')
const error = ref('')
const toasts = ref<Toast[]>([])
let toastId = 0

// ============ Computed ============
const prettyOutput = computed(() => {
  try {
    return JSON.stringify(JSON.parse(inputText.value), null, 2)
  } catch {
    return ''
  }
})

const minifiedOutput = computed(() => {
  try {
    return JSON.stringify(JSON.parse(inputText.value))
  } catch {
    return ''
  }
})

const lineCount = computed(() => {
  if (!inputText.value) return 0
  return inputText.value.split('\n').length
})

const charCount = computed(() => inputText.value.length)

const nodeCount = computed(() => {
  try {
    return countNodes(JSON.parse(inputText.value))
  } catch {
    return 0
  }
})

// ============ Toast ============
function showToast(message: string, type: Toast['type'] = 'info') {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 3000)
}

// ============ Actions ============
function format() {
  if (!inputText.value.trim()) {
    error.value = '请输入 JSON 内容'
    return
  }
  try {
    JSON.parse(inputText.value)
    activeView.value = 'pretty'
    error.value = ''
    showToast('格式化成功', 'success')
  } catch (e) {
    error.value = (e as SyntaxError).message
    showToast('JSON 格式错误，无法格式化', 'error')
  }
}

function minify() {
  if (!inputText.value.trim()) {
    error.value = '请输入 JSON 内容'
    return
  }
  try {
    JSON.parse(inputText.value)
    activeView.value = 'minified'
    error.value = ''
    showToast('压缩成功', 'success')
  } catch (e) {
    error.value = (e as SyntaxError).message
    showToast('JSON 格式错误，无法压缩', 'error')
  }
}

function validate() {
  if (!inputText.value.trim()) {
    error.value = '请输入 JSON 内容'
    return
  }
  try {
    JSON.parse(inputText.value)
    error.value = ''
    showToast('JSON 格式正确 ✓', 'success')
  } catch (e) {
    error.value = (e as SyntaxError).message
    showToast('JSON 格式错误', 'error')
  }
}

function escapeJson() {
  if (!inputText.value.trim()) return
  try {
    JSON.parse(inputText.value)
    inputText.value = JSON.stringify(inputText.value)
    error.value = ''
    activeView.value = 'pretty'
    showToast('转义成功', 'success')
  } catch (e) {
    error.value = (e as SyntaxError).message
    showToast('JSON 格式错误，无法转义', 'error')
  }
}

function unescapeJson() {
  if (!inputText.value.trim()) return
  try {
    const result = JSON.parse(inputText.value)
    if (typeof result === 'string') {
      inputText.value = result
      error.value = ''
      showToast('反转义成功', 'success')
    } else {
      error.value = '反转义要求输入为一个 JSON 字符串（用双引号包裹）'
      showToast('输入不是 JSON 字符串', 'error')
    }
  } catch (e) {
    error.value = (e as SyntaxError).message
    showToast('反转义失败', 'error')
  }
}

function copyResult() {
  let text = ''
  if (activeView.value === 'pretty') text = prettyOutput.value
  else if (activeView.value === 'minified') text = minifiedOutput.value
  else text = inputText.value

  if (!text) {
    showToast('没有可复制的内容', 'info')
    return
  }

  navigator.clipboard.writeText(text).then(() => {
    showToast('已复制到剪贴板', 'success')
  })
}

function clearInput() {
  inputText.value = ''
  error.value = ''
  showToast('已清空', 'info')
}

function handleTabInput(e: KeyboardEvent) {
  if (e.key === 'Tab') {
    e.preventDefault()
    const target = e.target as HTMLTextAreaElement
    const start = target.selectionStart
    const end = target.selectionEnd
    inputText.value = inputText.value.substring(0, start) + '  ' + inputText.value.substring(end)
    requestAnimationFrame(() => {
      target.selectionStart = target.selectionEnd = start + 2
    })
  }
}

// ============ Helpers ============
function countNodes(data: unknown): number {
  let count = 1
  if (data && typeof data === 'object') {
    if (Array.isArray(data)) {
      for (const item of data) count += countNodes(item)
    } else {
      for (const val of Object.values(data as Record<string, unknown>)) count += countNodes(val)
    }
  }
  return count
}

// ============ Syntax Highlighting ============
function highlightJson(text: string): string {
  if (!text) return ''
  // Escape HTML first
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // Tokenize and wrap with spans
  const tokens: { type: string; value: string }[] = []
  let i = 0
  const len = escaped.length

  while (i < len) {
    // Skip whitespace
    if (/^\s/.test(escaped[i])) {
      let ws = ''
      while (i < len && /^\s$/.test(escaped[i])) ws += escaped[i++]
      tokens.push({ type: 'ws', value: ws })
      continue
    }

    // String (key or value)
    if (escaped[i] === '"') {
      let str = '"'
      i++
      while (i < len) {
        if (escaped[i] === '\\' && i + 1 < len) {
          str += escaped[i] + escaped[i + 1]
          i += 2
        } else if (escaped[i] === '"') {
          str += '"'
          i++
          break
        } else {
          str += escaped[i]
          i++
        }
      }
      // Check if this is a key (followed by colon, possibly with whitespace)
      let after = ''
      let j = i
      while (j < len && /^\s$/.test(escaped[j])) after += escaped[j++]
      if (j < len && escaped[j] === ':') {
        tokens.push({ type: 'key', value: str })
        tokens.push({ type: 'ws', value: after })
        tokens.push({ type: 'punc', value: ':' })
        i = j + 1
      } else {
        tokens.push({ type: 'string', value: str })
      }
      continue
    }

    // Number
    if (/^-?\d/.test(escaped[i])) {
      let num = ''
      while (i < len && /^[-.\deE+]/.test(escaped[i])) num += escaped[i++]
      tokens.push({ type: 'number', value: num })
      continue
    }

    // Boolean
    if (escaped.substring(i, i + 4) === 'true') {
      tokens.push({ type: 'bool', value: 'true' })
      i += 4
      continue
    }
    if (escaped.substring(i, i + 5) === 'false') {
      tokens.push({ type: 'bool', value: 'false' })
      i += 5
      continue
    }

    // Null
    if (escaped.substring(i, i + 4) === 'null') {
      tokens.push({ type: 'null', value: 'null' })
      i += 4
      continue
    }

    // Punctuation
    if (/^[\[\]{},]/.test(escaped[i])) {
      tokens.push({ type: 'punc', value: escaped[i++] })
      continue
    }

    // Fallback
    tokens.push({ type: 'other', value: escaped[i++] })
  }

  return tokens
    .map((t) => {
      switch (t.type) {
        case 'ws':
          return t.value
        case 'key':
          return `<span class="hl-key">${t.value}</span>`
        case 'string':
          return `<span class="hl-string">${t.value}</span>`
        case 'number':
          return `<span class="hl-number">${t.value}</span>`
        case 'bool':
          return `<span class="hl-bool">${t.value}</span>`
        case 'null':
          return `<span class="hl-null">${t.value}</span>`
        case 'punc':
          return `<span class="hl-punc">${t.value}</span>`
        default:
          return t.value
      }
    })
    .join('')
}

// ============ Tree View ============
const collapsedPaths = ref(new Set<string>())

function togglePath(path: string) {
  if (collapsedPaths.value.has(path)) {
    collapsedPaths.value.delete(path)
  } else {
    collapsedPaths.value.add(path)
  }
}

function renderTreeValue(value: unknown, path: string, depth: number): string {
  const indent = '  '.repeat(depth)

  if (value === null) {
    return `<span class="hl-null">null</span>`
  }
  if (typeof value === 'boolean') {
    return `<span class="hl-bool">${value}</span>`
  }
  if (typeof value === 'number') {
    return `<span class="hl-number">${value}</span>`
  }
  if (typeof value === 'string') {
    const esc = value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    return `<span class="hl-string">"${esc}"</span>`
  }
  if (Array.isArray(value)) {
    const isCollapsed = collapsedPaths.value.has(path)
    if (value.length === 0) return '<span class="hl-punc">[ ]</span>'
    if (isCollapsed) {
      return `<span class="tree-toggle" data-path="${path}" data-depth="${depth}"><span class="hl-punc">[</span><span class="hl-ellipsis"> … </span><span class="hl-punc">]</span> <span class="hl-comment">// ${value.length} items</span></span>`
    }
    let html = `<span class="tree-toggle expanded" data-path="${path}" data-depth="${depth}"><span class="hl-punc">[</span></span>\n`
    for (let i = 0; i < value.length; i++) {
      html += `${indent}  <span class="tree-item">`
      html += renderTreeValue(value[i], `${path}[${i}]`, depth + 1)
      html += i < value.length - 1 ? '<span class="hl-punc">,</span>' : ''
      html += '</span>\n'
    }
    html += `${indent}<span class="hl-punc">]</span>`
    return html
  }
  if (typeof value === 'object') {
    const obj = value as Record<string, unknown>
    const keys = Object.keys(obj)
    const isCollapsed = collapsedPaths.value.has(path)
    if (keys.length === 0) return '<span class="hl-punc">{ }</span>'
    if (isCollapsed) {
      return `<span class="tree-toggle" data-path="${path}" data-depth="${depth}"><span class="hl-punc">{</span><span class="hl-ellipsis"> … </span><span class="hl-punc">}</span> <span class="hl-comment">// ${keys.length} keys</span></span>`
    }
    let html = `<span class="tree-toggle expanded" data-path="${path}" data-depth="${depth}"><span class="hl-punc">{</span></span>\n`
    for (let i = 0; i < keys.length; i++) {
      const k = keys[i]
      const escKey = k.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      html += `${indent}  <span class="tree-item"><span class="hl-key">"${escKey}"</span><span class="hl-punc">: </span>`
      html += renderTreeValue(obj[k], `${path}.${k}`, depth + 1)
      html += i < keys.length - 1 ? '<span class="hl-punc">,</span>' : ''
      html += '</span>\n'
    }
    html += `${indent}<span class="hl-punc">}</span>`
    return html
  }
  return String(value)
}

const treeOutput = computed(() => {
  try {
    const data = JSON.parse(inputText.value)
    return renderTreeValue(data, '$', 0)
  } catch {
    return ''
  }
})

function handleTreeClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  const toggle = target.closest('.tree-toggle')
  if (toggle && toggle.getAttribute('data-path')) {
    const path = toggle.getAttribute('data-path')!
    e.preventDefault()
    togglePath(path)
  }
}
</script>

<template>
  <div class="json-tool">
    <div class="tool-header">
      <div class="header-right">
        <button class="btn-action" @click="clearInput" :disabled="!inputText">清空</button>
      </div>
    </div>

    <div class="main-split">
      <!-- Left Panel: Input -->
      <div class="left-panel">
        <div class="toolbar">
          <button class="btn-toolbar primary" title="格式化 (Pretty Print)" @click="format">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M21 10H3"/><path d="M21 6H3"/><path d="M21 14H3"/><path d="M21 18H3"/></svg>
            格式化
          </button>
          <button class="btn-toolbar" title="压缩 (Minify)" @click="minify">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M8 3v3a2 2 0 0 1-2 2H3m0 0v11a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2h-1"/><path d="M3 8h4a2 2 0 0 0 2-2V3"/></svg>
            压缩
          </button>
          <button class="btn-toolbar" title="校验 (Validate)" @click="validate">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="m9 12 2 2 4-4"/></svg>
            校验
          </button>
          <button class="btn-toolbar" title="转义 (Escape)" @click="escapeJson">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M17 7 7 17"/><path d="M7 7h10v10"/></svg>
            转义
          </button>
          <button class="btn-toolbar" title="反转义 (Unescape)" @click="unescapeJson">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M7 17 17 7"/><path d="M7 7h10v10"/></svg>
            反转义
          </button>
          <span class="toolbar-spacer" />
          <button class="btn-toolbar btn-copy" title="复制结果" @click="copyResult">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
            复制
          </button>
        </div>

        <div class="editor-wrap">
          <textarea
            v-model="inputText"
            class="editor"
            placeholder="在此粘贴或输入 JSON 内容..."
            spellcheck="false"
            @keydown="handleTabInput"
          ></textarea>
        </div>

        <div class="status-bar">
          <span class="stat">{{ lineCount }} 行</span>
          <span class="stat-divider" />
          <span class="stat">{{ charCount }} 字符</span>
        </div>

        <div v-if="error" class="error-bar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
          <span>{{ error }}</span>
        </div>
      </div>

      <!-- Right Panel: Output -->
      <div class="right-panel">
        <div class="view-tabs">
          <button
            :class="{ active: activeView === 'pretty' }"
            @click="activeView = 'pretty'"
          >美化</button>
          <button
            :class="{ active: activeView === 'minified' }"
            @click="activeView = 'minified'"
          >压缩</button>
          <button
            :class="{ active: activeView === 'tree' }"
            @click="activeView = 'tree'"
          >树形</button>
        </div>

        <div class="output-wrap">
          <!-- Pretty View -->
          <div v-show="activeView === 'pretty'" class="output-content">
            <pre v-if="prettyOutput" class="highlight-pre"><code v-html="highlightJson(prettyOutput)"></code></pre>
            <div v-else class="output-empty">
              <p>输入 JSON 内容后，点击"格式化"查看美化结果</p>
            </div>
          </div>

          <!-- Minified View -->
          <div v-show="activeView === 'minified'" class="output-content">
            <pre v-if="minifiedOutput" class="highlight-pre"><code>{{ minifiedOutput }}</code></pre>
            <div v-else class="output-empty">
              <p>输入 JSON 内容后，点击"压缩"查看压缩结果</p>
            </div>
          </div>

          <!-- Tree View -->
          <div v-show="activeView === 'tree'" class="output-content">
            <div v-if="treeOutput" class="tree-view" @click="handleTreeClick">
              <pre class="highlight-pre"><code v-html="treeOutput"></code></pre>
            </div>
            <div v-else class="output-empty">
              <p>输入 JSON 内容后，切换到树形视图浏览数据结构</p>
            </div>
          </div>
        </div>

        <div class="status-bar">
          <span v-if="prettyOutput" class="stat">{{ nodeCount }} 个节点</span>
          <span v-if="prettyOutput" class="stat-divider" />
          <span v-if="prettyOutput" class="stat">{{ activeView === 'minified' ? minifiedOutput.length : prettyOutput.length }} 字符</span>
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
.json-tool {
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
  --t-editor-bg: #1e1e2e;
  --t-editor-text: #cdd6f4;
  --t-radius: 12px;
  --t-radius-sm: 8px;
  --t-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);

  --hl-key: #89b4fa;
  --hl-string: #a6e3a1;
  --hl-number: #fab387;
  --hl-bool: #cba6f7;
  --hl-null: #cba6f7;
  --hl-punc: #9399b2;

  min-height: 600px;
  display: flex;
  flex-direction: column;
}

/* ===================== Header ===================== */
.tool-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.header-right {
  display: flex;
  gap: 8px;
}

.btn-action {
  padding: 6px 14px;
  border: 1px solid var(--t-border);
  border-radius: 6px;
  background: var(--t-surface);
  color: var(--t-text-secondary);
  font-size: 0.84em;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}

.btn-action:hover:not(:disabled) {
  border-color: var(--t-error);
  color: var(--t-error);
}

.btn-action:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 10px;
  border-bottom: 1px solid var(--t-border);
  background: #f1f5f9;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.btn-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: 1px solid var(--t-border);
  border-radius: 6px;
  background: var(--t-surface);
  color: var(--t-text-secondary);
  font-size: 0.82em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.12s;
  font-family: inherit;
  white-space: nowrap;
}

.btn-toolbar:hover {
  border-color: var(--t-accent);
  color: var(--t-accent);
}

.btn-toolbar.primary {
  background: var(--t-accent);
  color: #fff;
  border-color: var(--t-accent);
}

.btn-toolbar.primary:hover {
  background: var(--t-accent-hover);
}

.btn-toolbar.btn-copy:hover {
  border-color: var(--t-success);
  color: var(--t-success);
}

.toolbar-spacer {
  flex: 1;
}

.editor-wrap {
  flex: 1;
  position: relative;
  min-height: 300px;
}

.editor {
  width: 100%;
  height: 100%;
  min-height: 300px;
  padding: 16px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.65;
  tab-size: 2;
  background: var(--t-editor-bg);
  color: var(--t-editor-text);
  caret-color: #f5e0dc;
}

.editor::placeholder {
  color: #585b70;
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

.view-tabs {
  display: flex;
  border-bottom: 1px solid var(--t-border);
  flex-shrink: 0;
}

.view-tabs button {
  flex: 1;
  padding: 10px 0;
  border: none;
  background: none;
  font-size: 0.85em;
  font-weight: 500;
  color: var(--t-text-secondary);
  cursor: pointer;
  transition: all 0.15s;
  border-bottom: 2px solid transparent;
  font-family: inherit;
}

.view-tabs button:hover {
  color: var(--t-text);
}

.view-tabs button.active {
  color: var(--t-accent);
  border-bottom-color: var(--t-accent);
}

.output-wrap {
  flex: 1;
  overflow: auto;
  position: relative;
}

.output-content {
  position: absolute;
  inset: 0;
  overflow: auto;
}

.highlight-pre {
  margin: 0;
  padding: 16px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.65;
  tab-size: 2;
  background: var(--t-editor-bg);
  color: var(--t-editor-text);
  min-height: 100%;
  overflow: visible;
}

.highlight-pre code {
  font-family: inherit;
}

/* Syntax Highlighting Colors */
:deep(.hl-key) {
  color: var(--hl-key);
}
:deep(.hl-string) {
  color: var(--hl-string);
}
:deep(.hl-number) {
  color: var(--hl-number);
}
:deep(.hl-bool) {
  color: var(--hl-bool);
}
:deep(.hl-null) {
  color: var(--hl-null);
}
:deep(.hl-punc) {
  color: var(--hl-punc);
}
:deep(.hl-comment) {
  color: #6c7086;
  font-style: italic;
}
:deep(.hl-ellipsis) {
  color: #6c7086;
}

/* Tree View */
.tree-view {
  cursor: default;
}

.tree-view :deep(.tree-toggle) {
  cursor: pointer;
  user-select: none;
}

.tree-view :deep(.tree-toggle::before) {
  content: '▶';
  display: inline-block;
  width: 14px;
  font-size: 9px;
  color: #6c7086;
  transition: transform 0.1s;
  margin-right: 2px;
}

.tree-view :deep(.tree-toggle.expanded::before) {
  content: '▼';
}

.tree-view :deep(.tree-item) {
  display: inline;
}

/* Empty State */
.output-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px 20px;
  text-align: center;
  color: var(--t-text-secondary);
  font-size: 0.92em;
  background: var(--t-bg);
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
    min-height: 350px;
  }

  .toolbar {
    gap: 2px;
    padding: 6px 8px;
  }

  .btn-toolbar {
    padding: 5px 8px;
    font-size: 0.78em;
  }
}
</style>