import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

export default function OAuthCallback() {
  const setTokens = useAuthStore((s) => s.setTokens)
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const access = params.get('accessToken')
    const refresh = params.get('refreshToken')
    if (access && refresh) {
      setTokens(access, refresh)
      window.history.replaceState({}, '', '/')
      navigate('/', { replace: true })
    } else {
      navigate('/login', { replace: true })
    }
  }, [setTokens, navigate])

  return <div style={{ padding: 24 }}>Вход через Google...</div>
}
