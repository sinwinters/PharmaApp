import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { categoriesList } from '../api/categories'

export default function Categories() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useQuery({
    queryKey: ['categories', page],
    queryFn: () => categoriesList(page, 10),
  })

  return (
    <div>
      <h1>Категории</h1>
      {isLoading ? <p>Загрузка...</p> : (
        <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #eee' }}>
              <th style={{ textAlign: 'left', padding: 12 }}>ID</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Название</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Описание</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((c) => (
              <tr key={c.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: 12 }}>{c.id}</td>
                <td style={{ padding: 12 }}>{c.name}</td>
                <td style={{ padding: 12 }}>{c.description ?? '—'}</td>
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
