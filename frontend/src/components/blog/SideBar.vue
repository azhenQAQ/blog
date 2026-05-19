<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { listPublicArticleByPage } from '@/api/modules/article'
import { listPublicTags } from '@/api/modules/tag'
import { listGuestbook } from '@/api/modules/comment'
import type { TagVO } from '@/types/tag'
import TableOfContents from '@/components/blog/TableOfContents.vue'
import { useTocStore } from '@/stores/toc'

const SITE_START = new Date('2026-05-16')

const tocStore = useTocStore()

const author = {
  name: '阿臻',
  avatar: '/images/author-avatar.jpg',
}

const articleCount = ref(0)
const guestbookCount = ref(0)

const authorStats = computed(() => ({
  文章: articleCount.value,
  标签: tags.value.length,
  留言: guestbookCount.value,
}))

const bio = ref('正在加载中。。。')

async function fetchBio() {
  try {
    const res = await fetch('https://api.suyanw.cn/api/gushi.php?type=json')
    const data = await res.json()
    if (data.text) {
      bio.value = data.text
    }
  } catch {
    // 静默失败，保留默认 bio
  }
}

const tags = ref<TagVO[]>([])

function calcRunningDays(): number {
  const diff = Date.now() - SITE_START.getTime()
  return Math.max(1, Math.floor(diff / (1000 * 60 * 60 * 24)))
}

const info = computed(() => ({
  运行天数: calcRunningDays(),
  访问量: '12.3k',
}))

onMounted(() => {
  fetchBio()
  listPublicTags()
    .then((data) => {
      tags.value = data
    })
    .catch(() => {})
  listPublicArticleByPage(1, 1)
    .then((page) => {
      articleCount.value = page.total
    })
    .catch(() => {})
  listGuestbook()
    .then((list) => {
      guestbookCount.value = list.length
    })
    .catch(() => {})
})

const activeTab = ref<'tags' | 'info'>('tags')
</script>

<template>
  <aside class="sidebar">
    <!-- 作者卡片 -->
    <div class="card author-card">
      <div class="author-info">
        <div class="avatar">
          <img v-if="author.avatar" :src="author.avatar" :alt="author.name" />
          <span v-else>{{ author.name[0] }}</span>
        </div>
        <h3 class="author-name">{{ author.name }}</h3>
        <p class="author-bio">{{ bio }}</p>
        <div class="author-stats">
          <div v-for="(val, key) in authorStats" :key="key" class="stat">
            <span class="stat-val">{{ val }}</span>
            <span class="stat-label">{{ key }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 目录 -->
    <TableOfContents v-if="tocStore.headings.length > 0" />

    <!-- 公告 -->
    <div class="card announcement">
      <h4 class="card-title">公告</h4>
      <p>欢迎来到废话回收站，把想说的都倒在这里喵ovo～</p>
    </div>

    <!-- 标签 & 网站资讯（Tab 切换） -->
    <div class="card info-card">
      <div class="tab-header">
        <button :class="['tab-btn', { active: activeTab === 'tags' }]" @click="activeTab = 'tags'">
          标签
        </button>
        <button :class="['tab-btn', { active: activeTab === 'info' }]" @click="activeTab = 'info'">
          资讯
        </button>
      </div>
      <div class="tab-body">
        <div v-show="activeTab === 'tags'" class="tags">
          <span v-for="tag in tags" :key="tag.id" class="tag">
            {{ tag.name }}
            <small class="tag-count">{{ tag.articleCount }}</small>
          </span>
        </div>
        <div v-show="activeTab === 'info'" class="info-list">
          <div v-for="(val, key) in info" :key="key" class="info-row">
            <span class="info-label">{{ key }}</span>
            <span class="info-val">{{ val }}</span>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  padding: 15px;
}

.card-title {
  font-family: var(--font-heading);
  font-size: 1.05em;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 5px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

/* Author */
.author-card {
  padding: 0;
  overflow: hidden;
  background:
    linear-gradient(to bottom, transparent 10%, var(--card-bg) 100%),
    url('/images/author-background.jpg') center/cover no-repeat;
  background-size:
    100% auto,
    cover;
}

.author-info {
  padding: 40px 20px 5px;
  text-align: center;
}

.avatar {
  width: 70px;
  height: 70px;
  background: #1a1a1a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-heading);
  font-size: 1.6em;
  font-weight: 700;
  margin: 0 auto 0px;
  border: 3px solid var(--card-bg);
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.author-name {
  margin: 0 0 4px;
  font-family: var(--font-heading);
  font-size: 1.2em;
  font-weight: 700;
  color: var(--text-strong);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.author-bio {
  margin: 0 0 14px;
  font-size: 0.95em;
  color: var(--text-main);
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 32px;
  border-top: 3px solid var(--shadow-color);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-val {
  font-family: var(--font-heading);
  font-size: 1.2em;
  font-weight: 700;
  color: var(--text-strong);
}

.stat-label {
  font-size: 0.85em;
  color: var(--text-main);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Announcement */
.announcement p {
  margin: 0;
  font-size: 0.9em;
  color: var(--text-main);
  line-height: 1.8;
}

/* Info card (标签 + 资讯 merged) */
.info-card {
  padding: 0;
  max-height: 360px;
  display: flex;
  flex-direction: column;
}

.tab-header {
  display: flex;
  border-bottom: 3px solid var(--shadow-color);
}

.tab-btn {
  flex: 1;
  padding: 12px 8px;
  font-family: var(--font-heading);
  font-size: 0.85em;
  font-weight: 600;
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: all 0.1s ease;
}

.tab-btn:hover {
  color: var(--text-main);
}

.tab-btn.active {
  color: #fff;
  background: #1a1a1a;
}

[data-theme='dark'] .tab-btn.active {
  background: var(--accent);
}

.tab-body {
  padding: 16px 20px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
  scrollbar-width: none;
}

.tab-body::-webkit-scrollbar {
  display: none;
}

/* Tags */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-block;
  padding: 3px 10px;
  font-size: 0.78em;
  font-weight: 600;
  color: #1a1a1a;
  background: var(--accent-yellow);
  transition: background 0.1s ease;
  cursor: default;
}

.tag-count {
  margin-left: 3px;
  font-size: 0.75em;
  color: #666;
}

.tag:hover {
  background: var(--accent);
  color: #fff;
}

.tag:hover .tag-count {
  color: rgba(255, 255, 255, 0.8);
}

/* Site info */
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 0.88em;
}

.info-row + .info-row {
  border-top: 2px solid var(--shadow-color);
}

.info-label {
  color: var(--text-muted);
  font-weight: 600;
}

.info-val {
  font-family: var(--font-heading);
  color: var(--text-strong);
  font-weight: 700;
  font-size: 1.05em;
}
</style>
