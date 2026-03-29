import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const baseURL = import.meta.env.VITE_API_URL || '/api/v1'

function readAccessTokenFromStorage() {
  try {
    const raw = localStorage.getItem('pharma-auth')
    if (!raw) return null
    const parsed = JSON.parse(raw) as { state?: { accessToken?: string | null } }
    return parsed.state?.accessToken ?? null
  } catch {
    return null
  }
}

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken ?? readAccessTokenFromStorage()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (r) => r,
  async (err) => {
    const original = err.config
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true
      const refresh = useAuthStore.getState().refreshToken
      if (refresh) {
        try {
          const { data } = await axios.post<TokenResponse>(`${baseURL}/auth/refresh`, { refreshToken: refresh })
          useAuthStore.getState().setTokens(data.accessToken, data.refreshToken)
          original.headers.Authorization = `Bearer ${data.accessToken}`
          return api(original)
        } catch {
          useAuthStore.getState().logout()
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(err)
  }
)

interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}
