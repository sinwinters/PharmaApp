import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { me } from '../api/auth'

export default function OAuthCallback() {
  const setTokens = useAuthStore((s) => s.setTokens)
  const setUserInfo = useAuthStore((s) => s.setUserInfo)
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const access = params.get('accessToken')
    const refresh = params.get('refreshToken')
    if (access && refresh) {
      setTokens(access, refresh)
      me()
        .then((currentUser) => setUserInfo(currentUser.username, currentUser.roleName))
        .finally(() => {
          window.history.replaceState({}, '', '/')
          navigate('/', { replace: true })
        })
    } else {
      navigate('/login', { replace: true })
    }
  }, [setTokens, setUserInfo, navigate])

  return <div style={{ padding: 24 }}>Вход через Google...</div>
}
