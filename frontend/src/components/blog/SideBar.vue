<script setup lang="ts">
const author = {
  name: '山岳',
  bio: '一只会敲代码的猫',
  avatar: '',
  stats: { posts: 42, tags: 18, categories: 6 },
}

const tags = [
  'Vue.js', 'TypeScript', 'Node.js', 'CSS', 'Docker',
  'Go', 'Rust', '数据库', 'DevOps', '算法',
]

const info = {
 运行天数: 365,
 访问量: '12.3k',
}
</script>

<template>
  <aside class="sidebar">
    <!-- 作者卡片 -->
    <div class="card author-card">
      <div class="author-bg" />
      <div class="author-info">
        <div class="avatar">
          <span>{{ author.name[0] }}</span>
        </div>
        <h3 class="author-name">{{ author.name }}</h3>
        <p class="author-bio">{{ author.bio }}</p>
        <div class="author-stats">
          <div v-for="(val, key) in author.stats" :key="key" class="stat">
            <span class="stat-val">{{ val }}</span>
            <span class="stat-label">{{ key }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 公告 -->
    <div class="card announcement">
      <h4 class="card-title">📢 公告</h4>
      <p>欢迎来到废话回收站，把想说的都倒在这里喵～</p>
    </div>

    <!-- 标签云 -->
    <div class="card tag-cloud">
      <h4 class="card-title">🏷 标签</h4>
      <div class="tags">
        <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>

    <!-- 网站资讯 -->
    <div class="card site-info">
      <h4 class="card-title">📊 网站资讯</h4>
      <div v-for="(val, key) in info" :key="key" class="info-row">
        <span class="info-label">{{ key }}</span>
        <span class="info-val">{{ val }}</span>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: var(--card-bg);
  border: var(--card-border);
  border-radius: var(--radius-card);
  padding: 20px;
  box-shadow: var(--card-shadow);
  transition: var(--card-transition);
}

.card:hover {
  box-shadow: var(--card-hover-shadow);
}

.card-title {
  font-size: 1em;
  color: var(--text-strong);
  margin: 0 0 12px;
}

/* Author */
.author-card {
  padding: 0;
  overflow: hidden;
}

.author-bg {
  height: 80px;
  background: linear-gradient(135deg, var(--accent), var(--accent-strong));
  opacity: 0.7;
}

.author-info {
  padding: 0 20px 20px;
  margin-top: -40px;
  text-align: center;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.6em;
  font-weight: 700;
  margin: 0 auto 10px;
  border: 3px solid var(--card-bg);
}

.author-name {
  margin: 0 0 4px;
  font-size: 1.15em;
  color: var(--text-strong);
}

.author-bio {
  margin: 0 0 14px;
  font-size: 0.85em;
  color: var(--text-muted);
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-val {
  font-size: 1.1em;
  font-weight: 700;
  color: var(--text-strong);
}

.stat-label {
  font-size: 0.75em;
  color: var(--text-muted);
}

/* Announcement */
.announcement p {
  margin: 0;
  font-size: 0.9em;
  color: var(--text-main);
  line-height: 1.8;
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
  color: var(--text-main);
  background: var(--accent-bg);
  border-radius: var(--radius-sm);
  transition: var(--card-transition);
  cursor: default;
}

.tag:hover {
  color: #fff;
  background: var(--accent);
}

/* Site info */
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 0.88em;
}

.info-row + .info-row {
  border-top: 1px solid var(--border-color);
}

.info-label {
  color: var(--text-muted);
}

.info-val {
  color: var(--text-strong);
  font-weight: 600;
}
</style>
