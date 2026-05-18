<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'

// ============ Types ============
type ToolTab = 'compress' | 'crop' | 'convert'
type OutputFormat = 'original' | 'image/png' | 'image/jpeg' | 'image/webp'
type CropPreset = 'free' | '1:1' | '4:3' | '16:9' | '3:2'

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

const CROP_RATIOS: Record<CropPreset, number | null> = {
  free: null,
  '1:1': 1,
  '4:3': 4 / 3,
  '16:9': 16 / 9,
  '3:2': 3 / 2,
}

const CROP_PRESETS: { key: CropPreset; label: string }[] = [
  { key: 'free', label: '自由' },
  { key: '1:1', label: '1:1' },
  { key: '4:3', label: '4:3' },
  { key: '16:9', label: '16:9' },
  { key: '3:2', label: '3:2' },
]

const MAX_SIZE = 20 * 1024 * 1024

const TABS: [ToolTab, string][] = [
  ['compress', '压缩'],
  ['crop', '裁剪'],
  ['convert', '格式转换'],
]

// ============ State ============
const activeTab = ref<ToolTab>('compress')
const showDropZone = ref(true)
const isDragging = ref(false)
const processing = ref(false)

// Image state
const imageFile = ref<File | null>(null)
const imageSrc = ref<string>('')
const imageEl = ref<HTMLImageElement | null>(null)
const imageNatural = ref({ width: 0, height: 0 })

// Compress state
const compressQuality = ref(80)
const compressLockRatio = ref(true)
const compressWidth = ref(0)
const compressHeight = ref(0)
const compressFormat = ref<OutputFormat>('original')
const compressEstimatedSize = ref('')

// Crop state
const cropPreset = ref<CropPreset>('free')
const cropCanvas = ref<HTMLCanvasElement | null>(null)
const cropContainer = ref<HTMLDivElement | null>(null)
const cropResult = ref<Blob | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
let cropX = 0, cropY = 0, cropW = 200, cropH = 200
let isDraggingCrop = false, isResizingCrop = false
let dragStart = { x: 0, y: 0, cx: 0, cy: 0, cw: 0, ch: 0 }
let resizeHandle = ''
let displayScale = 1
let displayOffsetX = 0, displayOffsetY = 0
let imgDisplayW = 0, imgDisplayH = 0

// Convert state
const convertFormat = ref<OutputFormat>('image/png')
const convertQuality = ref(85)
const convertEstimatedSize = ref('')

// Toast
const toasts = ref<Toast[]>([])
let toastId = 0

// ============ Computed ============
const imageExtension = computed(() => {
  if (!imageFile.value) return ''
  return imageFile.value.name.split('.').pop()?.toLowerCase() || ''
})

const imageSizeStr = computed(() => {
  if (!imageFile.value) return ''
  const kb = imageFile.value.size / 1024
  return kb >= 1024 ? `${(kb / 1024).toFixed(2)} MB` : `${kb.toFixed(1)} KB`
})

const imageFormat = computed(() => {
  if (!imageFile.value) return ''
  return imageFile.value.type || '未知'
})

// ============ Toast ============
function showToast(message: string, type: Toast['type'] = 'info') {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 3000)
}

// ============ Image Loading ============
function loadImage(file: File) {
  if (file.size > MAX_SIZE) {
    showToast('文件大小超过 20MB 限制，请选择更小的图片', 'error')
    return
  }

  const img = new Image()
  const reader = new FileReader()
  reader.onload = (e) => {
    imageSrc.value = e.target?.result as string
    img.onload = () => {
      imageNatural.value = { width: img.naturalWidth, height: img.naturalHeight }
      compressWidth.value = img.naturalWidth
      compressHeight.value = img.naturalHeight
      imageEl.value = img
      imageFile.value = file
      showDropZone.value = false
      cropResult.value = null
      showToast('图片加载成功', 'success')
      if (activeTab.value === 'crop') {
        nextTick(() => initCropCanvas())
      }
    }
    img.src = imageSrc.value
  }
  reader.readAsDataURL(file)
}

function handleFileInput(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.[0]) loadImage(input.files[0])
  input.value = ''
}

