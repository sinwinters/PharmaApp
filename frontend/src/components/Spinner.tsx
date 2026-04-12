export default function Spinner({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="loading-row">
      <div className="spinner" />
      <span>{label}</span>
    </div>
  )
}
