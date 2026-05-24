import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/api/user'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref(localStorage.getItem('adminToken') || '')

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('adminToken', newToken)
  }

  const setUser = (newUser: User) => {
    user.value = newUser
  }

  const login = async (username: string, password: string) => {
    const res = await userApi.login({ username, password })
    setToken(res.data.token)
    setUser(res.data.user)
    return res
  }

  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('adminToken')
  }

  const isLoggedIn = () => !!token.value

  return {
    user,
    token,
    setToken,
    setUser,
    login,
    logout,
    isLoggedIn,
  }
})