function handleDrop(e: DragEvent) {
  isDragging.value = false
  const file = e.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) {
    loadImage(file)
  } else {
    showToast('请拖入图片文件', 'error')
  }
}

function handlePaste(e: ClipboardEvent) {
  if (!showDropZone.value) return
  const items = e.clipboardData?.items
  if (!items) return
  for (let i = 0; i < items.length; i++) {
    if (items[i].type.startsWith('image/')) {
      const file = items[i].getAsFile()
      if (file) loadImage(file)
      return
    }
  }
}

function resetImage() {
  imageFile.value = null
  imageSrc.value = ''
  imageEl.value = null
  showDropZone.value = true
  cropResult.value = null
  imageNatural.value = { width: 0, height: 0 }
}

// ============ Compress ============
function onCompressWidthChange() {
  if (compressLockRatio.value && imageNatural.value.width) {
    const ratio = imageNatural.value.height / imageNatural.value.width
    compressHeight.value = Math.round(compressWidth.value * ratio)
  }
  updateCompressEstimate()
}

function onCompressHeightChange() {
  if (compressLockRatio.value && imageNatural.value.height) {
    const ratio = imageNatural.value.width / imageNatural.value.height
    compressWidth.value = Math.round(compressHeight.value * ratio)
  }
  updateCompressEstimate()
}

function updateCompressEstimate() {
  if (!imageFile.value) return
  const ratio =
    (compressWidth.value * compressHeight.value) /
    (imageNatural.value.width * imageNatural.value.height)
  const estimate = imageFile.value.size * (compressQuality.value / 100) * ratio
  compressEstimatedSize.value = formatSize(estimate)
}

watch([compressQuality, compressFormat], updateCompressEstimate)
watch(compressWidth, () => {
  if (compressLockRatio.value) onCompressWidthChange()
  else updateCompressEstimate()
})
watch(compressHeight, () => {
  if (compressLockRatio.value) onCompressHeightChange()
  else updateCompressEstimate()
})

async function doCompress() {
  if (!imageEl.value) return
  processing.value = true
  try {
    const canvas = document.createElement('canvas')
    canvas.width = compressWidth.value
    canvas.height = compressHeight.value
    const ctx = canvas.getContext('2d')!
    ctx.drawImage(imageEl.value, 0, 0, compressWidth.value, compressHeight.value)

    const fmt =
      compressFormat.value === 'original'
        ? (imageFile.value?.type || 'image/png')
        : compressFormat.value
    const quality = fmt === 'image/png' ? undefined : compressQuality.value / 100
    const blob = await new Promise<Blob | null>((resolve) =>
      canvas.toBlob(resolve, fmt, quality),
    )
    if (blob) downloadBlob(blob, 'compressed')
    showToast('压缩完成', 'success')
  } catch {
    showToast('压缩失败', 'error')
  } finally {
    processing.value = false
  }
}

// ============ Crop ============
function initCropCanvas() {
  if (!cropCanvas.value || !imageEl.value) return
  const canvas = cropCanvas.value
  const container = cropContainer.value
  if (!container) return

  const containerW = container.clientWidth
  const containerH = container.clientHeight || 420
  const imgW = imageNatural.value.width
  const imgH = imageNatural.value.height

  const scale = Math.min(containerW / imgW, containerH / imgH, 1)
  imgDisplayW = imgW * scale
  imgDisplayH = imgH * scale
  displayScale = scale
  displayOffsetX = Math.round((containerW - imgDisplayW) / 2)
  displayOffsetY = Math.round((containerH - imgDisplayH) / 2)

  canvas.width = containerW
  canvas.height = containerH

  cropW = Math.round(imgDisplayW * 0.6)
  cropH = Math.round(imgDisplayH * 0.6)
  cropX = Math.round((imgDisplayW - cropW) / 2) + displayOffsetX
  cropY = Math.round((imgDisplayH - cropH) / 2) + displayOffsetY

  drawCrop()
}

