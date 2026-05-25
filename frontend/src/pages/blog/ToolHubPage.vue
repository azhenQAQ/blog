<script setup lang="ts">
interface Tool {
  id: string
  name: string
  desc: string
  path: string
  external: boolean
}

const tools: Tool[] = [
  {
    id: 'image-process',
    name: '图片处理',
    desc: '支持图片压缩、裁剪与格式转换，纯本地处理，保护隐私安全',
    path: '/tools/image-process',
    external: false,
  },
  {
    id: 'json-formatter',
    name: 'JSON 格式化',
    desc: 'JSON 美化、压缩、校验与转义处理，支持语法高亮与树形浏览',
    path: '/tools/json-formatter',
    external: false,
  },
  {
    id: 'translate',
    name: '翻译',
    desc: '中英文双向翻译，支持单词、短语翻译与英文原文朗读',
    path: '/tools/translate',
    external: false,
  },
  {
    id: 'ai-code',
    name: 'AI零代码应用生成平台',
    desc: '一句话，一键生成应用',
    path: 'http://ai.azhenna.top',
    external: true,
  },
]
</script>

<template>
  <div class="tool-hub">
    <p class="page-subtitle">个人工具集，让日常开发中的小需求变得简单高效</p>

    <div class="tools-grid">
      <a
        v-for="tool in tools"
        :key="tool.id"
        :href="tool.path"
        target="_blank"
        rel="noopener noreferrer"
        class="tool-card"
      >
        <h3 class="tool-name">{{ tool.name }}{{ tool.external ? ' ↗' : '' }}</h3>
        <p class="tool-desc">{{ tool.desc }}</p>
        <span class="tool-arrow">→</span>
      </a>
    </div>

    <div v-if="tools.length === 0" class="empty-state">
      <p>还没有工具喵～</p>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-family: var(--font-heading);
  font-size: 2.2em;
  font-weight: 800;
  color: var(--text-strong);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 1.05em;
  color: var(--text-secondary, #666);
  margin: 0 0 10px 0;
  font-family: var(--font-body);
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.tool-card {
  display: block;
  padding: 28px 24px;
  background: var(--card-bg);
  border: 3px solid var(--shadow-color);
  box-shadow: 6px 6px 0 var(--shadow-color);
  text-decoration: none;
  color: var(--text-main);
  transition: all 0.1s ease;
  position: relative;
}

.tool-card:hover {
  transform: translate(-2px, -2px);
  box-shadow: 10px 10px 0 var(--shadow-color);
}

.tool-name {
  font-family: var(--font-heading);
  font-size: 1.25em;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--text-strong);
}

.tool-desc {
  font-size: 0.95em;
  line-height: 1.6;
  color: var(--text-secondary, #555);
  margin: 0;
}

.tool-arrow {
  position: absolute;
  bottom: 16px;
  right: 20px;
  font-size: 1.4em;
  font-weight: 700;
  color: var(--accent);
  transition: transform 0.1s ease;
}

.tool-card:hover .tool-arrow {
  transform: translateX(4px);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary, #999);
  font-size: 1.1em;
}

@media (max-width: 900px) {
  .tools-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .page-title {
    font-size: 1.7em;
  }

  .tool-card {
    padding: 20px 18px;
  }
}
</style>
