import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { MusicVO, LRCLine } from '@/types/music'
import { listMusicPublic } from '@/api/modules/music'
import { parseLRC, getCurrentLineIndex } from '@/utils/lrcParser'

export const useMusicStore = defineStore('music', () => {
  // === State ===
  const playlist = ref<MusicVO[]>([])
  const currentIndex = ref(-1)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(parseFloat(localStorage.getItem('music-volume') ?? '0.7'))
  const isPlaying = ref(false)
  const isExpanded = ref(false)
  const showPlaylist = ref(false)
  const lrcLines = ref<LRCLine[]>([])
  const currentLrcIndex = ref(0)
  const playMode = ref<'list' | 'single' | 'shuffle'>('list')
  const autoPlayEnabled = ref(localStorage.getItem('music-autoplay') !== 'false')
  const isLoading = ref(false)
  const hasError = ref(false)

  // === Getters ===
  const currentSong = computed(() =>
    currentIndex.value >= 0 ? playlist.value[currentIndex.value] : null
  )

  // === Audio 实例（模块级，非响应式）===
  let audio: HTMLAudioElement | null = null
  let resumeOnGesture: (() => void) | null = null

  function setupAutoplayResume() {
    if (resumeOnGesture) return
    const events = ['click', 'keydown', 'touchstart']
    function onGesture() {
      if (audio && currentIndex.value >= 0 && !isPlaying.value) {
        audio.play().then(() => { isPlaying.value = true }).catch(() => {})
      }
      events.forEach(e => document.removeEventListener(e, onGesture, { once: true }))
      resumeOnGesture = null
    }
    events.forEach(e => document.addEventListener(e, onGesture, { once: true }))
    resumeOnGesture = onGesture
  }

  // === 方法 ===
  function initAudio() {
    if (audio) return
    audio = new Audio()
    audio.preload = 'metadata'

    audio.addEventListener('timeupdate', () => {
      currentTime.value = audio!.currentTime
      if (lrcLines.value.length > 0) {
        currentLrcIndex.value = getCurrentLineIndex(lrcLines.value, audio!.currentTime)
      }
    })

    audio.addEventListener('loadedmetadata', () => {
      duration.value = audio!.duration || 0
    })

    audio.addEventListener('ended', () => {
      next()
    })

    audio.addEventListener('error', () => {
      hasError.value = true
      isPlaying.value = false
      isLoading.value = false
    })

    audio.addEventListener('loadstart', () => {
      isLoading.value = true
    })

    audio.addEventListener('canplay', () => {
      isLoading.value = false
    })

    audio.volume = volume.value
  }

  async function fetchPlaylist() {
    try {
      const list = await listMusicPublic()
      playlist.value = list
      if (list.length > 0 && currentIndex.value < 0) {
        playSong(0, autoPlayEnabled.value)
      }
    } catch (e) {
      console.error('获取歌单失败', e)
    }
  }

  async function playSong(index: number, autoPlay = true) {
    if (index < 0 || index >= playlist.value.length) return
    initAudio()

    currentIndex.value = index
    hasError.value = false
    isLoading.value = true
    const song = playlist.value[index]

    audio!.src = song.audioUrl
    audio!.load()

    if (song.lrcUrl) {
      try {
        const res = await fetch(song.lrcUrl)
        const text = await res.text()
        lrcLines.value = parseLRC(text)
      } catch {
        lrcLines.value = []
      }
    } else {
      lrcLines.value = []
    }
    currentLrcIndex.value = 0

    if (!autoPlay) {
      isLoading.value = false
      return
    }

    try {
      await audio!.play()
      isPlaying.value = true
    } catch (e) {
      isPlaying.value = false
      if (autoPlay && (e as DOMException)?.name === 'NotAllowedError') {
        setupAutoplayResume()
      }
    } finally {
      isLoading.value = false
    }
  }

  function togglePlay() {
    if (!audio || currentIndex.value < 0) {
      if (playlist.value.length > 0) {
        playSong(0)
      }
      return
    }

    if (isPlaying.value) {
      audio.pause()
      isPlaying.value = false
    } else {
      audio.play().then(() => {
        isPlaying.value = true
      })
    }
  }

  function next() {
    if (playlist.value.length === 0) return
    if (playMode.value === 'single') {
      if (audio) { audio.currentTime = 0; playSong(currentIndex.value) }
      return
    }
    let nextIndex: number
    if (playMode.value === 'shuffle') {
      nextIndex = Math.floor(Math.random() * playlist.value.length)
      if (playlist.value.length > 1 && nextIndex === currentIndex.value) {
        nextIndex = (nextIndex + 1) % playlist.value.length
      }
    } else {
      nextIndex = (currentIndex.value + 1) % playlist.value.length
    }
    playSong(nextIndex)
  }

  function togglePlayMode() {
    const modes: Array<'list' | 'single' | 'shuffle'> = ['list', 'single', 'shuffle']
    const idx = modes.indexOf(playMode.value)
    playMode.value = modes[(idx + 1) % modes.length]
  }

  function toggleAutoPlay() {
    autoPlayEnabled.value = !autoPlayEnabled.value
    localStorage.setItem('music-autoplay', String(autoPlayEnabled.value))
  }

  function prev() {
    if (playlist.value.length === 0) return
    if (currentTime.value > 3) {
      audio!.currentTime = 0
      return
    }
    const prevIndex = (currentIndex.value - 1 + playlist.value.length) % playlist.value.length
    playSong(prevIndex)
  }

  function seek(time: number) {
    if (audio) {
      audio.currentTime = time
      currentTime.value = time
    }
  }

  function setVolume(v: number) {
    volume.value = v
    if (audio) audio.volume = v
    localStorage.setItem('music-volume', String(v))
  }

  function toggleMute() {
    if (!audio) return
    if (audio.volume === 0) {
      setVolume(parseFloat(localStorage.getItem('music-volume') ?? '0.7'))
    } else {
      audio.volume = 0
      volume.value = 0
    }
  }

  function toggleExpand() {
    isExpanded.value = !isExpanded.value
    if (!isExpanded.value) {
      showPlaylist.value = false
    }
  }

  function togglePlaylist() {
    showPlaylist.value = !showPlaylist.value
  }

  return {
    playlist,
    currentIndex,
    currentTime,
    duration,
    volume,
    isPlaying,
    isExpanded,
    showPlaylist,
    lrcLines,
    currentLrcIndex,
    playMode,
    autoPlayEnabled,
    isLoading,
    hasError,
    currentSong,
    fetchPlaylist,
    playSong,
    togglePlay,
    next,
    prev,
    seek,
    setVolume,
    toggleMute,
    toggleExpand,
    togglePlaylist,
    togglePlayMode,
    toggleAutoPlay,
    initAudio,
  }
})
