import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useEffect } from 'react'
import { me } from '../api/auth'

const menu = [
  { to: '/drugs', label: 'Лекарства' },
  { to: '/sales', label: 'Продажи' },
  { to: '/orders', label: 'Заказы' },
  { to: '/categories', label: 'Категории' },
  { to: '/suppliers', label: 'Поставщики' },
]

export default function Layout() {
  const logout = useAuthStore((s) => s.logout)
  const accessToken = useAuthStore((s) => s.accessToken)
  const username = useAuthStore((s) => s.username)
  const roleName = useAuthStore((s) => s.roleName)
  const setUserInfo = useAuthStore((s) => s.setUserInfo)
  const navigate = useNavigate()

  const canViewReports = roleName === 'ADMIN' || roleName === 'PHARMACIST'

  useEffect(() => {
    if (!accessToken) return
    if (username && roleName) return

    me()
      .then((u) => setUserInfo(u.username, u.roleName))
      .catch(() => {
        // noop: token interceptor handles auth failures
      })
  }, [accessToken, roleName, setUserInfo, username])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <nav className="sidebar">
        <h2>PharmaApp</h2>
        <ul>
          {menu.map((item) => (
            <li key={item.to}>
              <NavLink to={item.to} className={({ isActive }) => (isActive ? 'active' : '')}>
                {item.label}
              </NavLink>
            </li>
          ))}
          {canViewReports && (
            <li>
              <NavLink to="/reports" className={({ isActive }) => (isActive ? 'active' : '')}>
                Отчёты
              </NavLink>
            </li>
          )}
        </ul>
        {username && roleName && <div className="top-user">{username} | {roleName}</div>}
        <button className="btn btn-secondary" onClick={handleLogout}>Выход</button>
      </nav>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
