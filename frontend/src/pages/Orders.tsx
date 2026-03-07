import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ordersList, createOrder } from '../api/orders'
import { drugsApi } from '../api/drugs'
import { suppliersList } from '../api/suppliers'

export default function Orders() {
  const [page, setPage] = useState(0)
  const [supplierId, setSupplierId] = useState<number | ''>('')
  const [items, setItems] = useState<{ drugId: number; quantity: number }[]>([])
  const [drugId, setDrugId] = useState<number | ''>('')
  const [qty, setQty] = useState(1)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['orders', page],
    queryFn: () => ordersList(page, 10),
  })

  const { data: drugsData } = useQuery({ queryKey: ['drugs-order'], queryFn: () => drugsApi({ page: 0, size: 500 }) })
  const { data: suppliers } = useQuery({ queryKey: ['suppliers'], queryFn: () => suppliersList(0, 200) })

  const createMu = useMutation({
    mutationFn: ({ supplierId, items }: { supplierId: number; items: { drugId: number; quantity: number }[] }) =>
      createOrder(supplierId, items),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      setSupplierId('')
      setItems([])
    },
  })

  const addItem = () => {
    if (drugId === '' || qty < 1) return
    setItems((prev) => [...prev, { drugId: drugId as number, quantity: qty }])
  }

  const drugName = (id: number) => drugsData?.content.find((d) => d.id === id)?.name ?? id

  return (
    <div>
      <h1>Заказы поставщикам</h1>

      <div style={{ background: '#fff', padding: 16, marginBottom: 24, borderRadius: 8 }}>
        <h3>Новый заказ</h3>
        <div style={{ marginBottom: 12 }}>
          <label>Поставщик </label>
          <select value={supplierId} onChange={(e) => setSupplierId(e.target.value === '' ? '' : Number(e.target.value))} style={{ padding: 8, minWidth: 200 }}>
            <option value="">Выберите поставщика</option>
            {suppliers?.content.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap', marginBottom: 12 }}>
          <select value={drugId} onChange={(e) => setDrugId(e.target.value === '' ? '' : Number(e.target.value))} style={{ padding: 8, minWidth: 200 }}>
            <option value="">Лекарство</option>
            {drugsData?.content.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
          <input type="number" min={1} value={qty} onChange={(e) => setQty(Number(e.target.value))} style={{ width: 60, padding: 8 }} />
          <button onClick={addItem} style={{ padding: '8px 16px' }}>Добавить</button>
        </div>
        {items.length > 0 && (
          <div style={{ marginTop: 12 }}>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {items.map((item, i) => (
                <li key={i}>{drugName(item.drugId)} × {item.quantity}</li>
              ))}
            </ul>
            <button
              onClick={() => supplierId !== '' && createMu.mutate({ supplierId: supplierId as number, items })}
              disabled={createMu.isPending || supplierId === ''}
              style={{ marginTop: 8 }}
            >
              Создать заказ
            </button>
          </div>
        )}
      </div>

      <h3>Список заказов</h3>
      {isLoading ? <p>Загрузка...</p> : (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee' }}>
                <th style={{ textAlign: 'left', padding: 12 }}>ID</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Поставщик</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Статус</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Дата</th>
              </tr>
            </thead>
            <tbody>
              {data?.content.map((o) => (
                <tr key={o.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: 12 }}>{o.id}</td>
                  <td style={{ padding: 12 }}>{o.supplierName}</td>
                  <td style={{ padding: 12 }}>{o.status}</td>
                  <td style={{ padding: 12 }}>{new Date(o.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {data && data.totalPages > 1 && (
            <div style={{ marginTop: 16 }}>
              <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Назад</button>
              <span style={{ margin: '0 16px' }}>Стр. {page + 1} из {data.totalPages}</span>
              <button disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Вперёд</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
