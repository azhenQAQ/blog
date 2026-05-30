<script setup lang="ts">
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminTabsStore, resolveTabPath, resolveMenuPath, getMenuRoutes, findAdminRoute } from '@/stores/adminTabs'
import { updatePassword } from '@/api/modules/user'
import { ElMessage } from 'element-plus'
import {
  Grid,
  User,
  Document,
  PriceTag,
  ChatDotSquare,
  Folder,
  Headset,
  Monitor,
} from '@element-plus/icons-vue'
import TabBar from '@/components/admin/TabBar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const tabStore = useAdminTabsStore()

const isCollapse = ref(false)

const activeMenu = ref(route.path)

const iconMap: Record<string, unknown> = {
  Grid,
  User,
  Document,
  PriceTag,
  ChatDotSquare,
  Folder,
  Headset,
  Monitor,
}

const menuRoutes = computed(() => getMenuRoutes())

const cachedComponentNames = computed(() => {
  const names: string[] = []
  for (const tab of tabStore.tabs) {
    const route = findAdminRoute(tab.path)
    if (route?.meta?.keepAlive === true) {
      const name = route.meta?.componentName as string | undefined
      if (name && !names.includes(name)) names.push(name)
    }
  }
  return names
})

watch(
  () => route.path,
  (path) => {
    const tabPath = resolveTabPath(path)
    if (tabPath) {
      tabStore.openTab(tabPath)
      tabStore.setActiveTab(tabPath)
    }
    // 菜单高亮：解析到父级菜单路径
    activeMenu.value = resolveMenuPath(path) || path
  },
  { immediate: true },
)

function navigateTo(path: string) {
  tabStore.openTab(path)
  router.push(path)
}

const avatarSrc = computed(() => userStore.currentUser?.avatar || '/images/default-avatar.png')

function onAvatarError(e: Event) {
  ;(e.target as HTMLImageElement).src = '/images/default-avatar.png'
}

function handleLogout() {
  tabStore.reset()
  userStore.logout()
  router.push('/login')
}

// 浏览器刷新后重置所有 Tab 并回到控制台
onMounted(() => {
  tabStore.reset()
  router.replace('/admin/dashboard')
})

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
    tabStore.reset()
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
          <el-menu-item
            v-for="item in menuRoutes"
            :key="item.path"
            :index="'/admin/' + item.path"
          >
            <el-icon v-if="item.meta?.icon">
              <component :is="iconMap[(item.meta.icon as string)]" />
            </el-icon>
            <template #title>{{ item.meta?.menuTitle || item.name }}</template>
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

        <TabBar />

        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <keep-alive :include="cachedComponentNames">
              <component :is="Component" :key="$route.fullPath" />
            </keep-alive>
          </router-view>
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