function drawCrop() {
  if (!cropCanvas.value || !imageEl.value) return
  const ctx = cropCanvas.value.getContext('2d')!
  const { width, height } = cropCanvas.value

  ctx.clearRect(0, 0, width, height)
  ctx.drawImage(imageEl.value, displayOffsetX, displayOffsetY, imgDisplayW, imgDisplayH)

  // dark overlay
  ctx.fillStyle = 'rgba(0, 0, 0, 0.5)'
  ctx.fillRect(0, 0, width, height)

  // clear crop area
  ctx.clearRect(cropX, cropY, cropW, cropH)
  ctx.drawImage(
    imageEl.value,
    Math.round((cropX - displayOffsetX) / displayScale),
    Math.round((cropY - displayOffsetY) / displayScale),
    Math.round(cropW / displayScale),
    Math.round(cropH / displayScale),
    cropX,
    cropY,
    cropW,
    cropH,
  )

  // crop border
  ctx.strokeStyle = '#3b82f6'
  ctx.lineWidth = 2
  ctx.strokeRect(cropX, cropY, cropW, cropH)

  // corner handles
  ctx.fillStyle = '#3b82f6'
  const handles = [
    [cropX, cropY],
    [cropX + cropW, cropY],
    [cropX, cropY + cropH],
    [cropX + cropW, cropY + cropH],
  ]
  for (const [hx, hy] of handles) {
    ctx.fillRect(hx - 5, hy - 5, 10, 10)
  }
}

function getHandle(x: number, y: number): string {
  const tol = 10
  const corners: [string, number, number][] = [
    ['tl', cropX, cropY],
    ['tr', cropX + cropW, cropY],
    ['bl', cropX, cropY + cropH],
    ['br', cropX + cropW, cropY + cropH],
  ]
  for (const [name, hx, hy] of corners) {
    if (Math.abs(x - hx) < tol && Math.abs(y - hy) < tol) return name
  }
  if (
    x >= cropX - tol &&
    x <= cropX + cropW + tol &&
    y >= cropY - tol &&
    y <= cropY + cropH + tol
  ) {
    return 'move'
  }
  return ''
}

function clampCrop() {
  const minW = 40
  const minH = 40
  const maxX = displayOffsetX
  const maxY = displayOffsetY
  const maxW = imgDisplayW
  const maxH = imgDisplayH

  if (cropW < minW) cropW = minW
  if (cropH < minH) cropH = minH
  if (cropX < maxX) cropX = maxX
  if (cropY < maxY) cropY = maxY
  if (cropX + cropW > maxX + maxW) cropX = maxX + maxW - cropW
  if (cropY + cropH > maxY + maxH) cropY = maxY + maxH - cropH
}

function onCropMouseDown(e: MouseEvent) {
  const rect = cropCanvas.value!.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top

  resizeHandle = getHandle(mx, my)
  if (resizeHandle) {
    isResizingCrop = resizeHandle !== 'move'
    isDraggingCrop = resizeHandle === 'move'
    dragStart = {
      x: mx,
      y: my,
      cx: cropX,
      cy: cropY,
      cw: cropW,
      ch: cropH,
    }
    e.preventDefault()
  }
}

