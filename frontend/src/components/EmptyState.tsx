export default function EmptyState({ icon, message, action }: { icon: string; message: string; action?: JSX.Element }) {
  return (
    <div className="empty-state card">
      <div className="icon">{icon}</div>
      <p>{message}</p>
      {action}
    </div>
  )
}
