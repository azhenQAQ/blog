<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listUserByPage } from '@/api/modules/user'
import { isAdmin } from '@/enums/userRole'
import type { UserVO, UserQueryRequest } from '@/types/user'

const loading = ref(false)
const userList = ref<UserVO[]>([])
const total = ref(0)

const queryForm = reactive<UserQueryRequest>({
  username: '',
  nickname: '',
  role: '',
  current: 1,
  pageSize: 10,
})

async function fetchUsers() {
  loading.value = true
  try {
    const page = await listUserByPage({ ...queryForm })
    userList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取用户列表失败，${msg}`)
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchUsers()
}

function handleReset() {
  queryForm.username = ''
  queryForm.nickname = ''
  queryForm.role = ''
  queryForm.current = 1
  fetchUsers()
}

function handlePageChange(page: number) {
  queryForm.current = page
  fetchUsers()
}

function handleSizeChange(size: number) {
  queryForm.pageSize = size
  queryForm.current = 1
  fetchUsers()
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
  fetchUsers()
})
</script>

<template>
  <div class="user-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            placeholder="模糊搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="queryForm.nickname"
            placeholder="模糊搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
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
        :data="userList"
        stripe
        :border="true"
        style="width: 100%"
        :header-cell-style="{
          background: 'var(--el-bg-color-overlay, #fff)',
          color: 'var(--el-text-color-secondary, #999)',
        }"
        empty-text="暂无用户数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="isAdmin(row.role) ? 'warning' : 'info'" size="small" effect="plain">
              {{ isAdmin(row.role) ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="userProfile"
          label="个人简介"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="注册时间" width="160" align="center">
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
.user-manage {
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
