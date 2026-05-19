<script setup lang="ts">
import { ref } from 'vue'
import { useTocStore } from '@/stores/toc'
import type { HeadingItem } from '@/utils/toc'

const tocStore = useTocStore()

const collapsed = ref<Set<string>>(new Set())

function isCollapsed(id: string): boolean {
  return collapsed.value.has(id)
}

function toggleCollapse(id: string) {
  if (collapsed.value.has(id)) {
    collapsed.value.delete(id)
  } else {
    collapsed.value.add(id)
  }
  collapsed.value = new Set(collapsed.value)
}

function scrollToHeading(id: string) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
</script>

<template>
  <div v-if="tocStore.headings.length > 0" class="card toc-card">
    <h4 class="card-title">目录</h4>
    <nav class="toc-nav">
      <template v-for="item in tocStore.headings" :key="item.id">
        <div class="toc-item toc-h2" :class="{ active: tocStore.activeId === item.id }">
          <span
            v-if="item.children.length > 0"
            class="toc-toggle"
            :class="{ collapsed: isCollapsed(item.id) }"
            @click.stop="toggleCollapse(item.id)"
          />
          <span v-else class="toc-toggle placeholder" />
          <a
            class="toc-link"
            :href="`#${item.id}`"
            @click.prevent="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </a>
        </div>
        <div v-if="item.children.length && !isCollapsed(item.id)" class="toc-children">
          <div
            v-for="child in item.children"
            :key="child.id"
            class="toc-item toc-h3"
            :class="{ active: tocStore.activeId === child.id }"
          >
            <a
              class="toc-link"
              :href="`#${child.id}`"
              @click.prevent="scrollToHeading(child.id)"
            >
              {{ child.text }}
            </a>
          </div>
        </div>
      </template>
    </nav>
  </div>
</template>

<style scoped>
.toc-card {
  padding: 15px;
}

.toc-card .card-title {
  margin-bottom: 8px;
}

.toc-nav {
  display: flex;
  flex-direction: column;
}

.toc-item {
  display: flex;
  align-items: baseline;
  padding: 4px 0;
  border-left: 3px solid transparent;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.toc-item.active {
  border-left-color: var(--accent);
  background: var(--accent-bg);
}

.toc-toggle {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 0.6em;
  color: var(--text-muted);
  margin-right: 4px;
  transition: transform 0.15s ease;
  user-select: none;
}

.toc-toggle::before {
  content: '▼';
}

.toc-toggle.collapsed::before {
  content: '▶';
}

.toc-toggle.placeholder {
  visibility: hidden;
}

.toc-link {
  flex: 1;
  font-size: 0.85em;
  color: var(--text-main);
  text-decoration: none;
  line-height: 1.5;
  padding: 2px 0;
  transition: color 0.1s ease;
  word-break: break-word;
}

.toc-link:hover {
  color: var(--accent);
}

.toc-item.active .toc-link {
  color: var(--accent);
  font-weight: 600;
}

.toc-h3 .toc-link {
  font-size: 0.8em;
  padding-left: 28px;
  color: var(--text-muted);
}

.toc-h3.active .toc-link {
  color: var(--accent);
}
</style>
