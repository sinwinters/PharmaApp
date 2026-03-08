import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

const menu = [
  { to: '/drugs', label: 'Лекарства' },
  { to: '/sales', label: 'Продажи' },
  { to: '/orders', label: 'Заказы' },
  { to: '/categories', label: 'Категории' },
  { to: '/suppliers', label: 'Поставщики' },
]

export default function Layout() {
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

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
        </ul>
        <button onClick={handleLogout}>Выход</button>
      </nav>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
