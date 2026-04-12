import { api } from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface CurrentUser {
  id: number
  username: string
  email: string | null
  roleName: string
  enabled: boolean
}

// Используем базовый URL из client.ts, поэтому не добавляем /api/v1 вручную
export function login(body: LoginRequest) {
  return api.post<TokenResponse>('/auth/login', body).then((r) => r.data)
}

export function me() {
  return api.get<CurrentUser>('/auth/me').then((r) => r.data)
}

// OAuth через Google
// Фронт будет проксировать /oauth2 на backend, учитываем baseURL
export const OAUTH_URL = '/api/v1/oauth2/authorization/google'
