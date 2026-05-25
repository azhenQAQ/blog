<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listVisitRecordByPage } from '@/api/modules/visitRecord'
import type { VisitRecordVO, VisitRecordQueryRequest } from '@/types/visitRecord'

defineOptions({ name: 'VisitRecordManagePage' })

const loading = ref(false)
const visitList = ref<VisitRecordVO[]>([])
const total = ref(0)

const queryForm = reactive<VisitRecordQueryRequest>({
  current: 1,
  pageSize: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const page = await listVisitRecordByPage({ ...queryForm })
    visitList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取访问记录失败，${msg}`)
    visitList.value = []
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
  queryForm.path = undefined
  queryForm.ip = undefined
  queryForm.referer = undefined
  queryForm.current = 1
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

function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function truncate(str: string | null | undefined, max = 40): string {
  if (!str) return '—'
  return str.length > max ? str.slice(0, max) + '...' : str
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="visit-record-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="路径">
          <el-input
            v-model="queryForm.path"
            placeholder="路径搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="IP">
          <el-input
            v-model="queryForm.ip"
            placeholder="IP搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="来源">
          <el-input
            v-model="queryForm.referer"
            placeholder="来源搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="visitList"
        stripe
        :border="true"
        style="width: 100%"
        :header-cell-style="{
          background: 'var(--el-bg-color-overlay, #fff)',
          color: 'var(--el-text-color-secondary, #999)',
        }"
        empty-text="暂无访问记录"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="路径" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.path" placement="top">
              <span>{{ truncate(row.path, 50) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column label="User-Agent" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.userAgent ?? '—'" placement="top">
              <span>{{ truncate(row.userAgent, 40) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="来源" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.referer ?? '—'" placement="top">
              <span>{{ truncate(row.referer, 30) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="关联文章" width="90" align="center">
          <template #default="{ row }">
            {{ row.articleId ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column label="访问时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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
.visit-record-manage {
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
