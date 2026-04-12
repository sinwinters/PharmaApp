export default function Reports() {
  return (
    <div>
      <h1>Отчёты</h1>
      <p>Раздел отчётности доступен для ролей ADMIN и PHARMACIST.</p>
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginTop: 16 }}>
        <button>Отчёт по продажам</button>
        <button>Отчёт по заказам</button>
        <button>Отчёт по остаткам</button>
        <button>Отчёт по списаниям</button>
      </div>
    </div>
  )
}
