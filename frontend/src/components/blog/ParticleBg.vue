<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const canvasRef = ref<HTMLCanvasElement>()
let animId = 0

interface Point {
  x: number
  y: number
  vx: number
  vy: number
  size: number
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
  const count = Math.min(60, Math.floor((canvas.width * canvas.height) / 20000))

  const points: Point[] = Array.from({ length: count }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    vx: (Math.random() - 0.5) * 0.6,
    vy: (Math.random() - 0.5) * 0.6,
    size: Math.random() * 2 + 0.5,
  }))

  const draw = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    const dark = isDark()
    const lineColor = dark ? 'rgba(73, 177, 245, 0.15)' : 'rgba(73, 177, 245, 0.12)'
    const dotColor = dark ? 'rgba(73, 177, 245, 0.25)' : 'rgba(73, 177, 245, 0.2)'

    for (const p of points) {
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1
    }

    for (let i = 0; i < points.length; i++) {
      for (let j = i + 1; j < points.length; j++) {
        const dx = points[i].x - points[j].x
        const dy = points[i].y - points[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 150) {
          ctx.beginPath()
          ctx.moveTo(points[i].x, points[i].y)
          ctx.lineTo(points[j].x, points[j].y)
          ctx.strokeStyle = lineColor
          ctx.lineWidth = 0.6
          ctx.stroke()
        }
      }
    }

    for (const p of points) {
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = dotColor
      ctx.fill()
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
