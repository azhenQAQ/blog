<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const canvasRef = ref<HTMLCanvasElement>()
let animId = 0

interface Block {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  rotation: number
  vr: number
  type: 'square' | 'circle' | 'cross'
  alpha: number
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const isDark = () => document.documentElement.getAttribute('data-theme') !== 'light'
  const count = Math.min(30, Math.floor((canvas.width * canvas.height) / 30000))

  const types: Block['type'][] = ['square', 'circle', 'cross']
  const blocks: Block[] = Array.from({ length: count }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    vx: (Math.random() - 0.5) * 0.3,
    vy: (Math.random() - 0.5) * 0.3,
    size: Math.random() * 16 + 6,
    rotation: Math.random() * Math.PI * 2,
    vr: (Math.random() - 0.5) * 0.002,
    type: types[Math.floor(Math.random() * types.length)],
    alpha: Math.random() * 0.06 + 0.03,
  }))

  const drawCross = (x: number, y: number, s: number) => {
    const t = s * 0.3
    ctx.fillRect(x - s / 2, y - t / 2, s, t)
    ctx.fillRect(x - t / 2, y - s / 2, t, s)
  }

  const draw = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    const dark = isDark()

    for (const b of blocks) {
      b.x += b.vx
      b.y += b.vy
      b.rotation += b.vr
      if (b.x < -20 || b.x > canvas.width + 20) b.vx *= -1
      if (b.y < -20 || b.y > canvas.height + 20) b.vy *= -1

      ctx.save()
      ctx.translate(b.x, b.y)
      ctx.rotate(b.rotation)
      ctx.globalAlpha = b.alpha

      if (dark) {
        ctx.fillStyle = '#ffd600'
        ctx.strokeStyle = '#ff5722'
      } else {
        ctx.fillStyle = '#1a1a1a'
        ctx.strokeStyle = '#ff5722'
      }
      ctx.lineWidth = 1.5

      switch (b.type) {
        case 'square':
          ctx.fillRect(-b.size / 2, -b.size / 2, b.size, b.size)
          break
        case 'circle':
          ctx.beginPath()
          ctx.arc(0, 0, b.size / 2, 0, Math.PI * 2)
          ctx.fill()
          break
        case 'cross':
          drawCross(0, 0, b.size)
          break
      }

      ctx.restore()
    }

    animId = requestAnimationFrame(draw)
  }

  draw()

  onBeforeUnmount(() => {
    cancelAnimationFrame(animId)
    window.removeEventListener('resize', resize)
  })
})
</script>

<template>
  <canvas ref="canvasRef" class="particle-bg" />
</template>

<style scoped>
.particle-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
