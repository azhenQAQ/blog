<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicArticle, getAdjacentArticles } from '@/api/modules/article'
import type { ArticleDetailVO, ArticleVO } from '@/types/article'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import hljs from 'highlight.js'
import CommentSection from '@/components/blog/CommentSection.vue'
import { useTocStore } from '@/stores/toc'
import { parseHeadings, slugify } from '@/utils/toc'

const route = useRoute()
const router = useRouter()
const tocStore = useTocStore()

const article = ref<ArticleDetailVO | null>(null)
const prevArticle = ref<ArticleVO | null>(null)
const nextArticle = ref<ArticleVO | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

let observer: IntersectionObserver | null = null

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight(str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        const highlighted = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
        const lines = highlighted.split('\n')
        const numbered = lines
          .map((line, i) => `<span class="line-num">${i + 1}</span>${line}`)
          .join('\n')
        return `<pre class="hljs"><code data-language="${lang}">${numbered}</code></pre>`
      } catch {
        /* fallback */
      }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  },
})

md.use(markdownItAnchor, { level: [2, 3], slugify })

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return md.render(article.value.content)
})

const wordCount = computed(() => {
  if (!article.value?.content) return 0
  return article.value.content.replace(/[#*`>[\]()\-_|~\s]/g, '').length
})

const readTime = computed(() => {
  return Math.max(1, Math.ceil(wordCount.value / 200))
})

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function fetchArticle(id: number) {
  loading.value = true
  error.value = null
  try {
    const [articleData, adjacentData] = await Promise.all([
      getPublicArticle(id),
      getAdjacentArticles(id),
    ])
    article.value = articleData
    prevArticle.value = adjacentData.prev
    nextArticle.value = adjacentData.next
    document.title = articleData.title + ' - 废话回收站'
    tocStore.setHeadings(parseHeadings(articleData.content))
    await nextTick()
    setupScrollSpy()
  } catch (e: any) {
    error.value = e.message || '文章加载失败'
  } finally {
    loading.value = false
  }
}

function setupScrollSpy() {
  destroyScrollSpy()
  const headings = document.querySelectorAll('.article-content h2[id], .article-content h3[id]')
  if (headings.length === 0) return

  observer = new IntersectionObserver(
    () => {
      let best: Element | null = null
      for (const el of headings) {
        const rect = el.getBoundingClientRect()
        if (rect.top <= 150) {
          best = el
        }
      }
      if (best) {
        tocStore.setActiveId(best.id)
      }
    },
    { rootMargin: '-80px 0px -70% 0px' },
  )
  headings.forEach((el) => observer!.observe(el))
}

function destroyScrollSpy() {
  if (observer) {
    observer.disconnect()
    observer = null
  }
}

onMounted(() => {
  const id = Number(route.params.id)
  if (Number.isNaN(id) || id <= 0) {
    error.value = '无效的文章 ID'
    loading.value = false
    return
  }
  fetchArticle(id)
})

onUnmounted(() => {
  destroyScrollSpy()
  tocStore.clear()
})

watch(
  () => route.params.id,
  (newId) => {
    const id = Number(newId)
    if (!Number.isNaN(id) && id > 0) {
      destroyScrollSpy()
      tocStore.clear()
      fetchArticle(id)
      window.scrollTo({ top: 0 })
    }
  },
)
</script>

<template>
  <div class="detail">
    <!-- Loading -->
    <div v-if="loading" class="state-box">加载中...</div>

    <!-- Error -->
    <div v-else-if="error" class="state-box error">
      <p>{{ error }}</p>
      <button class="back-btn" @click="router.push('/')">返回首页</button>
    </div>

    <!-- Content -->
    <template v-else-if="article">
      <!-- Cover -->
      <div v-if="article.coverImage" class="cover-wrap">
        <img :src="article.coverImage" :alt="article.title" class="cover-img" />
      </div>

      <!-- Title -->
      <h1 class="article-title">{{ article.title }}</h1>

      <!-- Tags -->
      <div v-if="article.tags?.length" class="article-tags">
        <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
      </div>

      <!-- Meta -->
      <div class="article-meta">
        <span>发布于 {{ formatDate(article.createdAt) }}</span>
        <span v-if="article.createdAt !== article.updatedAt">
          更新于 {{ formatDate(article.updatedAt) }}
        </span>
        <span>{{ wordCount }} 字</span>
        <span>阅读约 {{ readTime }} 分钟</span>
        <span>浏览 {{ article.viewCount }} 次</span>
      </div>

      <!-- Content -->
      <div class="article-content markdown-body" v-html="renderedContent" />

      <!-- Comments -->
      <CommentSection type="comment" :article-id="article.id" />

      <!-- Adjacent nav -->
      <nav v-if="prevArticle || nextArticle" class="article-nav">
        <router-link
          v-if="prevArticle"
          :to="`/posts/${prevArticle.id}`"
          class="nav-link prev"
        >
          <span class="nav-label">‹ 上一篇</span>
          <span class="nav-title">{{ prevArticle.title }}</span>
        </router-link>
        <div v-else class="nav-link empty" />

        <router-link
          v-if="nextArticle"
          :to="`/posts/${nextArticle.id}`"
          class="nav-link next"
        >
          <span class="nav-label">下一篇 ›</span>
          <span class="nav-title">{{ nextArticle.title }}</span>
        </router-link>
        <div v-else class="nav-link empty" />
      </nav>
    </template>
  </div>
</template>

<style scoped>
/* States */
.state-box {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
  font-family: var(--font-heading);
  font-size: 1.2em;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.state-box.error p {
  color: var(--text-main);
  margin: 0 0 16px;
}

.back-btn {
  padding: 10px 24px;
  background: var(--accent);
  color: #fff;
  border: 3px solid #1a1a1a;
  cursor: pointer;
  font-family: var(--font-heading);
  font-size: 0.9em;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  box-shadow: 4px 4px 0 #1a1a1a;
  transition: all 0.1s ease;
}

.back-btn:hover {
  box-shadow: 2px 2px 0 #1a1a1a;
  transform: translate(2px, 2px);
}

[data-theme='dark'] .back-btn {
  border-color: #555;
  box-shadow: 4px 4px 0 #555;
}

[data-theme='dark'] .back-btn:hover {
  box-shadow: 2px 2px 0 #555;
}

/* Cover */
.cover-wrap {
  border: 3px solid var(--shadow-color);
  overflow: hidden;
  margin-bottom: 32px;
}

.cover-img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  display: block;
}

/* Title */
.article-title {
  font-family: var(--font-heading);
  font-size: 2.2em;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 16px;
  line-height: 1.2;
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

/* Tags */
.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tag {
  padding: 3px 12px;
  font-size: 0.8em;
  font-weight: 600;
  color: #1a1a1a;
  background: var(--accent-yellow);
}

/* Meta */
.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 18px;
  font-size: 0.85em;
  color: var(--text-muted);
  padding-bottom: 20px;
  border-bottom: 3px solid var(--shadow-color);
  margin-bottom: 28px;
}

/* Adjacent nav */
.article-nav {
  display: flex;
  gap: 16px;
  margin-top: 48px;
  padding-top: 24px;
  border-top: 3px solid var(--shadow-color);
}

.nav-link {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  text-decoration: none;
  transition: box-shadow 0.1s ease, transform 0.1s ease;
}

.nav-link:hover {
  box-shadow: var(--card-hover-shadow);
  transform: translate(-2px, -2px);
}

.nav-link.next {
  text-align: right;
  margin-left: auto;
}

.nav-link.empty {
  flex: 1;
  visibility: hidden;
}

.nav-label {
  font-family: var(--font-heading);
  font-size: 0.78em;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.nav-title {
  font-family: var(--font-heading);
  font-size: 0.92em;
  font-weight: 600;
  color: var(--text-strong);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (max-width: 900px) {
  .article-title {
    font-size: 1.5em;
  }

  .cover-img {
    max-height: 240px;
  }

  .article-nav {
    flex-direction: column;
    gap: 12px;
  }

  .nav-link.empty {
    display: none;
  }
}
</style>
