<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useMusicStore } from '@/stores/music'
import SvgIcon from '@/components/common/SvgIcon.vue'

const store = useMusicStore()
const showVolumeSlider = ref(false)
const progressRef = ref<HTMLElement | null>(null)
const volumeTrackRef = ref<HTMLElement | null>(null)

// Hover 展开/收起控制
let expandTimer: ReturnType<typeof setTimeout> | null = null
let collapseTimer: ReturnType<typeof setTimeout> | null = null
const EXPAND_DELAY = 300
const COLLAPSE_DELAY = 500

function clearTimers() {
  if (expandTimer) {
    clearTimeout(expandTimer)
    expandTimer = null
  }
  if (collapseTimer) {
    clearTimeout(collapseTimer)
    collapseTimer = null
  }
}

function handleMouseEnter() {
  if (collapseTimer) {
    clearTimeout(collapseTimer)
    collapseTimer = null
    return
  }
  if (store.isExpanded) return
  if (expandTimer) return
  expandTimer = setTimeout(() => {
    store.isExpanded = true
    expandTimer = null
  }, EXPAND_DELAY)
}

function handleMouseLeave() {
  if (expandTimer) {
    clearTimeout(expandTimer)
    expandTimer = null
    return
  }
  if (!store.isExpanded) return
  if (collapseTimer) return
  collapseTimer = setTimeout(() => {
    store.isExpanded = false
    store.showPlaylist = false
    collapseTimer = null
  }, COLLAPSE_DELAY)
}

