import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useAuthStore } from '../store/authStore'
import { login, OAUTH_URL } from '../api/auth'

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
    <div className="login-page">
      <div className="login-card">
        <h1>Вход в PharmaApp</h1>
        <p>Управление лекарствами, продажами и поставками в одном месте.</p>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label>Логин</label>
            <input {...register('username', { required: true })} placeholder="Введите логин" />
          </div>
          <div>
            <label>Пароль</label>
            <input type="password" {...register('password', { required: true })} placeholder="Введите пароль" />
          </div>
          {error && <p className="error-msg">{error}</p>}
          <button type="submit">Войти</button>
        </form>

        <p>
          <a href={OAUTH_URL}>Войти через Google</a>
        </p>
      </div>
    </div>
  )
}
