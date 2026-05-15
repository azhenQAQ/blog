import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type {
  LoginUserVO,
  UserLoginRequest,
  UserRegisterRequest,
  UserUpdateMyRequest,
  UserUpdatePasswordRequest,
} from '@/types/user'
import * as userApi from '@/api/modules/user'
import { isAdmin } from '@/enums/userRole'

const STORAGE_KEY = 'currentUser'

export const useUserStore = defineStore('user', () => {
  let initial: LoginUserVO | null = null
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) initial = JSON.parse(stored)
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
  const currentUser = ref<LoginUserVO | null>(initial)

  const isLogin = computed(() => currentUser.value !== null)
  const isAdminUser = computed(() => isAdmin(currentUser.value?.role))

  async function login(data: UserLoginRequest) {
    const user = await userApi.userLogin(data)
    currentUser.value = user
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  }

  async function register(data: UserRegisterRequest): Promise<number> {
    return userApi.userRegister(data)
  }

  async function logout() {
    await userApi.userLogout()
    currentUser.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  function clearLoginState() {
    currentUser.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  async function fetchCurrentUser() {
    try {
      currentUser.value = await userApi.getCurrentUser()
      localStorage.setItem(STORAGE_KEY, JSON.stringify(currentUser.value))
    } catch {
      currentUser.value = null
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  async function updateProfile(data: UserUpdateMyRequest): Promise<boolean> {
    const result = await userApi.updateMyUser(data)
    if (result) {
      await fetchCurrentUser()
    }
    return result
  }

  async function updatePassword(data: UserUpdatePasswordRequest): Promise<boolean> {
    return userApi.updatePassword(data)
  }

  return {
    currentUser,
    isLogin,
    isAdminUser,
    login,
    register,
    logout,
    clearLoginState,
    fetchCurrentUser,
    updateProfile,
    updatePassword,
  }
})
