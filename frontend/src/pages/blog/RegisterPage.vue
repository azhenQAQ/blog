<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLogin) {
    router.replace('/admin/dashboard')
  }
})

const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  checkPassword: '',
})

const validateCheckPassword = (_rule: any, value: string, callback: (e?: Error) => void) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, message: '用户名至少4个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8个字符', trigger: 'blur' },
  ],
  checkPassword: [
    { validator: validateCheckPassword, trigger: 'blur' },
  ],
}

const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

async function handleRegister() {
  errorMsg.value = ''
  successMsg.value = ''
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.register({
      username: form.username.trim(),
      password: form.password,
      checkPassword: form.checkPassword,
    })
    successMsg.value = '注册成功，即将跳转到登录页...'
  } catch (e: unknown) {
    errorMsg.value = e instanceof Error ? e.message : '注册失败'
  } finally {
    loading.value = false
  }

  if (successMsg.value) {
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="never">
      <h2 class="auth-title">注册</h2>

      <el-alert
        v-if="errorMsg"
        type="error"
        :title="errorMsg"
        show-icon
        closable
        @close="errorMsg = ''"
      />
      <el-alert
        v-if="successMsg"
        type="success"
        :title="successMsg"
        show-icon
        :closable="false"
      />

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名（至少4个字符）"
            :disabled="!!successMsg"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少8个字符）"
            show-password
            :disabled="!!successMsg"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPassword">
          <el-input
            v-model="form.checkPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            :disabled="!!successMsg"
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!!successMsg"
            class="auth-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <p class="auth-footer">
        已有账号？<router-link to="/login">去登录</router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
  padding: 24px;
}

.auth-card {
  width: 420px;
  max-width: 100%;
}

.auth-title {
  text-align: center;
  font-size: 1.4em;
  color: var(--el-text-color-primary);
  margin: 0 0 20px;
}

.auth-btn {
  width: 100%;
}

.auth-footer {
  text-align: center;
  font-size: 0.9em;
  color: var(--el-text-color-secondary);
  margin: 16px 0 0;
}

.auth-footer a {
  color: var(--el-color-primary);
  text-decoration: none;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
