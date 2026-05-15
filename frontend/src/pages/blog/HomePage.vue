<script setup lang="ts">
const heroTitle = '废话回收站'

const posts = [
  {
    id: 1,
    title: '深入理解 Vue 3 响应式原理',
    date: '2026-04-15',
    category: '前端开发',
    tags: ['Vue.js', '响应式'],
    summary:
      'Vue 3 的响应式系统基于 Proxy 实现，相比 Vue 2 的 Object.defineProperty 有着更好的性能和更完整的能力。本文从源码角度深入解析其实现机制...',
  },
  {
    id: 2,
    title: 'Rust 所有权系统详解',
    date: '2026-04-08',
    category: '编程语言',
    tags: ['Rust'],
    summary:
      '所有权是 Rust 最独特的特性之一，它让 Rust 在不需要垃圾回收器的情况下保证内存安全。本文通过实际示例来理解所有权、借用和生命周期...',
  },
  {
    id: 3,
    title: '搭建个人 CI/CD 流水线',
    date: '2026-03-28',
    category: 'DevOps',
    tags: ['Docker', 'CI/CD'],
    summary:
      '使用 GitHub Actions 配合 Docker 搭建一套轻量的 CI/CD 流水线，实现自动化测试、构建和部署，让代码提交到上线完全自动化...',
  },
  {
    id: 4,
    title: 'CSS Container Queries 实战指南',
    date: '2026-03-18',
    category: '前端开发',
    tags: ['CSS'],
    summary:
      'Container Queries 让组件可以根据容器尺寸而非视口尺寸来调整样式，这是响应式设计的一次重要进化。本文通过实际案例来学习如何使用...',
  },
]
</script>

<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <h1 class="hero-title">{{ heroTitle }}</h1>
    </section>

    <!-- Post list -->
    <section class="post-list">
      <h1 class="section-title">最新文章</h1>
      <article
        v-for="(post, idx) in posts"
        :key="post.id"
        class="post-card"
        :class="{ reverse: idx % 2 === 1 }"
      >
        <div class="post-cover">
          <div class="cover-placeholder">
            <span>{{ post.category }}</span>
          </div>
        </div>
        <div class="post-content">
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-meta">
            <span class="post-date">{{ post.date }}</span>
            <span class="post-category">{{ post.category }}</span>
          </div>
          <p class="post-summary">{{ post.summary }}</p>
          <div class="post-tags">
            <span v-for="tag in post.tags" :key="tag" class="post-tag">{{ tag }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
/* Hero */
.hero {
  text-align: center;
  padding: 24px 0;
}

.hero-title {
  font-size: 5em;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0;
  letter-spacing: 0.04em;
}

/* Post list */
.post-card {
  display: flex;
  height: 18em;
  background: var(--card-bg);
  border: var(--card-border);
  border-radius: var(--radius-card);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  transition: var(--card-transition);
  margin-bottom: 24px;
}

.post-card:hover {
  box-shadow: var(--card-hover-shadow);
}

.post-card.reverse {
  flex-direction: row-reverse;
}

.post-cover {
  width: 44%;
  flex-shrink: 0;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--accent-bg), rgba(73, 177, 245, 0.2));
  color: var(--accent);
  font-size: 1.2em;
  font-weight: 600;
}

.post-content {
  flex: 1;
  padding: 28px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.post-title {
  font-size: 1.5em;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 10px;
  line-height: 1.4;
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
  color: var(--text-main);
  background: var(--accent-bg);
  border-radius: var(--radius-sm);
  transition: var(--card-transition);
  cursor: default;
}

.post-tag:hover {
  color: #fff;
  background: var(--accent);
}

@media (max-width: 900px) {
  .hero-title {
    font-size: 2.8em;
  }

  .post-card,
  .post-card.reverse {
    flex-direction: column;
    height: auto;
  }

  .post-cover {
    width: 100%;
    height: 180px;
  }

  .post-content {
    padding: 20px;
  }

  .post-title {
    font-size: 1.25em;
  }
}
</style>
