import { useNavigate } from 'react-router-dom'

export default function Forbidden() {
  const navigate = useNavigate()

  return (
    <div style={{ display: 'grid', placeItems: 'center', minHeight: '70vh' }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 12, maxWidth: 520, textAlign: 'center' }}>
        <div style={{ fontSize: 64, marginBottom: 8 }}>🚫💊</div>
        <h1>403</h1>
        <p>Доступ запрещён.</p>
        <p style={{ color: '#667085' }}>Запросите дополнительные права у администратора.</p>
        <button className="btn" onClick={() => navigate('/')} style={{ marginTop: 12 }}>Вернуться на главную</button>
      </div>
    </div>
  )
}
