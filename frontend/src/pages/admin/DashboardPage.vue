<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminTabsStore } from '@/stores/adminTabs'
import { ElMessage } from 'element-plus'
import { getDashboard } from '@/api/modules/dashboard'
import type { DashboardVO } from '@/types/dashboard'
import type { ArticleVO } from '@/types/article'

defineOptions({ name: 'DashboardPage' })

const router = useRouter()
const tabStore = useAdminTabsStore()

const loading = ref(false)
const dashboard = ref<DashboardVO | null>(null)
const echartsReady = ref(false)
let pieChart: any = null
const pieChartRef = ref<HTMLDivElement | null>(null)

async function fetchDashboard() {
  loading.value = true
  try {
    dashboard.value = await getDashboard()
  } catch {
    ElMessage.error('获取控制台数据失败')
  } finally {
    loading.value = false
  }
}

async function initPieChart() {
  if (!pieChartRef.value) return
  try {
    const echarts = await import('echarts')
    pieChart = (echarts as any).init(pieChartRef.value)
    echartsReady.value = true
    updatePieChart()
  } catch {
    // ECharts 未安装，饼图不可用
  }
}

function updatePieChart() {
  if (!pieChart || !dashboard.value?.tagArticleStats) return
  const stats = dashboard.value.tagArticleStats.filter((s) => s.articleCount > 0)
  if (stats.length === 0) {
    pieChart.clear()
    pieChart.showLoading({ text: '暂无数据', maskColor: 'transparent' })
    return
  }
  pieChart.hideLoading()
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 篇 ({d}%)',
    },
    series: [
      {
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: 'var(--el-bg-color, #fff)',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          color: 'var(--el-text-color-primary, #303133)',
          fontSize: 12,
        },
        labelLine: { show: true },
        data: stats.map((s) => ({
          name: s.tagName,
          value: s.articleCount,
          itemStyle: { color: s.tagColor || undefined },
        })),
      },
    ],
  })
}

function handleResize() {
  pieChart?.resize()
}

function navigateToEdit(article: ArticleVO) {
  const path = `/admin/posts/${article.id}/edit`
  tabStore.openTab(path)
  router.push(path)
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

watch(
  () => dashboard.value?.tagArticleStats,
  () => {
    if (echartsReady.value) nextTick(updatePieChart)
  },
)

onMounted(() => {
  fetchDashboard().then(() => {
    echartsReady.value = true
    nextTick(initPieChart)
  })
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <!-- Section 1: 文章统计 -->
    <section class="dashboard-section">
      <div class="section-title">
        <el-icon><Document /></el-icon>
        <span>文章统计</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">总文章数</div>
            <div class="stat-value primary">{{ dashboard?.articleStats.total ?? '—' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">已发布</div>
            <div class="stat-value success">{{ dashboard?.articleStats.published ?? '—' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">草稿</div>
            <div class="stat-value warning">{{ dashboard?.articleStats.draft ?? '—' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-label">已归档</div>
            <div class="stat-value info">{{ dashboard?.articleStats.archived ?? '—' }}</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- Section 2: 访问统计 + 资源统计 -->
    <section class="dashboard-section">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Monitor /></el-icon>
                <span>访问统计</span>
              </div>
            </template>
            <div class="stat-group">
              <div class="stat-item">
                <span class="stat-item-label">总浏览量 (PV)</span>
                <span class="stat-value primary">{{ dashboard?.visitStats.totalViews ?? '—' }}</span>
              </div>
              <el-divider />
              <div class="stat-item">
                <span class="stat-item-label">独立访客 (UV)</span>
                <span class="stat-value primary">{{ dashboard?.visitStats.uniqueVisitors ?? '—' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Folder /></el-icon>
                <span>资源统计</span>
              </div>
            </template>
            <el-row :gutter="16">
              <el-col :span="6">
                <div class="resource-item">
                  <span class="resource-label">标签</span>
                  <span class="resource-value warning">{{ dashboard?.resourceStats.tags ?? '—' }}</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="resource-item">
                  <span class="resource-label">评论</span>
                  <span class="resource-value info">{{ dashboard?.resourceStats.comments ?? '—' }}</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="resource-item">
                  <span class="resource-label">附件</span>
                  <span class="resource-value">{{ dashboard?.resourceStats.attachments ?? '—' }}</span>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="resource-item">
                  <span class="resource-label">音乐</span>
                  <span class="resource-value">{{ dashboard?.resourceStats.music ?? '—' }}</span>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- Section 3: 热门文章 + 草稿提醒 -->
    <section class="dashboard-section">
      <el-row :gutter="16">
        <el-col :span="16">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon>
                <span>热门文章排行</span>
                <el-tag size="small" type="warning" effect="plain">TOP 10</el-tag>
              </div>
            </template>
            <el-table
              :data="dashboard?.hotArticles ?? []"
              stripe
              size="small"
              empty-text="暂无数据"
            >
              <el-table-column type="index" label="#" width="50" align="center" />
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="viewCount" label="浏览量" width="100" align="center">
                <template #default="{ row }">
                  <el-tag size="small" effect="plain" type="warning">
                    {{ row.viewCount }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="120" align="center">
                <template #default="{ row }">
                  {{ formatDate(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="navigateToEdit(row)">
                    编辑
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><EditPen /></el-icon>
                <span>草稿提醒</span>
                <el-tag
                  v-if="(dashboard?.draftArticles?.length ?? 0) > 0"
                  size="small"
                  type="info"
                  effect="plain"
                >
                  {{ dashboard?.draftArticles.length }}
                </el-tag>
              </div>
            </template>
            <div v-if="dashboard?.draftArticles && dashboard.draftArticles.length > 0" class="draft-list">
              <div
                v-for="article in dashboard.draftArticles"
                :key="article.id"
                class="draft-item"
                @click="navigateToEdit(article)"
              >
                <div class="draft-title">{{ article.title }}</div>
                <div class="draft-meta">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDate(article.updatedAt) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无草稿" :image-size="80" />
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- Section 4: 标签文章数饼图 -->
    <section v-if="echartsReady" class="dashboard-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><PieChart /></el-icon>
            <span>标签文章分布</span>
          </div>
        </template>
        <div ref="pieChartRef" class="pie-chart-container"></div>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

/* 文章统计单值卡片 */
.stat-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--el-text-color-primary);
}

.stat-value.primary { color: var(--el-color-primary); }
.stat-value.success { color: var(--el-color-success); }
.stat-value.warning { color: var(--el-color-warning); }
.stat-value.info    { color: var(--el-color-info); }

/* 访问统计成组 */
.stat-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.stat-item .stat-value {
  font-size: 24px;
}

/* 资源统计 */
.resource-item {
  text-align: center;
  padding: 8px 0;
}

.resource-label {
  display: block;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.resource-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.resource-value.warning { color: var(--el-color-warning); }
.resource-value.info    { color: var(--el-color-info); }

/* 草稿列表 */
.draft-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.draft-item {
  padding: 10px 12px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  cursor: pointer;
  transition: background 0.2s;
}

.draft-item:hover {
  background: var(--el-color-primary-light-9);
}

.draft-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 饼图容器 */
.pie-chart-container {
  width: 100%;
  height: 360px;
}
</style>