function onCropMouseMove(e: MouseEvent) {
  if (!isDraggingCrop && !isResizingCrop) return
  const rect = cropCanvas.value!.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top
  const dx = mx - dragStart.x
  const dy = my - dragStart.y

  if (isDraggingCrop) {
    cropX = dragStart.cx + dx
    cropY = dragStart.cy + dy
  } else if (isResizingCrop) {
    const ratio = CROP_RATIOS[cropPreset.value]
    if (ratio) {
      // for constrained resize, let width drive based on proportion
      let nw: number, nh: number
      if (resizeHandle.includes('r')) {
        nw = dragStart.cw + dx
      } else if (resizeHandle.includes('l')) {
        nw = dragStart.cw - dx
      } else {
        nw = dragStart.cw
      }
      nh = Math.round(nw / ratio)

      if (resizeHandle.includes('t')) {
        nh = dragStart.ch - dy
        nw = Math.round(nh * ratio)
      }
      if (resizeHandle.includes('b')) {
        nh = dragStart.ch + dy
        nw = Math.round(nh * ratio)
      }

      if (resizeHandle === 'tl') {
        nw = dragStart.cw - dx
        nh = Math.round(nw / ratio)
      }
      if (resizeHandle === 'br') {
        nw = dragStart.cw + dx
        nh = Math.round(nw / ratio)
      }
      if (resizeHandle === 'tr') {
        nw = dragStart.cw + dx
        nh = Math.round(nw / ratio)
      }
      if (resizeHandle === 'bl') {
        nw = dragStart.cw - dx
        nh = Math.round(nw / ratio)
      }

      if (resizeHandle.includes('l')) cropX = dragStart.cx + dragStart.cw - nw
      if (resizeHandle.includes('t')) cropY = dragStart.cy + dragStart.ch - nh

      cropW = nw
      cropH = nh
    } else {
      // free resize
      if (resizeHandle.includes('l')) {
        cropX = dragStart.cx + dx
        cropW = dragStart.cw - dx
      } else if (resizeHandle.includes('r')) {
        cropW = dragStart.cw + dx
      }
      if (resizeHandle.includes('t')) {
        cropY = dragStart.cy + dy
        cropH = dragStart.ch - dy
      } else if (resizeHandle.includes('b')) {
        cropH = dragStart.ch + dy
      }
    }
  }

  clampCrop()
  drawCrop()
}

function onCropMouseUp() {
  isDraggingCrop = false
  isResizingCrop = false
  resizeHandle = ''
}

function selectPreset(preset: CropPreset) {
  cropPreset.value = preset
  if (!imageEl.value || !cropCanvas.value) return

  const ratio = CROP_RATIOS[preset]
  if (ratio) {
    // adjust crop area to match ratio
    const maxW = imgDisplayW * 0.8
    const maxH = imgDisplayH * 0.8
    let nw: number, nh: number
    if (maxW / ratio <= maxH) {
      nw = maxW
      nh = Math.round(maxW / ratio)
    } else {
      nh = maxH
      nw = Math.round(maxH * ratio)
    }
    cropW = nw
    cropH = nh
  }
  cropX = Math.round((imgDisplayW - cropW) / 2) + displayOffsetX
  cropY = Math.round((imgDisplayH - cropH) / 2) + displayOffsetY
  clampCrop()
  drawCrop()
}

async function doCrop() {
  if (!imageEl.value) return
  processing.value = true
  try {
    const srcX = Math.round((cropX - displayOffsetX) / displayScale)
    const srcY = Math.round((cropY - displayOffsetY) / displayScale)
    const srcW = Math.round(cropW / displayScale)
    const srcH = Math.round(cropH / displayScale)

    const canvas = document.createElement('canvas')
    canvas.width = srcW
    canvas.height = srcH
    const ctx = canvas.getContext('2d')!
    ctx.drawImage(imageEl.value, srcX, srcY, srcW, srcH, 0, 0, srcW, srcH)

    const blob = await new Promise<Blob | null>((resolve) =>
      canvas.toBlob(resolve, 'image/png'),
    )
    if (blob) {
      cropResult.value = blob
      downloadBlob(blob, 'cropped')
    }
    showToast('裁剪完成', 'success')
  } catch {
    showToast('裁剪失败', 'error')
  } finally {
    processing.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'crop' && !showDropZone.value) {
    nextTick(() => initCropCanvas())
  }
})

// ============ Convert ============
function updateConvertEstimate() {
  if (!imageFile.value) return
  const est =
    convertFormat.value === 'image/png'
      ? imageFile.value.size * 1.5
      : imageFile.value.size * (convertQuality.value / 100) * 0.3
  convertEstimatedSize.value = formatSize(est)
}

watch([convertFormat, convertQuality], updateConvertEstimate)

async function doConvert() {
  if (!imageEl.value) return
  processing.value = true
  try {
    const canvas = document.createElement('canvas')
    canvas.width = imageNatural.value.width
    canvas.height = imageNatural.value.height
    const ctx = canvas.getContext('2d')!
    ctx.drawImage(imageEl.value, 0, 0)
    const quality =
      convertFormat.value === 'image/png' ? undefined : convertQuality.value / 100
    const blob = await new Promise<Blob | null>((resolve) =>
      canvas.toBlob(resolve, convertFormat.value, quality),
    )
    if (blob) downloadBlob(blob, 'converted')
    showToast('转换完成', 'success')
  } catch {
    showToast('转换失败', 'error')
  } finally {
    processing.value = false
  }
}