// 格式化时间
function formatTime(seconds: number): string {
  if (!isFinite(seconds)) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

// 进度条百分比
const progressPercent = computed(() =>
  store.duration > 0 ? (store.currentTime / store.duration) * 100 : 0,
)

// 进度条拖拽
function calcProgressRatio(clientX: number): number {
  if (!progressRef.value) return 0
  const rect = progressRef.value.getBoundingClientRect()
  return Math.max(0, Math.min(1, (clientX - rect.left) / rect.width))
}

function onProgressMouseDown(event: MouseEvent) {
  if (!store.currentSong) return
  store.seek(calcProgressRatio(event.clientX) * store.duration)
  document.addEventListener('mousemove', onProgressDrag)
  document.addEventListener('mouseup', onProgressMouseUp)
}

function onProgressDrag(event: MouseEvent) {
  store.seek(calcProgressRatio(event.clientX) * store.duration)
}

function onProgressMouseUp() {
  document.removeEventListener('mousemove', onProgressDrag)
  document.removeEventListener('mouseup', onProgressMouseUp)
}

// 音量条拖拽
function calcVolumeRatio(clientY: number): number {
  if (!volumeTrackRef.value) return 0
  const rect = volumeTrackRef.value.getBoundingClientRect()
  return (clientY - rect.top) / rect.height
}

function onVolumeMouseDown(event: MouseEvent) {
  store.setVolume(Math.max(0, Math.min(1, calcVolumeRatio(event.clientY))))
  document.addEventListener('mousemove', onVolumeDrag)
  document.addEventListener('mouseup', onVolumeMouseUp)
}

function onVolumeDrag(event: MouseEvent) {
  store.setVolume(Math.max(0, Math.min(1, calcVolumeRatio(event.clientY))))
}

function onVolumeMouseUp() {
  document.removeEventListener('mousemove', onVolumeDrag)
  document.removeEventListener('mouseup', onVolumeMouseUp)
}

// 当前歌词文本
const currentLyricText = computed(() => {
  const lines = store.lrcLines
  if (lines.length === 0) return ''
  return lines[store.currentLrcIndex]?.text ?? ''
})

// 播放模式提示
const modeTitle = computed(() => {
  switch (store.playMode) {
    case 'single': return '单曲循环'
    case 'shuffle': return '随机播放'
    default: return '列表循环'
  }
})

// 音量 hover 显示/隐藏
let volumeTimer: ReturnType<typeof setTimeout> | null = null

function handleVolumeEnter() {
  if (volumeTimer) {
    clearTimeout(volumeTimer)
    volumeTimer = null
  }
  showVolumeSlider.value = true
}

function handleVolumeLeave() {
  volumeTimer = setTimeout(() => {
    showVolumeSlider.value = false
    volumeTimer = null
  }, 300)
}

onUnmounted(() => {
  clearTimers()
  if (volumeTimer) clearTimeout(volumeTimer)
  document.removeEventListener('mousemove', onProgressDrag)
  document.removeEventListener('mouseup', onProgressMouseUp)
  document.removeEventListener('mousemove', onVolumeDrag)
  document.removeEventListener('mouseup', onVolumeMouseUp)
})
</script>

<template>
  <div
    class="music-player"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <!-- 折叠态 -->
    <div class="music-toggle-btn" :class="{ 'has-song': store.isPlaying }">
      <SvgIcon name="music-note" :size="18" class="toggle-icon" />
      <span v-if="store.isPlaying" class="toggle-song-info">
        <span class="toggle-title">{{ store.currentSong.title }}</span>
        <span class="toggle-sep">-</span>
        <span class="toggle-artist">{{ store.currentSong.artist }}</span>
      </span>
    </div>

    <!-- 展开面板 -->
    <div v-if="store.isExpanded" class="player-panel">
      <!-- Row 1: 歌曲信息 + 控件 -->
      <div class="player-header">
        <div class="song-info">
          <div class="cover-wrap">
            <img
              v-if="store.currentSong?.coverUrl"
              :src="store.currentSong.coverUrl"
              :alt="store.currentSong.title"
              class="cover"
            />
            <div v-else class="cover-placeholder"><SvgIcon name="music-note" :size="18" /></div>
          </div>
          <div class="song-text">
            <div class="song-title">
              {{ store.currentSong?.title || '暂无歌曲' }}
            </div>
            <div class="song-artist">
              {{ store.currentSong?.artist || '-' }}
            </div>
          </div>
        </div>

        <div class="header-controls">
          <button class="ctrl-btn" @click.stop="store.prev()" :disabled="store.playlist.length === 0">
            <SvgIcon name="prev" :size="14" />
          </button>
          <button
            class="ctrl-btn ctrl-play"
            @click.stop="store.togglePlay()"
            :disabled="store.playlist.length === 0"
          >
            <template v-if="store.isLoading">…</template>
            <SvgIcon v-else :name="store.isPlaying ? 'pause' : 'play'" :size="16" />
          </button>
          <button class="ctrl-btn" @click.stop="store.next()" :disabled="store.playlist.length === 0">
            <SvgIcon name="next" :size="14" />
          </button>
          <button
            class="ctrl-btn ctrl-mode"
            @click.stop="store.togglePlayMode()"
            :title="modeTitle"
          >
            <SvgIcon :name="'mode-' + store.playMode" :size="14" />
          </button>

          <div
            class="volume-control"
            @mouseenter="handleVolumeEnter"
            @mouseleave="handleVolumeLeave"
          >
            <button class="ctrl-btn volume-btn" @click.stop="store.toggleMute()">
              <SvgIcon :name="store.volume === 0 ? 'volume-mute' : 'volume-on'" :size="14" />
            </button>
            <div v-if="showVolumeSlider" class="volume-slider-wrap">
              <div
                ref="volumeTrackRef"
                class="volume-track"
                @mousedown.stop.prevent="onVolumeMouseDown"
              >
                <div class="volume-filled" :style="{ height: (store.volume * 100) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Row 2: 进度条 -->
      <div class="progress-wrap">
        <span class="time-label">{{ formatTime(store.currentTime) }}</span>
        <div
          ref="progressRef"
          class="progress-bar"
          @mousedown.prevent="onProgressMouseDown"
          :class="{ disabled: !store.currentSong }"
        >
          <div class="progress-filled" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <span class="time-label">{{ formatTime(store.duration) }}</span>
      </div>

      <div v-if="store.hasError" class="error-tip">加载失败</div>

      <!-- Row 3: 当前歌词 + 歌单图标 -->
      <div class="lyrics-row">
        <span class="current-lyric">{{ currentLyricText }}</span>
        <button
          class="playlist-icon-btn"
          @click.stop="store.togglePlaylist()"
          :title="store.showPlaylist ? '收起歌单' : '展开歌单'"
        >
          <SvgIcon name="playlist" :size="14" />
        </button>
      </div>

      <!-- Row 4: 歌单列表 -->
      <transition name="playlist">
        <div v-if="store.showPlaylist" class="playlist-wrap">
          <div
            v-for="(song, idx) in store.playlist"
            :key="song.id"
            class="playlist-item"
            :class="{ active: idx === store.currentIndex }"
            @click="store.playSong(idx)"
          >
            <span class="play-marker">
              <SvgIcon v-if="idx === store.currentIndex" name="play" :size="10" />
            </span>
            <span class="item-title">{{ song.title }}</span>
            <span class="item-artist">{{ song.artist }}</span>
          </div>
          <div v-if="store.playlist.length === 0" class="empty-tip">暂无歌曲</div>
        </div>
      </transition>
    </div>
  </div>
</template>

<style scoped>
.music-player {
  position: relative;
  display: flex;
  align-items: center;
}

/* ========== 折叠态按钮 ========== */
.music-toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  background: var(--card-bg);
  border: 3px solid var(--shadow-color);
  box-shadow: 3px 3px 0 var(--shadow-color);
  font-family: var(--font-heading);
  font-weight: 700;
  cursor: default;
  transition: all 0.15s ease;
  padding: 0 10px;
}

