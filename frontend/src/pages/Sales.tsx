import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { salesList, createSale, type SaleItemRequest } from '../api/sales'
import { drugsApi } from '../api/drugs'

export default function Sales() {
  const [page, setPage] = useState(0)
  const [cart, setCart] = useState<SaleItemRequest[]>([])
  const [drugId, setDrugId] = useState<number | ''>('')
  const [qty, setQty] = useState(1)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['sales', page],
    queryFn: () => salesList(page, 10),
  })

  const { data: drugsData } = useQuery({
    queryKey: ['drugs-short'],
    queryFn: () => drugsApi({ page: 0, size: 500 }),
  })

  const createMu = useMutation({
    mutationFn: createSale,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sales'] })
      queryClient.invalidateQueries({ queryKey: ['drugs'] })
      setCart([])
    },
  })

  const addToCart = () => {
    if (drugId === '' || qty < 1) return
    setCart((c) => {
      const existing = c.find((x) => x.drugId === drugId)
      if (existing) {
        return c.map((x) => x.drugId === drugId ? { ...x, quantity: x.quantity + qty } : x)
      }
      return [...c, { drugId: drugId as number, quantity: qty }]
    })
  }

  const removeFromCart = (drugId: number) => setCart((c) => c.filter((x) => x.drugId !== drugId))

  const drugName = (id: number) => drugsData?.content.find((d) => d.id === id)?.name ?? id

  return (
    <div>
      <h1>Продажи</h1>

      <div style={{ background: '#fff', padding: 16, marginBottom: 24, borderRadius: 8 }}>
        <h3>Новая продажа</h3>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <select value={drugId} onChange={(e) => setDrugId(e.target.value === '' ? '' : Number(e.target.value))} style={{ padding: 8, minWidth: 200 }}>
            <option value="">Выберите лекарство</option>
            {drugsData?.content.map((d) => (
              <option key={d.id} value={d.id}>{d.name} (остаток: {d.stockQuantity})</option>
            ))}
          </select>
          <input type="number" min={1} value={qty} onChange={(e) => setQty(Number(e.target.value))} style={{ width: 60, padding: 8 }} />
          <button onClick={addToCart} style={{ padding: '8px 16px' }}>Добавить</button>
        </div>
        {cart.length > 0 && (
          <div style={{ marginTop: 16 }}>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {cart.map((item) => (
                <li key={item.drugId} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                  <span>{drugName(item.drugId)} × {item.quantity}</span>
                  <button onClick={() => removeFromCart(item.drugId)}>Удалить</button>
                </li>
              ))}
            </ul>
            <button onClick={() => createMu.mutate(cart)} disabled={createMu.isPending} style={{ marginTop: 8 }}>Провести продажу</button>
          </div>
        )}
      </div>

      <h3>История продаж</h3>
      {isLoading ? <p>Загрузка...</p> : (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee' }}>
                <th style={{ textAlign: 'left', padding: 12 }}>ID</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Дата</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Кассир</th>
                <th style={{ textAlign: 'right', padding: 12 }}>Сумма</th>
              </tr>
            </thead>
            <tbody>
              {data?.content.map((s) => (
                <tr key={s.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: 12 }}>{s.id}</td>
                  <td style={{ padding: 12 }}>{new Date(s.createdAt).toLocaleString()}</td>
                  <td style={{ padding: 12 }}>{s.username}</td>
                  <td style={{ padding: 12, textAlign: 'right' }}>{s.totalAmount}</td>
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
