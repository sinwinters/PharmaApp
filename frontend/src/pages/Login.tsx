import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useAuthStore } from '../store/authStore'
import { login } from '../api/auth'

// Для OAuth редирект должен идти на тот же origin (Vite проксирует /oauth2 на backend)
const OAUTH_URL = '/oauth2/authorization/google'

type Form = { username: string; password: string }

export default function Login() {
  const setTokens = useAuthStore((s) => s.setTokens)
  const navigate = useNavigate()
  const [error, setError] = useState('')

  const { register, handleSubmit } = useForm<Form>()

  const onSubmit = async (data: Form) => {
    setError('')
    try {
      const res = await login(data)
      setTokens(res.accessToken, res.refreshToken)
      navigate('/')
    } catch {
      setError('Неверный логин или пароль')
    }
  }

  return (
    <div style={{ maxWidth: 400, margin: '80px auto', padding: 24, background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}>
      <h1 style={{ marginTop: 0 }}>Вход</h1>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', marginBottom: 4 }}>Логин</label>
          <input {...register('username', { required: true })} style={{ width: '100%', padding: 8 }} />
        </div>
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', marginBottom: 4 }}>Пароль</label>
          <input type="password" {...register('password', { required: true })} style={{ width: '100%', padding: 8 }} />
        </div>
        {error && <p style={{ color: 'crimson', marginBottom: 16 }}>{error}</p>}
        <button type="submit" style={{ padding: '10px 24px', cursor: 'pointer' }}>Войти</button>
      </form>
      <p style={{ marginTop: 16, fontSize: 14 }}>
        <a href={OAUTH_URL}>Войти через Google</a>
      </p>
      <p style={{ marginTop: 8, fontSize: 12, color: '#666' }}>Демо: admin / password</p>
    </div>
  )
}
