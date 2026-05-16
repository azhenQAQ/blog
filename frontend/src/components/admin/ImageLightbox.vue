<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps<{
  images: string[]
  modelValue: boolean
  initialIndex: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const currentIndex = ref(0)
const imageLoaded = ref(false)
const imageError = ref(false)
const zoom = ref(1)
const MIN_ZOOM = 0.25
const MAX_ZOOM = 5
const ZOOM_STEP = 0.15

const isFirst = computed(() => currentIndex.value === 0)
const isLast = computed(() => currentIndex.value === props.images.length - 1)
const zoomPercent = computed(() => Math.round(zoom.value * 100))

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      currentIndex.value = props.initialIndex
      imageLoaded.value = false
      imageError.value = false
      zoom.value = 1
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
  }
)

function resetZoom() {
  zoom.value = 1
}

function close() {
  emit('update:modelValue', false)
}

function prev() {
  if (isFirst.value) return
  imageLoaded.value = false
  imageError.value = false
  zoom.value = 1
  currentIndex.value--
}

function next() {
  if (isLast.value) return
  imageLoaded.value = false
  imageError.value = false
  zoom.value = 1
  currentIndex.value++
}

function onWheel(e: WheelEvent) {
  if (!imageLoaded.value || imageError.value) return
  e.preventDefault()
  const delta = e.deltaY > 0 ? -ZOOM_STEP : ZOOM_STEP
  zoom.value = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, zoom.value + delta))
}

function onOverlayClick(e: MouseEvent) {
  if ((e.target as HTMLElement).classList.contains('lightbox-overlay')) {
    close()
  }
}

function onKeydown(e: KeyboardEvent) {
  if (!props.modelValue) return
  switch (e.key) {
    case 'Escape':
      close()
      break
    case 'ArrowLeft':
      prev()
      break
    case 'ArrowRight':
      next()
      break
  }
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="lightbox-fade">
      <div
        v-if="modelValue"
        class="lightbox-overlay"
        @click="onOverlayClick"
      >
        <!-- 关闭按钮 -->
        <button class="lightbox-close" @click="close" title="关闭 (ESC)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none">
            <path
              d="M18 6L6 18M6 6l12 12"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
        </button>

        <!-- 上一张 -->
        <button
          v-if="images.length > 1"
          class="lightbox-arrow lightbox-arrow--left"
          :class="{ 'lightbox-arrow--disabled': isFirst }"
          :disabled="isFirst"
          @click.stop="prev"
          title="上一张 (←)"
        >
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none">
            <path
              d="M15 18l-6-6 6-6"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>

        <!-- 下一张 -->
        <button
          v-if="images.length > 1"
          class="lightbox-arrow lightbox-arrow--right"
          :class="{ 'lightbox-arrow--disabled': isLast }"
          :disabled="isLast"
          @click.stop="next"
          title="下一张 (→)"
        >
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none">
            <path
              d="M9 6l6 6-6 6"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>

        <!-- 图片容器 -->
        <div class="lightbox-body" @wheel.prevent="onWheel">
          <!-- 加载中 -->
          <div v-if="!imageLoaded && !imageError" class="lightbox-loader">
            <svg viewBox="0 0 24 24" width="40" height="40" fill="none" class="spinner">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" opacity="0.2" />
              <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </div>

          <!-- 加载失败 -->
          <div v-if="imageError" class="lightbox-error">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5" />
              <line x1="12" y1="8" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              <circle cx="12" cy="16.5" r="0.8" fill="currentColor" />
            </svg>
            <span>图片加载失败</span>
          </div>

          <img
            v-show="imageLoaded && !imageError"
            :src="images[currentIndex]"
            :alt="`图片 ${currentIndex + 1}`"
            class="lightbox-img"
            :style="{ transform: `scale(${zoom})` }"
            @load="imageLoaded = true"
            @error="imageError = true"
            @dblclick.prevent="resetZoom"
          />
        </div>

        <!-- 底部信息栏 -->
        <div class="lightbox-bottom">
          <span v-if="images.length > 1" class="lightbox-counter">
            {{ currentIndex + 1 }} / {{ images.length }}
          </span>
          <span v-if="imageLoaded && !imageError && zoom !== 1" class="lightbox-zoom">
            {{ zoomPercent }}%
          </span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.lightbox-close {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.25);
}

.lightbox-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.lightbox-arrow:hover {
  background: rgba(255, 255, 255, 0.25);
}

.lightbox-arrow--left {
  left: 20px;
}

.lightbox-arrow--right {
  right: 20px;
}

.lightbox-arrow--disabled,
.lightbox-arrow:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.lightbox-arrow--disabled:hover,
.lightbox-arrow:disabled:hover {
  background: rgba(255, 255, 255, 0.1);
}

.lightbox-body {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 90vw;
  max-height: 90vh;
  cursor: default;
}

.lightbox-img {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.5);
  user-select: none;
  -webkit-user-drag: none;
}

.lightbox-loader {
  color: rgba(255, 255, 255, 0.6);
}

.spinner {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.lightbox-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.95em;
}

.lightbox-counter {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9em;
  font-variant-numeric: tabular-nums;
}

.lightbox-zoom {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85em;
  font-variant-numeric: tabular-nums;
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.lightbox-bottom {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 过渡动画 */
.lightbox-fade-enter-active,
.lightbox-fade-leave-active {
  transition: opacity 0.25s ease;
}

.lightbox-fade-enter-from,
.lightbox-fade-leave-to {
  opacity: 0;
}
</style>
