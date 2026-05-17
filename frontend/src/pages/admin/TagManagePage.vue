<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTagByPage, addTag, updateTag, deleteTag } from '@/api/modules/tag'
import type { TagVO, TagQueryRequest } from '@/types/tag'

const loading = ref(false)
const tagList = ref<TagVO[]>([])
const total = ref(0)

const queryForm = reactive<TagQueryRequest>({
  keyword: '',
  current: 1,
  pageSize: 10,
})

// 弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('新建标签')
const dialogLoading = ref(false)
const editingTag = reactive({
  id: 0,
  name: '',
  color: '#409eff',
})

async function fetchList() {
  loading.value = true
  try {
    const page = await listTagByPage({ ...queryForm })
    tagList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取标签列表失败，${msg}`)
    tagList.value = []
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
  queryForm.keyword = ''
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

function openCreateDialog() {
  dialogTitle.value = '新建标签'
  editingTag.id = 0
  editingTag.name = ''
  editingTag.color = '#409eff'
  dialogVisible.value = true
}

function openEditDialog(tag: TagVO) {
  dialogTitle.value = '编辑标签'
  editingTag.id = tag.id
  editingTag.name = tag.name
  editingTag.color = tag.color || '#409eff'
  dialogVisible.value = true
}

async function handleSave() {
  if (!editingTag.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  if (editingTag.name.length > 50) {
    ElMessage.warning('标签名称不能超过50个字符')
    return
  }
  dialogLoading.value = true
  try {
    if (editingTag.id) {
      await updateTag({
        id: editingTag.id,
        name: editingTag.name,
        color: editingTag.color,
      })
      ElMessage.success('标签已更新')
    } else {
      await addTag({
        name: editingTag.name,
        color: editingTag.color,
      })
      ElMessage.success('标签已创建')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '保存失败'
    ElMessage.error(msg)
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(tag: TagVO) {
  try {
    await deleteTag(tag.id, false)
    ElMessage.success('标签已删除')
    fetchList()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : ''
    // 如果有关联文章，弹出确认框
    if (msg.includes('关联')) {
      try {
        await ElMessageBox.confirm(
          `该标签关联了文章，移除关联后删除，确定继续吗？`,
          '确认删除',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
          },
        )
        await deleteTag(tag.id, true)
        ElMessage.success('标签已删除')
        fetchList()
      } catch {
        // 用户取消
      }
    } else {
      ElMessage.error(msg || '删除失败')
    }
  }
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
  <div class="tag-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.keyword"
            placeholder="标签名模糊搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openCreateDialog">新建标签</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tagList"
        stripe
        :border="true"
        style="width: 100%"
        :header-cell-style="{
          background: 'var(--el-bg-color-overlay, #fff)',
          color: 'var(--el-text-color-secondary, #999)',
        }"
        empty-text="暂无标签数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column label="颜色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :color="row.color || '#409eff'" size="small" effect="dark" style="border: none">
              {{ row.color || '默认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="关联文章" width="100" align="center" />
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
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

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px" @close="dialogVisible = false">
      <el-form label-width="60px">
        <el-form-item label="名称" required>
          <el-input
            v-model="editingTag.name"
            placeholder="标签名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="editingTag.color" show-alpha />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleSave"> 保存 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.tag-manage {
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
