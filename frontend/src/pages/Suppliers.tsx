import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { suppliersList } from '../api/suppliers'

export default function Suppliers() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useQuery({
    queryKey: ['suppliers-page', page],
    queryFn: () => suppliersList(page, 10),
  })

  return (
    <div>
      <h1>Поставщики</h1>
      {isLoading ? <p>Загрузка...</p> : (
        <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #eee' }}>
              <th style={{ textAlign: 'left', padding: 12 }}>ID</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Название</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Контакты</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Email</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((s) => (
              <tr key={s.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: 12 }}>{s.id}</td>
                <td style={{ padding: 12 }}>{s.name}</td>
                <td style={{ padding: 12 }}>{s.contactInfo ?? '—'}</td>
                <td style={{ padding: 12 }}>{s.email ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {data && data.totalPages > 1 && (
        <div style={{ marginTop: 16 }}>
          <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Назад</button>
          <span style={{ margin: '0 16px' }}>Стр. {page + 1} из {data.totalPages}</span>
          <button disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Вперёд</button>
        </div>
      )}
    </div>
  )
}
