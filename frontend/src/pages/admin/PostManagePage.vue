<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listArticleByPage,
  deleteArticle,
  restoreArticle,
  permanentDeleteArticle,
} from '@/api/modules/article'
import { ARTICLE_STATUS, articleStatusLabel } from '@/types/article'
import { useAdminTabsStore } from '@/stores/adminTabs'
import type { ArticleVO, ArticleQueryRequest } from '@/types/article'

defineOptions({ name: 'PostManagePage' })

const router = useRouter()
const tabStore = useAdminTabsStore()

const loading = ref(false)
const articleList = ref<ArticleVO[]>([])
const total = ref(0)

const queryForm = reactive<ArticleQueryRequest>({
  title: '',
  status: undefined,
  current: 1,
  pageSize: 10,
})

// 状态下拉框绑定的值（用 string 来容纳 'recycle_bin'）
const statusSelect = ref<string | undefined>(undefined)

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '已发布', value: String(ARTICLE_STATUS.PUBLISHED) },
  { label: '草稿', value: String(ARTICLE_STATUS.DRAFT) },
  { label: '已归档', value: String(ARTICLE_STATUS.ARCHIVED) },
  { label: '回收站', value: 'recycle_bin' },
]

function handleStatusChange(val: string | undefined) {
  if (val === 'recycle_bin') {
    queryForm.isRecycleBin = true
    queryForm.status = undefined
  } else {
    queryForm.isRecycleBin = false
    queryForm.status = val !== undefined && val !== '' ? Number(val) : undefined
  }
  handleSearch()
}

async function fetchList() {
  loading.value = true
  try {
    const page = await listArticleByPage({ ...queryForm })
    articleList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取文章列表失败，${msg}`)
    articleList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchList()
}

function handleReset() {
  queryForm.title = ''
  queryForm.status = undefined
  queryForm.isRecycleBin = false
  queryForm.current = 1
  statusSelect.value = undefined
  fetchList()
}

function handlePageChange(page: number) {
  queryForm.current = page
  fetchList()
}

function handleSizeChange(size: number) {
  queryForm.pageSize = size
  queryForm.current = 1
  fetchList()
}

function handleCreate() {
  const path = '/admin/posts/new'
  if (tabStore.tabs.some((t) => t.path === path)) {
    tabStore.setActiveTab(path)
    router.push(path)
    return
  }
  router.push(path)
}

function handleEdit(article: ArticleVO) {
  const path = `/admin/posts/${article.id}/edit`
  if (tabStore.tabs.some((t) => t.path === path)) {
    tabStore.setActiveTab(path)
    router.push(path)
    return
  }
  router.push(path)
}

function handleDelete(article: ArticleVO) {
  ElMessageBox.confirm(`确定将「${article.title}」移入回收站吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteArticle(article.id)
        ElMessage.success('已移入回收站')
        fetchList()
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : '删除失败'
        ElMessage.error(msg)
      }
    })
    .catch(() => {})
}

function handleRestore(article: ArticleVO) {
  ElMessageBox.confirm(`确定恢复「${article.title}」吗？`, '恢复确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  })
    .then(async () => {
      try {
        await restoreArticle(article.id)
        ElMessage.success('文章已恢复')
        fetchList()
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : '恢复失败'
        ElMessage.error(msg)
      }
    })
    .catch(() => {})
}

function handlePermanentDelete(article: ArticleVO) {
  ElMessageBox.confirm(
    `此操作将永久删除「${article.title}」，无法恢复！确定继续吗？`,
    '永久删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
    },
  )
    .then(async () => {
      try {
        await permanentDeleteArticle(article.id)
        ElMessage.success('文章已永久删除')
        fetchList()
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : '删除失败'
        ElMessage.error(msg)
      }
    })
    .catch(() => {})
}

function getStatusTagType(status: number): 'success' | 'info' | 'warning' | 'danger' | '' {
  if (status === ARTICLE_STATUS.PUBLISHED) return 'success'
  if (status === ARTICLE_STATUS.DRAFT) return 'info'
  if (status === ARTICLE_STATUS.ARCHIVED) return 'warning'
  return 'info'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="post-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="标题">
          <el-input
            v-model="queryForm.title"
            placeholder="模糊搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="statusSelect"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="handleStatusChange"
          >
            <el-option
              v-for="opt in statusOptions"
              :key="String(opt.value ?? 'all')"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleCreate">新建文章</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="articleList"
        stripe
        :border="true"
        style="width: 100%"
        :header-cell-style="{
          background: 'var(--el-bg-color-overlay, #fff)',
          color: 'var(--el-text-color-secondary, #999)',
        }"
        empty-text="暂无文章数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small" effect="plain">
              {{ articleStatusLabel[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <template v-if="row.tags && row.tags.length > 0">
              <el-tag
                v-for="tag in row.tags"
                :key="tag.id"
                :color="tag.color || '#409eff'"
                size="small"
                effect="dark"
                style="margin-right: 4px; margin-bottom: 2px; border: none"
              >
                {{ tag.name }}
              </el-tag>
            </template>
            <span v-else style="color: var(--el-text-color-placeholder, #c0c4cc)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.isTop" style="color: var(--el-color-warning, #e6a23c); font-size: 16px"
              >&#9733;</span
            >
            <span v-else style="color: var(--el-text-color-placeholder, #c0c4cc)">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
        <el-table-column label="发布时间" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="!queryForm.isRecycleBin">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
            <template v-else>
              <el-button type="success" link size="small" @click="handleRestore(row)">
                恢复
              </el-button>
              <el-button type="danger" link size="small" @click="handlePermanentDelete(row)">
                彻底删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.post-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.table-card {
  border: 1px solid var(--el-border-color-light, #e8e8e8);
  border-radius: 8px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.table-card :deep(.el-card__body) {
  padding: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
