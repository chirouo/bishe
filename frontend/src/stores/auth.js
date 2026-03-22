import { defineStore } from 'pinia'
import { fetchCurrentUser, login } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isTeacher: (state) => state.userInfo?.role === 'TEACHER'
  },
  actions: {
    async login(form) {
      const result = await login(form)
      this.token = result.data.token
      this.userInfo = result.data.userInfo
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return this.userInfo
    },
    async refreshUser() {
      if (!this.token) {
        return null
      }
      const result = await fetchCurrentUser()
      this.userInfo = result.data
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return this.userInfo
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})

