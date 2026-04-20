import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { suppliersList } from '../api/suppliers'
import { getApiErrorMessage } from '../api/client'

export default function Suppliers() {
  const [page, setPage] = useState(0)

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: ['suppliers-page', page],
    queryFn: () => suppliersList(page, 10),
  })

  return (
    <div>
      <h1 className="page-header">Поставщики</h1>

      {isFetching && !isLoading && <p>Обновляем список...</p>}

      {isLoading ? (
        <p>Загрузка...</p>
      ) : isError ? (
        <p className="error-text">{getApiErrorMessage(error, 'Не удалось загрузить поставщиков.')}</p>
      ) : !data || data.content.length === 0 ? (
        <p>Поставщики не найдены.</p>
      ) : (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Название</th>
                  <th>УНП</th>
                  <th>GLN</th>
                  <th>Адрес</th>
                  <th>Контакты</th>
                  <th>Email</th>
                </tr>
              </thead>

              <tbody>
                {data.content.map((s) => (
                  <tr key={s.id}>
                    <td>{s.id}</td>
                    <td>{s.name}</td>
                    <td>{s.unp ?? '—'}</td>
                    <td>{s.gln ?? '—'}</td>
                    <td>{s.address ?? '—'}</td>
                    <td>{s.contactInfo ?? '—'}</td>
                    <td>{s.email ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {data.totalPages > 1 && (
            <div className="pagination">
              <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
                Назад
              </button>

              <span>Стр. {page + 1} из {data.totalPages}</span>

              <button className="btn btn-secondary" disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>
                Вперёд
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
