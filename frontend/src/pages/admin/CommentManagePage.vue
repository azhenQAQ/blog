<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listCommentByPage, deleteComment } from '@/api/modules/comment'
import type { CommentVO, CommentQueryRequest } from '@/types/comment'
import { COMMENT_TYPE } from '@/types/comment'

defineOptions({ name: 'CommentManagePage' })

const loading = ref(false)
const commentList = ref<CommentVO[]>([])
const total = ref(0)

const queryForm = reactive<CommentQueryRequest>({
  current: 1,
  pageSize: 10,
})

async function fetchList() {
  loading.value = true
  try {
    const page = await listCommentByPage({ ...queryForm })
    commentList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取评论列表失败，${msg}`)
    commentList.value = []
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
  queryForm.type = undefined
  queryForm.nickname = undefined
  queryForm.email = undefined
  queryForm.content = undefined
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

async function handleDelete(comment: CommentVO) {
  try {
    await deleteComment(comment.id)
    ElMessage.success('已删除')
    fetchList()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '删除失败'
    ElMessage.error(msg)
  }
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

function typeLabel(type: string): string {
  return type === COMMENT_TYPE.COMMENT ? '文章评论' : '留言板'
}

function truncateContent(content: string, max = 60): string {
  if (!content) return ''
  return content.length > max ? content.slice(0, max) + '...' : content
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="comment-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 130px">
            <el-option label="文章评论" value="comment" />
            <el-option label="留言板" value="guestbook" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="queryForm.nickname"
            placeholder="昵称搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
            v-model="queryForm.email"
            placeholder="邮箱搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="queryForm.content"
            placeholder="内容搜索"
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
        :data="commentList"
        stripe
        :border="true"
        style="width: 100%"
        :header-cell-style="{
          background: 'var(--el-bg-color-overlay, #fff)',
          color: 'var(--el-text-color-secondary, #999)',
        }"
        empty-text="暂无评论数据"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'comment' ? 'primary' : 'success'" size="small" effect="plain">
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="关联文章" width="80" align="center">
          <template #default="{ row }">
            {{ row.articleId ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="200">
          <template #default="{ row }">
            <span v-if="row.replyToName" style="color: var(--el-color-primary)">@{{ row.replyToName }} </span>
            {{ truncateContent(row.content) }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelete(row)"> 删除 </el-button>
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
.comment-manage {
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
