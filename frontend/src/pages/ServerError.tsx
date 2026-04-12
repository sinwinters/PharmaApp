import { useNavigate } from 'react-router-dom'

export default function ServerError() {
  const navigate = useNavigate()
  const retry = () => window.location.reload()

  return (
    <div style={{ display: 'grid', placeItems: 'center', minHeight: '70vh' }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 12, maxWidth: 520, textAlign: 'center' }}>
        <div style={{ fontSize: 64, marginBottom: 8 }}>☕🖥️</div>
        <h1>500</h1>
        <p>Our server is taking a coffee break ☕</p>
        <p style={{ color: '#667085' }}>Give it one sip and try again.</p>
        <div style={{ marginTop: 12, display: 'flex', gap: 8, justifyContent: 'center' }}>
          <button className="btn" onClick={retry}>Retry</button>
          <button className="btn btn-secondary" onClick={() => navigate('/')}>Go back to dashboard</button>
        </div>
      </div>
    </div>
  )
}