.music-toggle-btn.has-song {
  padding: 0 14px 0 10px;
}

[data-theme='dark'] .music-toggle-btn {
  background: #2a2a2a;
  border-color: #888;
  box-shadow: 3px 3px 0 #444;
}

.toggle-icon {
  font-size: 1.4em;
  color: var(--text-main);
  line-height: 1;
}

[data-theme='dark'] .toggle-icon {
  color: var(--accent-yellow);
}

.toggle-song-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85em;
  line-height: 1;
  white-space: nowrap;
  overflow: hidden;
}

.toggle-title {
  color: var(--text-strong);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

[data-theme='dark'] .toggle-title {
  color: var(--text-main);
}

.toggle-sep {
  color: var(--text-muted);
}

.toggle-artist {
  color: var(--text-muted);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 下拉面板 ========== */
.player-panel {
  position: absolute;
  top: calc(var(--nav-height) - 8px);
  right: 0;
  width: 360px;
  background: var(--card-bg);
  border: 3px solid var(--shadow-color);
  box-shadow: 6px 6px 0 var(--shadow-color);
  padding: 14px;
  z-index: 200;
}

[data-theme='dark'] .player-panel {
  background: #222;
  border-color: #666;
  box-shadow: 6px 6px 0 rgba(0, 0, 0, 0.6);
}

/* ========== Row 1: Header ========== */
.player-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.song-info {
  display: flex;
  gap: 10px;
  flex: 1;
  min-width: 0;
  align-items: center;
}

.cover-wrap {
  flex-shrink: 0;
}

.cover,
.cover-placeholder {
  width: 44px;
  height: 44px;
  border: 3px solid var(--shadow-color);
  background: var(--page-bg);
  display: block;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-heading);
  font-size: 1.3em;
  color: var(--text-muted);
}

[data-theme='dark'] .cover-placeholder {
  background: #2a2a2a;
  border-color: #666;
}

.song-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}

.song-title {
  font-family: var(--font-heading);
  font-size: 1em;
  font-weight: 700;
  color: var(--text-strong);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-family: var(--font-main);
  font-size: 0.82em;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== Header 控件组 ========== */
.header-controls {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.ctrl-btn {
  width: 32px;
  height: 32px;
  background: var(--page-bg);
  border: 2px solid var(--shadow-color);
  font-family: var(--font-heading);
  font-size: 0.9em;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  line-height: 1;
  color: var(--text-main);
}

.ctrl-btn:hover:not(:disabled) {
  background: var(--accent-yellow);
  transform: translate(-1px, -1px);
  box-shadow: 2px 2px 0 var(--shadow-color);
}

[data-theme='dark'] .ctrl-btn {
  background: #2a2a2a;
  border-color: #666;
  color: var(--text-main);
}

[data-theme='dark'] .ctrl-btn:hover:not(:disabled) {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}

.ctrl-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.ctrl-play {
  width: 36px;
  height: 36px;
  font-size: 1em;
}

.ctrl-mode {
  font-size: 0.85em;
}

.volume-btn {
  font-size: 0.85em;
}

/* ========== 音量弹出 ========== */
.volume-control {
  position: relative;
}

.volume-slider-wrap {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  padding: 12px 0;
  height: 110px;
  width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--card-bg);
  border: 3px solid var(--shadow-color);
  box-shadow: 4px 4px 0 var(--shadow-color);
  z-index: 10;
}

[data-theme='dark'] .volume-slider-wrap {
  background: #222;
  border-color: #666;
  box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.6);
}

