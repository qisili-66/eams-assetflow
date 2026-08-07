import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login } from '../api/auth'
import {
  clearAccessToken,
  getAccessToken,
  saveAccessToken,
} from '../utils/token'
import type { CurrentUser, LoginRequest } from '../types/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getAccessToken())
  const user = ref<CurrentUser | null>(null)
  const initialized = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const roles = computed(() => user.value?.roles ?? [])
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const homePath = computed(() => (isAdmin.value ? '/dashboard' : '/assets'))

  function hasAnyRole(allowedRoles: string[]): boolean {
    return allowedRoles.some((role) => roles.value.includes(role))
  }

  async function loginByPassword(form: LoginRequest): Promise<void> {
    const response = await login(form)

    saveAccessToken(response.accessToken)
    token.value = response.accessToken
    user.value = response.user
    initialized.value = true
  }

  async function initialize(): Promise<void> {
    if (initialized.value) {
      return
    }

    if (!token.value) {
      initialized.value = true
      return
    }

    try {
      user.value = await getCurrentUser()
    } catch {
      logout()
    } finally {
      initialized.value = true
    }
  }

  function logout(): void {
    clearAccessToken()
    token.value = ''
    user.value = null
    initialized.value = true
  }

  return {
    token,
    user,
    initialized,
    isLoggedIn,
    isAdmin,
    homePath,
    hasAnyRole,
    loginByPassword,
    initialize,
    logout,
  }
})
