import axios from 'axios'

// Em produção (Vercel) defina VITE_API_URL=https://seu-backend.onrender.com/api
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 90000, // Render grátis pode levar ~1 min para "acordar"
})

// Coloca o token em toda requisição (se houver)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Token vencido/inválido: limpa a sessão e manda para o login
api.interceptors.response.use(
  (res) => res,
  (erro) => {
    const rota = erro.config?.url || ''
    if (erro.response?.status === 401 && !rota.includes('/auth/login')) {
      localStorage.removeItem('token')
      localStorage.removeItem('usuario')
      if (window.location.pathname !== '/login') window.location.href = '/login'
    }
    return Promise.reject(erro)
  },
)

/** Transforma qualquer erro do axios numa frase para mostrar na tela. */
export function mensagemDeErro(erro) {
  if (erro.response?.data?.erro) return erro.response.data.erro
  if (erro.code === 'ECONNABORTED') return 'O servidor demorou demais para responder. Tente de novo.'
  if (!erro.response) return 'Não foi possível conectar ao servidor.'
  return 'Algo deu errado. Tente de novo.'
}