// ============ Download ============
function downloadBlob(blob: Blob, prefix: string) {
  const ext = blob.type.split('/')[1] || 'png'
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${prefix}-${Date.now()}.${ext}`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

// ============ Lifecycle ============
onMounted(() => {
  document.addEventListener('paste', handlePaste)
})

onUnmounted(() => {
  document.removeEventListener('paste', handlePaste)
})
</script>

<template>
  <div class="tool-root">
    <!-- Header -->
    <header class="tool-header">
      <h1 class="tool-logo">图片处理</h1>
      <a href="/" class="back-link">← 返回首页</a>
    </header>

    <!-- Upload Zone -->
    <div v-if="showDropZone" class="upload-area">
      <div
        class="drop-zone"
        :class="{ dragging: isDragging }"
        @dragover.prevent="isDragging = true"
        @dragleave="isDragging = false"
        @drop.prevent="handleDrop"
        @click="fileInput?.click()"
      >
        <div class="drop-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <circle cx="8.5" cy="8.5" r="1.5" />
            <polyline points="21 15 16 10 5 21" />
          </svg>
        </div>
        <p class="drop-text">拖拽图片到此处，或点击选择文件</p>
        <p class="drop-hint">支持 Ctrl+V 粘贴 | 最大 20MB | PNG / JPEG / WebP / BMP / GIF / TIFF</p>
        <input
          ref="fileInput"
          type="file"
          accept="image/png,image/jpeg,image/webp,image/bmp,image/gif,image/tiff"
          style="display: none"
          @change="handleFileInput"
        />
      </div>
    </div>

    <!-- Workspace -->
    <div v-else class="workspace">
      <!-- Image Preview -->
      <div class="preview-panel">
        <img :src="imageSrc" class="preview-img" alt="preview" />
        <div class="image-info">
          <span>{{ imageNatural.width }} × {{ imageNatural.height }}</span>
          <span>{{ imageSizeStr }}</span>
          <span>{{ imageFormat }}</span>
        </div>
        <button class="btn-reset" @click="resetImage">换一张</button>
      </div>

      <!-- Tool Panel -->
      <div class="tool-panel">
        <div class="tab-bar">
          <button
            v-for="[key, label] in TABS"
            :key="key"
            :class="{ active: activeTab === key }"
            @click="activeTab = key"
          >
            {{ label }}
          </button>
        </div>

        <!-- Compress Panel -->
        <div v-if="activeTab === 'compress'" class="panel-body">
          <div class="control-group">
            <label>质量 ({{ compressQuality }}%)</label>
            <input
              v-model.number="compressQuality"
              type="range"
              min="1"
              max="100"
              class="slider"
            />
          </div>

          <div class="control-row">
            <div class="control-group flex-1">
              <label>宽度 (px)</label>
              <input
                v-model.number="compressWidth"
                type="number"
                min="1"
                class="input"
                @input="onCompressWidthChange"
              />
            </div>
            <button class="lock-btn" :class="{ locked: compressLockRatio }" @click="compressLockRatio = !compressLockRatio">
              <svg v-if="compressLockRatio" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M12 14v4"/></svg>
            </button>
            <div class="control-group flex-1">
              <label>高度 (px)</label>
              <input
                v-model.number="compressHeight"
                type="number"
                min="1"
                class="input"
                @input="onCompressHeightChange"
              />
            </div>
          </div>

          <div class="control-group">
            <label>输出格式</label>
            <select v-model="compressFormat" class="select">
              <option value="original">保持原格式</option>
              <option value="image/png">PNG</option>
              <option value="image/jpeg">JPEG</option>
              <option value="image/webp">WebP</option>
            </select>
          </div>

          <div v-if="compressEstimatedSize" class="estimate">
            预估输出大小：<strong>{{ compressEstimatedSize }}</strong>
          </div>

          <button class="btn-primary" :disabled="processing" @click="doCompress">
            {{ processing ? '处理中...' : '压缩并下载' }}
          </button>
        </div>

        <!-- Crop Panel -->
        <div v-if="activeTab === 'crop'" class="panel-body">
          <div class="preset-bar">
            <button
              v-for="p in CROP_PRESETS"
              :key="p.key"
              :class="{ active: cropPreset === p.key }"
              class="preset-btn"
              @click="selectPreset(p.key)"
            >
              {{ p.label }}
            </button>
          </div>

          <div ref="cropContainer" class="crop-container">
            <canvas
              ref="cropCanvas"
              class="crop-canvas"
              @mousedown="onCropMouseDown"
              @mousemove="onCropMouseMove"
              @mouseup="onCropMouseUp"
              @mouseleave="onCropMouseUp"
            />
          </div>

          <button class="btn-primary" :disabled="processing" @click="doCrop">
            {{ processing ? '处理中...' : '裁剪并下载' }}
          </button>
        </div>

        <!-- Convert Panel -->
        <div v-if="activeTab === 'convert'" class="panel-body">
          <div class="control-group">
            <label>目标格式</label>
            <select v-model="convertFormat" class="select">
              <option value="image/png">PNG</option>
              <option value="image/jpeg">JPEG</option>
              <option value="image/webp">WebP</option>
            </select>
          </div>

          <div v-if="convertFormat !== 'image/png'" class="control-group">
            <label>质量 ({{ convertQuality }}%)</label>
            <input
              v-model.number="convertQuality"
              type="range"
              min="1"
              max="100"
              class="slider"
            />
          </div>

          <div v-if="convertEstimatedSize" class="estimate">
            预估输出大小：<strong>{{ convertEstimatedSize }}</strong>
          </div>

          <button class="btn-primary" :disabled="processing" @click="doConvert">
            {{ processing ? '处理中...' : '转换并下载' }}
          </button>
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
.tool-root {
  --t-bg: #f8fafc;
  --t-surface: #ffffff;
  --t-border: #e2e8f0;
  --t-text: #1e293b;
  --t-text-secondary: #64748b;
  --t-accent: #3b82f6;
  --t-accent-hover: #2563eb;
  --t-radius: 12px;
  --t-radius-sm: 8px;
  --t-shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.04);
  --t-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --t-shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -4px rgba(0, 0, 0, 0.04);
  --t-success: #10b981;
  --t-error: #ef4444;
  --t-info: #3b82f6;

  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: var(--t-bg);
  color: var(--t-text);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ===================== Header ===================== */
.tool-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  background: var(--t-surface);
  border-bottom: 1px solid var(--t-border);
  flex-shrink: 0;
}

.tool-logo {
  font-size: 1.15em;
  font-weight: 600;
  color: var(--t-text);
  margin: 0;
  letter-spacing: -0.01em;
}

.back-link {
  font-size: 0.9em;
  color: var(--t-text-secondary);
  text-decoration: none;
  transition: color 0.15s;
}

.back-link:hover {
  color: var(--t-accent);
}

/* ===================== Upload Zone ===================== */
.upload-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.drop-zone {
  width: 100%;
  max-width: 520px;
  padding: 64px 32px;
  border: 2px dashed #cbd5e1;
  border-radius: var(--t-radius);
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--t-surface);
}

.drop-zone:hover,
.drop-zone.dragging {
  border-color: var(--t-accent);
  background: #eff6ff;
}

.drop-icon {
  margin-bottom: 16px;
}

.drop-text {
  font-size: 1.1em;
  color: var(--t-text);
  margin: 0 0 8px 0;
  font-weight: 500;
}

.drop-hint {
  font-size: 0.82em;
  color: var(--t-text-secondary);
  margin: 0;
}

/* ===================== Workspace ===================== */
.workspace {
  flex: 1;
  display: flex;
  gap: 0;
  min-height: 0;
}

.preview-panel {
  width: 55%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #f1f5f9;
  gap: 16px;
}

.preview-img {
  max-width: 100%;
  max-height: 60vh;
  object-fit: contain;
  border-radius: var(--t-radius-sm);
  box-shadow: var(--t-shadow-lg);
}

.image-info {
  display: flex;
  gap: 20px;
  font-size: 0.85em;
  color: var(--t-text-secondary);
}

.btn-reset {
  padding: 6px 16px;
  border: 1px solid var(--t-border);
  border-radius: 6px;
  background: var(--t-surface);
  color: var(--t-text-secondary);
  font-size: 0.85em;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-reset:hover {
  border-color: var(--t-accent);
  color: var(--t-accent);
}

/* ===================== Tool Panel ===================== */
.tool-panel {
  width: 45%;
  display: flex;
  flex-direction: column;
  background: var(--t-surface);
  border-left: 1px solid var(--t-border);
}

.tab-bar {
  display: flex;
  border-bottom: 1px solid var(--t-border);
  flex-shrink: 0;
}

.tab-bar button {
  flex: 1;
  padding: 14px 0;
  border: none;
  background: none;
  font-size: 0.9em;
  font-weight: 500;
  color: var(--t-text-secondary);
  cursor: pointer;
  transition: all 0.15s;
  border-bottom: 2px solid transparent;
  font-family: inherit;
}

.tab-bar button:hover {
  color: var(--t-text);
}

.tab-bar button.active {
  color: var(--t-accent);
  border-bottom-color: var(--t-accent);
}

/* ===================== Panel Body ===================== */
.panel-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
  overflow-y: auto;
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.control-group label {
  font-size: 0.82em;
  font-weight: 600;
  color: var(--t-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.control-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.flex-1 {
  flex: 1;
}

.input {
  padding: 8px 12px;
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius-sm);
  font-size: 0.92em;
  font-family: inherit;
  color: var(--t-text);
  background: var(--t-surface);
  outline: none;
  transition: border-color 0.15s;
}

.input:focus {
  border-color: var(--t-accent);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.select {
  padding: 8px 12px;
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius-sm);
  font-size: 0.92em;
  font-family: inherit;
  color: var(--t-text);
  background: var(--t-surface);
  outline: none;
  cursor: pointer;
  transition: border-color 0.15s;
}

.select:focus {
  border-color: var(--t-accent);
}

.lock-btn {
  padding: 8px 10px;
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius-sm);
  background: var(--t-surface);
  color: var(--t-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  margin-bottom: 0;
}

.lock-btn.locked {
  color: var(--t-accent);
  border-color: var(--t-accent);
  background: #eff6ff;
}

.slider {
  -webkit-appearance: none;
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: #e2e8f0;
  outline: none;
  cursor: pointer;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--t-accent);
  cursor: pointer;
  border: 2px solid #fff;
  box-shadow: var(--t-shadow-sm);
}

.estimate {
  font-size: 0.88em;
  color: var(--t-text-secondary);
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: var(--t-radius-sm);
  border: 1px solid var(--t-border);
}

.estimate strong {
  color: var(--t-accent);
}

.btn-primary {
  padding: 12px 24px;
  border: none;
  border-radius: var(--t-radius-sm);
  background: var(--t-accent);
  color: #fff;
  font-size: 0.95em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
  margin-top: auto;
}

.btn-primary:hover:not(:disabled) {
  background: var(--t-accent-hover);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===================== Crop ===================== */
.preset-bar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.preset-btn {
  padding: 6px 14px;
  border: 1px solid var(--t-border);
  border-radius: 20px;
  background: var(--t-surface);
  color: var(--t-text-secondary);
  font-size: 0.82em;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}

.preset-btn:hover {
  border-color: var(--t-accent);
  color: var(--t-accent);
}

.preset-btn.active {
  background: var(--t-accent);
  color: #fff;
  border-color: var(--t-accent);
}

.crop-container {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #0f172a;
  border-radius: var(--t-radius-sm);
  overflow: hidden;
}

.crop-canvas {
  width: 100%;
  height: 100%;
  cursor: crosshair;
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
  box-shadow: var(--t-shadow-lg);
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
</style>
