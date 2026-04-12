import { useNavigate } from 'react-router-dom'

export default function NotFound() {
  const navigate = useNavigate()

  return (
    <div style={{ display: 'grid', placeItems: 'center', minHeight: '70vh' }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 12, maxWidth: 520, textAlign: 'center' }}>
        <div style={{ fontSize: 64, marginBottom: 8 }}>🧪📦</div>
        <h1>404</h1>
        <p>This page got lost in the pharmacy storage 🧪</p>
        <p style={{ color: '#667085' }}>Looks like someone mislabeled the shelf.</p>
        <button className="btn" onClick={() => navigate('/')} style={{ marginTop: 12 }}>Go back to dashboard</button>
      </div>
    </div>
  )
}