.volume-track {
  position: relative;
  width: 8px;
  height: 86px;
  background: #d0d0d0;
  border: 2px solid #888;
  cursor: pointer;
}

[data-theme='dark'] .volume-track {
  background: #555;
  border-color: #888;
}

.volume-filled {
  width: 100%;
  background: var(--accent-yellow);
  transition: height 0.1s ease;
}

[data-theme='dark'] .volume-filled {
  background: var(--accent);
}

/* ========== Row 2: 进度条 ========== */
.progress-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.time-label {
  font-family: var(--font-mono);
  font-size: 0.8em;
  color: var(--text-muted);
  min-width: 38px;
  text-align: center;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #d0d0d0;
  border: 2px solid #888;
  cursor: pointer;
  position: relative;
}

[data-theme='dark'] .progress-bar {
  background: #555;
  border-color: #888;
}

.progress-bar.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.progress-filled {
  height: 100%;
  background: var(--accent-yellow);
  transition: width 0.1s linear;
}

[data-theme='dark'] .progress-filled {
  background: var(--accent);
}

/* ========== 错误提示 ========== */
.error-tip {
  text-align: center;
  color: var(--accent);
  font-family: var(--font-heading);
  font-size: 0.85em;
  margin-bottom: 6px;
}

/* ========== Row 3: 歌词行 ========== */
.lyrics-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-top: 2px solid var(--shadow-color);
}

[data-theme='dark'] .lyrics-row {
  border-color: #444;
}

.current-lyric {
  flex: 1;
  text-align: center;
  font-family: var(--font-main);
  font-size: 0.9em;
  color: var(--accent);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 1.4em;
  line-height: 1.4;
}

.playlist-icon-btn {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: none;
  border: 2px solid var(--shadow-color);
  font-size: 0.9em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: all 0.15s ease;
}

.playlist-icon-btn:hover {
  background: var(--accent-yellow);
  color: var(--text-strong);
}

[data-theme='dark'] .playlist-icon-btn {
  border-color: #666;
  color: var(--text-muted);
  background: #2a2a2a;
}

[data-theme='dark'] .playlist-icon-btn:hover {
  background: var(--accent);
  color: #fff;
}

/* ========== Row 4: 歌单列表 ========== */
.playlist-wrap {
  max-height: 200px;
  overflow-y: auto;
  border-top: 2px solid var(--shadow-color);
  margin-top: 6px;
  padding-top: 8px;
}

[data-theme='dark'] .playlist-wrap {
  border-color: #444;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 8px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.playlist-item:hover {
  background: var(--page-bg);
}

[data-theme='dark'] .playlist-item:hover {
  background: #2a2a2a;
}

.playlist-item.active {
  background: var(--accent-yellow);
}

[data-theme='dark'] .playlist-item.active {
  background: rgba(255, 87, 34, 0.35);
}

.play-marker {
  width: 16px;
  text-align: center;
  font-family: var(--font-heading);
  font-size: 0.85em;
  color: var(--accent);
  flex-shrink: 0;
}

.item-title {
  flex: 1;
  font-family: var(--font-main);
  font-size: 0.9em;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-artist {
  max-width: 30%;
  font-family: var(--font-main);
  font-size: 0.82em;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-tip {
  text-align: center;
  padding: 16px;
  color: var(--text-muted);
  font-family: var(--font-main);
  font-size: 0.9em;
}

/* ========== 歌单过渡动画 ========== */
.playlist-enter-active,
.playlist-leave-active {
  transition: all 0.2s ease;
}

.playlist-enter-from,
.playlist-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  margin-top: 0;
}

/* ========== 响应式：≤900px ========== */
@media (max-width: 900px) {
  .player-panel {
    position: fixed;
    top: var(--nav-height);
    right: 0;
    left: 0;
    width: auto;
    box-shadow: none;
    border-left: none;
    border-right: none;
  }
}
</style>
