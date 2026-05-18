<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { listPublicTopArticles, listPublicArticleByPage } from '@/api/modules/article'
import type { ArticleVO } from '@/types/article'

const topArticles = ref<ArticleVO[]>([])
const articles = ref<ArticleVO[]>([])
const current = ref(1)
const total = ref(0)
const totalPages = ref(0)
const pageSize = 10
const loading = ref(false)

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function fetchData(page: number) {
  loading.value = true
  try {
    const [topRes, pageRes] = await Promise.all([
      listPublicTopArticles(),
      listPublicArticleByPage(page, pageSize),
    ])
    topArticles.value = topRes
    articles.value = pageRes.records
    total.value = pageRes.total
    totalPages.value = pageRes.pages
    current.value = page
  } catch {
    // 静默处理，页面显示空状态
  } finally {
    loading.value = false
  }
}

const visiblePages = computed(() => {
  const pages: number[] = []
  const tp = totalPages.value
  if (tp <= 1) return pages
  let start = Math.max(1, current.value - 2)
  let end = Math.min(tp, start + 4)
  if (end - start < 4) start = Math.max(1, end - 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function goToPage(page: number) {
  if (page < 1 || page > totalPages.value || page === current.value) return
  fetchData(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => fetchData(1))
</script>

<template>
  <div class="home">
    <!-- 置顶文章 -->
    <section v-if="topArticles.length" class="post-list">
      <h2 class="section-title">📌 置顶文章</h2>
      <article
        v-for="(post, idx) in topArticles"
        :key="post.id"
        class="post-card"
        :class="{ reverse: idx % 2 === 1 }"
      >
        <router-link :to="`/posts/${post.id}`" class="post-card-link">
          <div class="post-cover">
            <img
              v-if="post.coverImage"
              :src="post.coverImage"
              :alt="post.title"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">
              <span>{{ post.title.slice(0, 1) }}</span>
            </div>
          </div>
          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <div class="post-meta">
              <span class="post-date">{{ formatDate(post.createdAt) }}</span>
              <span v-if="post.authorName" class="post-author">{{ post.authorName }}</span>
            </div>
            <p class="post-summary">{{ post.summary || '暂无摘要' }}</p>
            <div v-if="post.tags?.length" class="post-tags">
              <span v-for="tag in post.tags" :key="tag.id" class="post-tag">{{ tag.name }}</span>
            </div>
          </div>
        </router-link>
      </article>
    </section>

    <!-- 最新文章 -->
    <section class="post-list">
      <h2 class="section-title">最新文章</h2>

      <div v-if="loading && !articles.length" class="loading-state">加载中...</div>

      <template v-else-if="articles.length">
        <article
          v-for="(post, idx) in articles"
          :key="post.id"
          class="post-card"
          :class="{ reverse: idx % 2 === 1 }"
        >
          <router-link :to="`/posts/${post.id}`" class="post-card-link">
            <div class="post-cover">
              <img
                v-if="post.coverImage"
                :src="post.coverImage"
                :alt="post.title"
                class="cover-img"
              />
              <div v-else class="cover-placeholder">
                <span>{{ post.title.slice(0, 1) }}</span>
              </div>
            </div>
            <div class="post-content">
              <h3 class="post-title">{{ post.title }}</h3>
              <div class="post-meta">
                <span class="post-date">{{ formatDate(post.createdAt) }}</span>
                <span v-if="post.authorName" class="post-author">{{ post.authorName }}</span>
              </div>
              <p class="post-summary">{{ post.summary || '暂无摘要' }}</p>
              <div v-if="post.tags?.length" class="post-tags">
                <span v-for="tag in post.tags" :key="tag.id" class="post-tag">{{ tag.name }}</span>
              </div>
            </div>
          </router-link>
        </article>
      </template>

      <div v-else-if="!loading" class="empty-state">暂无文章</div>
    </section>

    <!-- 分页 -->
    <nav v-if="totalPages > 1" class="pagination">
      <button :disabled="current <= 1" @click="goToPage(current - 1)">‹ 上一页</button>
      <button
        v-for="p in visiblePages"
        :key="p"
        :class="{ active: p === current }"
        @click="goToPage(p)"
      >
        {{ p }}
      </button>
      <button :disabled="current >= totalPages" @click="goToPage(current + 1)">下一页 ›</button>
    </nav>
  </div>
</template>

<style scoped>
.section-title {
  font-family: var(--font-heading);
  font-size: 1.4em;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 24px;
  padding-left: 12px;
  border-left: 6px solid var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

/* Post card */
.post-card {
  display: flex;
  height: 18em;
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
  margin-bottom: 28px;
}

.post-card:hover {
  box-shadow: var(--card-hover-shadow);
  transform: translate(-2px, -2px);
}

.post-card.reverse {
  flex-direction: row-reverse;
}

.post-card-link {
  display: flex;
  width: 100%;
  color: inherit;
  text-decoration: none;
}

.post-card.reverse .post-card-link {
  flex-direction: row-reverse;
}

.post-cover {
  width: 44%;
  flex-shrink: 0;
  border-right: 3px solid var(--shadow-color);
}

.post-card.reverse .post-cover {
  border-right: none;
  border-left: 3px solid var(--shadow-color);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-yellow);
  color: #1a1a1a;
  font-family: var(--font-heading);
  font-size: 3em;
  font-weight: 700;
}

[data-theme='dark'] .cover-placeholder {
  background: var(--accent-bg);
  color: var(--accent-yellow);
}

.post-content {
  flex: 1;
  padding: 28px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.post-title {
  font-family: var(--font-heading);
  font-size: 1.5em;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 10px;
  line-height: 1.3;
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.post-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
  font-size: 0.85em;
  color: var(--text-muted);
}

.post-summary {
  font-size: 0.92em;
  color: var(--text-main);
  line-height: 1.8;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.post-tag {
  padding: 3px 10px;
  font-size: 0.78em;
  font-weight: 600;
  color: #1a1a1a;
  background: var(--accent-yellow);
  transition: background 0.1s ease;
  cursor: default;
}

.post-tag:hover {
  background: var(--accent);
  color: #fff;
}

/* Loading / Empty */
.loading-state,
.empty-state {
  text-align: center;
  padding: 48px 0;
  color: var(--text-muted);
  font-family: var(--font-heading);
  font-size: 1.2em;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0;
  padding: 20px 0;
}

.pagination button {
  padding: 8px 16px;
  border: 3px solid var(--shadow-color);
  background: var(--card-bg);
  color: var(--text-main);
  cursor: pointer;
  transition: all 0.1s ease;
  font-family: var(--font-heading);
  font-size: 0.9em;
  font-weight: 600;
  margin-left: -3px;
}

.pagination button:first-child {
  margin-left: 0;
}

.pagination button:hover:not(:disabled) {
  background: var(--accent);
  color: #fff;
  border-color: var(--accent);
  z-index: 1;
  position: relative;
}

.pagination button.active {
  background: #1a1a1a;
  color: #fff;
  border-color: #1a1a1a;
  z-index: 1;
  position: relative;
}

[data-theme='dark'] .pagination button.active {
  background: var(--accent);
  border-color: var(--accent);
}

.pagination button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .post-card,
  .post-card.reverse {
    height: auto;
    flex-direction: column;
  }

  .post-card-link,
  .post-card.reverse .post-card-link {
    flex-direction: column;
  }

  .post-cover {
    width: 100%;
    height: 180px;
    border-right: none;
    border-bottom: 3px solid var(--shadow-color);
  }

  .post-card.reverse .post-cover {
    border-left: none;
    border-bottom: 3px solid var(--shadow-color);
  }

  .post-content {
    padding: 20px;
  }

  .post-title {
    font-size: 1.2em;
  }

  .pagination button {
    padding: 6px 10px;
    font-size: 0.82em;
  }
}
</style>
