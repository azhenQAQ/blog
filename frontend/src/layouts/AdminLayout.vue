<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updatePassword } from '@/api/modules/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = ref(route.path)

watch(
  () => route.path,
  (path) => {
    activeMenu.value = path
  },
)

function navigateTo(path: string) {
  router.push(path)
}

const avatarSrc = computed(() => userStore.currentUser?.avatar || '/images/default-avatar.png')

function onAvatarError(e: Event) {
  ;(e.target as HTMLImageElement).src = '/images/default-avatar.png'
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

// 修改密码
const passwordVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordLoading = ref(false)

function openPasswordDialog() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = true
}

async function handleUpdatePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordVisible.value = false
    userStore.logout()
    router.push('/login')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '密码修改失败'
    ElMessage.error(msg)
  } finally {
    passwordLoading.value = false
  }
}
</script>

<template>
  <div class="admin-layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '200px'" class="admin-aside">
        <div class="admin-logo">
          <span v-if="!isCollapse" class="admin-logo-text">管理后台</span>
          <span v-else class="admin-logo-icon">⚙</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :router="false"
          text-color="var(--el-text-color-secondary, #909399)"
          active-text-color="var(--el-color-primary, #409eff)"
          class="admin-menu"
          @select="navigateTo"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon
              ><svg viewBox="0 0 24 24" width="1em" height="1em">
                <rect x="3" y="3" width="7" height="7" rx="1" fill="currentColor" />
                <rect x="14" y="3" width="7" height="7" rx="1" fill="currentColor" />
                <rect x="3" y="14" width="7" height="7" rx="1" fill="currentColor" />
                <rect x="14" y="14" width="7" height="7" rx="1" fill="currentColor" /></svg
            ></el-icon>
            <template #title>控制台</template>
          </el-menu-item>

          <el-menu-item index="/admin/userManage">
            <el-icon
              ><svg viewBox="0 0 24 24" width="1em" height="1em">
                <circle cx="12" cy="8" r="4" fill="currentColor" />
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" fill="currentColor" /></svg
            ></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/posts">
            <el-icon
              ><svg viewBox="0 0 24 24" width="1em" height="1em">
                <rect
                  x="3"
                  y="3"
                  width="18"
                  height="18"
                  rx="2"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <path d="M3 9h18" stroke="currentColor" stroke-width="2" />
                <path d="M9 3v18" stroke="currentColor" stroke-width="2" /></svg
            ></el-icon>
            <template #title>文章管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/tags">
            <el-icon
              ><svg viewBox="0 0 24 24" width="1em" height="1em">
                <path
                  d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <line x1="7" y1="7" x2="7.01" y2="7" stroke="currentColor" stroke-width="2" /></svg
            ></el-icon>
            <template #title>标签管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/comments">
            <el-icon
              ><svg viewBox="0 0 24 24" width="1em" height="1em">
                <path
                  d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
                  fill="currentColor"
                /></svg
            ></el-icon>
            <template #title>评论管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/attachments">
            <el-icon
              ><svg viewBox="0 0 24 24" width="1em" height="1em">
                <path
                  d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <polyline
                  points="14 2 14 8 20 8"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <line x1="12" y1="18" x2="12" y2="12" stroke="currentColor" stroke-width="2" />
                <line x1="9" y1="15" x2="15" y2="15" stroke="currentColor" stroke-width="2" /></svg
            ></el-icon>
            <template #title>附件库</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container class="admin-content-wrap">
        <el-header class="admin-header">
          <div class="admin-header-left">
            <span class="admin-header-title">QAQ</span>
          </div>

          <div class="admin-header-right">
            <el-dropdown trigger="click">
              <span class="user-trigger">
                <img :src="avatarSrc" alt="" class="user-avatar" @error="onAvatarError" />
                <span class="user-name">{{ userStore.currentUser?.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openPasswordDialog">修改密码</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>

        <!-- 修改密码弹窗 -->
        <el-dialog
          v-model="passwordVisible"
          title="修改密码"
          width="420px"
          :close-on-click-modal="false"
        >
          <el-form :model="passwordForm" label-width="80px" @keyup.enter="handleUpdatePassword">
            <el-form-item label="旧密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="passwordVisible = false">取消</el-button>
            <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">
              确认修改
            </el-button>
          </template>
        </el-dialog>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--el-bg-color, #f0f2f5);
  color: var(--el-text-color-primary, #303133);
}

.admin-aside {
  background: var(--el-bg-color, #1d1e2c);
  transition: width 0.3s;
  overflow: hidden;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  border-right: 1px solid gainsboro;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-logo-text {
  font-size: 1.1em;
  font-weight: 600;
  color: var(--el-color-primary, #409eff);
  letter-spacing: 2px;
}

.admin-logo-icon {
  font-size: 1.4em;
}

.admin-menu {
  border-right: none;
  margin-top: 8px;
}

.admin-content-wrap {
  margin-left: v-bind("isCollapse ? '64px' : '200px'");
  transition: margin-left 0.3s;
}

.admin-main {
  padding: 24px;
  height: 100vh - 56px;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--el-bg-color-overlay, #fff);
  border-bottom: 1px solid var(--el-border-color-light, #e4e7ed);
  height: 56px;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.admin-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-header-title {
  font-weight: 600;
  color: var(--el-color-primary, #409eff);
}

.admin-header-right {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--el-text-color-primary, #303133);
  cursor: pointer;
  transition: background 0.2s;
  font-family: inherit;
  font-size: 0.95em;
  outline: none;
}

.user-trigger:hover {
  background: var(--el-color-primary-light-9, #ecf5ff);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
