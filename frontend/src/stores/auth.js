import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/services/api'

function lerUsuarioSalvo() {
  try {
    return JSON.parse(localStorage.getItem('usuario'))
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const usuario = ref(lerUsuarioSalvo())

  const logado = computed(() => !!token.value)
  const isAdmin = computed(() => usuario.value?.perfil === 'ADMIN')

  async function login(email, senha) {
    const { data } = await api.post('/auth/login', { email, senha })
    token.value = data.token
    usuario.value = data.usuario
    localStorage.setItem('token', data.token)
    localStorage.setItem('usuario', JSON.stringify(data.usuario))
  }

  async function cadastrar(nome, email, senha) {
    await api.post('/auth/register', { nome, email, senha })
  }

  /** Atualiza os dados do usuário (ex.: ao abrir "Minha conta"). */
  async function recarregar() {
    const { data } = await api.get('/auth/me')
    usuario.value = data
    localStorage.setItem('usuario', JSON.stringify(data))
  }

  function logout() {
    token.value = null
    usuario.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('usuario')
  }

  return { token, usuario, logado, isAdmin, login, cadastrar, recarregar, logout }
})
