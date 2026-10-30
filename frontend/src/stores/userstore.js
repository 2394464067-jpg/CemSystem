import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    // ===== state =====
    const token = ref(localStorage.getItem('token') || '')
    const username = ref(localStorage.getItem('username') || '')
    const role = ref(localStorage.getItem('role') || '')
    const factoryName = ref(localStorage.getItem('factoryName') || '')

    // ===== getters =====
    const isAdmin = computed(() => role.value === 'ADMIN')
    const isLogin = computed(() => !!token.value)

    // ===== actions =====
    async function login(form) {
        const res = await loginApi(form)
        const { token: t, username: u, role: r, factoryName: f } = res.data

        token.value = t || ''
        username.value = u || ''
        role.value = r || ''
        factoryName.value = f || ''

        localStorage.setItem('token', t || '')
        localStorage.setItem('username', u || '')
        localStorage.setItem('role', r || '')
        localStorage.setItem('factoryName', f || '')

        return res.data
    }

    function register(form) {
        return registerApi(form)
    }

    // 更新部分用户信息（个人中心用）
    function setUserInfo(info = {}) {
        if (info.username !== undefined) {
            username.value = info.username || ''
            localStorage.setItem('username', info.username || '')
        }
        if (info.factoryName !== undefined) {
            factoryName.value = info.factoryName || ''
            localStorage.setItem('factoryName', info.factoryName || '')
        }
    }

    function logout() {
        token.value = ''
        username.value = ''
        role.value = ''
        factoryName.value = ''

        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        localStorage.removeItem('factoryName')
    }

    return {
        token, username, role, factoryName,
        isAdmin, isLogin,
        login, register, setUserInfo, logout
    }
})