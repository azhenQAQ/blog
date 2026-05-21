<script setup lang="ts">
import { computed } from 'vue'
import { musicIcons } from '@/assets/musicIcons'

const props = withDefaults(
  defineProps<{
    name: string
    size?: number
  }>(),
  { size: 16 },
)

const icon = computed(() => musicIcons[props.name])
</script>

<template>
  <svg
    v-if="icon"
    :width="props.size"
    :height="props.size"
    :viewBox="icon.viewBox"
    xmlns="http://www.w3.org/2000/svg"
    :fill="icon.fill ? 'currentColor' : 'none'"
    :stroke="icon.fill ? 'none' : 'currentColor'"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
  >
    <path :d="icon.d" />
  </svg>
</template>
