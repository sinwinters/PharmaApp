import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

export default function Layout() {
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <nav style={{ width: 220, background: '#1a1a2e', color: '#eee', padding: 16 }}>
        <h2 style={{ margin: '0 0 24px 0', fontSize: 18 }}>Аптека</h2>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
          <li style={{ marginBottom: 8 }}><Link to="/drugs" style={{ color: '#eee' }}>Лекарства</Link></li>
          <li style={{ marginBottom: 8 }}><Link to="/sales" style={{ color: '#eee' }}>Продажи</Link></li>
          <li style={{ marginBottom: 8 }}><Link to="/orders" style={{ color: '#eee' }}>Заказы</Link></li>
          <li style={{ marginBottom: 8 }}><Link to="/categories" style={{ color: '#eee' }}>Категории</Link></li>
          <li style={{ marginBottom: 8 }}><Link to="/suppliers" style={{ color: '#eee' }}>Поставщики</Link></li>
        </ul>
        <button onClick={handleLogout} style={{ marginTop: 24, padding: '8px 16px', cursor: 'pointer' }}>Выход</button>
      </nav>
      <main style={{ flex: 1, padding: 24, overflow: 'auto' }}>
        <Outlet />
      </main>
    </div>
  )
}
