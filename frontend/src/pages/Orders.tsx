import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ordersList, createOrder } from '../api/orders'
import { drugsApi } from '../api/drugs'
import { suppliersList } from '../api/suppliers'
import { getApiErrorMessage } from '../api/client'
import { useToastStore } from '../store/toastStore'

export default function Orders() {
  const [page, setPage] = useState(0)
  const [supplierId, setSupplierId] = useState<number | ''>('')
  const [items, setItems] = useState<{ drugId: number; quantity: number }[]>([])
  const [drugId, setDrugId] = useState<number | ''>('')
  const [qty, setQty] = useState(1)
  const [actionError, setActionError] = useState<string | null>(null)

  const queryClient = useQueryClient()
  const pushToast = useToastStore((s) => s.push)

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: ['orders', page],
    queryFn: () => ordersList(page, 10),
  })

  const { data: drugsData } = useQuery({
    queryKey: ['drugs-order'],
    queryFn: () => drugsApi({ page: 0, size: 500 }),
  })

  const { data: suppliers } = useQuery({
    queryKey: ['suppliers'],
    queryFn: () => suppliersList(0, 200),
  })

  const createMu = useMutation({
    mutationFn: ({ supplierId, items }: { supplierId: number; items: { drugId: number; quantity: number }[] }) =>
      createOrder(supplierId, items),

    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      setSupplierId('')
      setItems([])
      pushToast('Заказ успешно создан')
    },

    onError: (e) => {
      setActionError(getApiErrorMessage(e, 'Не удалось создать заказ.'))
    },
  })

  const addItem = () => {
    if (drugId === '' || qty < 1) return

    setItems((prev) => [...prev, { drugId: drugId as number, quantity: qty }])
  }

  const drugName = (id: number) => drugsData?.content.find((d) => d.id === id)?.name ?? id

  const handleCreateOrder = () => {
    if (supplierId === '' || items.length === 0) return

    createMu.mutate({
      supplierId: supplierId as number,
      items,
    })
  }

  return (
    <div>
      <h1 className="page-header">Заказы поставщикам</h1>

      {actionError && <p className="error-text">{actionError}</p>}
      {isFetching && !isLoading && <p>Обновляем список...</p>}

      <div className="card" style={{ marginBottom: 24 }}>
        <h3>Новый заказ</h3>

        <div style={{ marginBottom: 12 }}>
          <label>Поставщик </label>
          <select
            className="select"
            value={supplierId}
            onChange={(e) => setSupplierId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">Выберите поставщика</option>
            {suppliers?.content.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>

        <div className="page-actions">
          <select
            className="select"
            value={drugId}
            onChange={(e) => setDrugId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">Лекарство</option>
            {drugsData?.content.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>

          <input
            className="input"
            type="number"
            min={1}
            value={qty}
            onChange={(e) => setQty(Number(e.target.value))}
            style={{ width: 70 }}
          />

          <button className="btn" onClick={addItem}>Добавить</button>
        </div>

        {items.length > 0 && (
          <div style={{ marginTop: 12 }}>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {items.map((item, i) => (
                <li key={i}>
                  {drugName(item.drugId)} × {item.quantity}
                </li>
              ))}
            </ul>

            <button
              className="btn"
              onClick={handleCreateOrder}
              disabled={createMu.isPending || supplierId === '' || items.length === 0}
            >
              Создать заказ
            </button>
          </div>
        )}
      </div>

      <h3>Список заказов</h3>

      {isLoading ? (
        <p>Загрузка...</p>
      ) : isError ? (
        <p className="error-text">{getApiErrorMessage(error, 'Не удалось загрузить список заказов.')}</p>
      ) : (
        <>
          {data?.content.length === 0 ? (
            <p>Пока нет заказов поставщикам.</p>
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Поставщик</th>
                    <th>Статус</th>
                    <th>Дата</th>
                  </tr>
                </thead>

                <tbody>
                  {data?.content.map((o) => (
                    <tr key={o.id}>
                      <td>{o.id}</td>
                      <td>{o.supplierName}</td>
                      <td>{o.status}</td>
                      <td>{new Date(o.createdAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {data && data.totalPages > 1 && (
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
