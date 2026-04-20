import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { salesList, createSale, benefitsRbList, type SaleItemRequest } from '../api/sales'
import { drugsApi } from '../api/drugs'
import { getApiErrorMessage } from '../api/client'
import { useToastStore } from '../store/toastStore'

export default function Sales() {
  const [page, setPage] = useState(0)
  const [cart, setCart] = useState<SaleItemRequest[]>([])
  const [drugId, setDrugId] = useState<number | ''>('')
  const [qty, setQty] = useState(1)
  const [benefitCode, setBenefitCode] = useState('')
  const [prescriptionNumber, setPrescriptionNumber] = useState('')
  const [edsSignature, setEdsSignature] = useState('')
  const [error, setError] = useState('')
  const queryClient = useQueryClient()
  const pushToast = useToastStore((s) => s.push)

  const { data, isLoading } = useQuery({
    queryKey: ['sales', page],
    queryFn: () => salesList(page, 10),
  })

  const { data: drugsData } = useQuery({
    queryKey: ['drugs-short'],
    queryFn: () => drugsApi({ page: 0, size: 500 }),
  })

  const { data: benefits } = useQuery({
    queryKey: ['rb-benefits'],
    queryFn: benefitsRbList,
  })

  const createMu = useMutation({
    mutationFn: createSale,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sales'] })
      queryClient.invalidateQueries({ queryKey: ['drugs'] })
      setCart([])
      setBenefitCode('')
      setPrescriptionNumber('')
      setEdsSignature('')
      setError('')
      pushToast('Продажа успешно проведена')
    },
    onError: (e) => {
      setError(getApiErrorMessage(e, 'Не удалось провести продажу'))
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

  const removeFromCart = (targetDrugId: number) => setCart((c) => c.filter((x) => x.drugId !== targetDrugId))

  const selectedBenefit = benefits?.find((b) => b.code === benefitCode)

  const submitSale = () => {
    setError('')
    createMu.mutate({
      items: cart,
      benefitCode: benefitCode || undefined,
      prescriptionNumber: prescriptionNumber || undefined,
      edsSignature: edsSignature || undefined,
      edsProvider: prescriptionNumber || edsSignature ? 'AVEST' : undefined,
    })
  }

  const drugName = (id: number) => drugsData?.content.find((d) => d.id === id)?.name ?? id

  return (
    <div>
      <h1 className="page-header">Продажи</h1>

      <div className="card" style={{ marginBottom: 24 }}>
        <h3>Новая продажа</h3>
        <div className="page-actions">
          <select className="select" value={drugId} onChange={(e) => setDrugId(e.target.value === '' ? '' : Number(e.target.value))} style={{ minWidth: 240 }}>
            <option value="">Выберите лекарство</option>
            {drugsData?.content.map((d) => (
              <option key={d.id} value={d.id}>{d.name} (остаток: {d.stockQuantity})</option>
            ))}
          </select>
          <input className="input" type="number" min={1} value={qty} onChange={(e) => setQty(Number(e.target.value))} style={{ width: 70 }} />
          <button className="btn" onClick={addToCart}>Добавить</button>
        </div>

        <div className="page-actions" style={{ marginTop: 12 }}>
          <select className="select" value={benefitCode} onChange={(e) => setBenefitCode(e.target.value)} style={{ minWidth: 320 }}>
            <option value="">Без льготы</option>
            {benefits?.map((b) => (
              <option key={b.code} value={b.code}>{b.title} ({b.discountPercent}%)</option>
            ))}
          </select>
          <input className="input" placeholder="Электронный рецепт (номер)" value={prescriptionNumber} onChange={(e) => setPrescriptionNumber(e.target.value)} style={{ minWidth: 220 }} />
          <input className="input" placeholder="ЭЦП Avest (подпись)" value={edsSignature} onChange={(e) => setEdsSignature(e.target.value)} style={{ minWidth: 220 }} />
        </div>
        {selectedBenefit && <p style={{ fontSize: 13, color: '#475569' }}>{selectedBenefit.lawReference}</p>}

        {cart.length > 0 && (
          <div style={{ marginTop: 16 }}>
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {cart.map((item) => (
                <li key={item.drugId} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                  <span>{drugName(item.drugId)} × {item.quantity}</span>
                  <button className="btn btn-danger" onClick={() => removeFromCart(item.drugId)}>Удалить</button>
                </li>
              ))}
            </ul>
            {error && <p className="error-text">{error}</p>}
            <button className="btn" onClick={submitSale} disabled={createMu.isPending} style={{ marginTop: 8 }}>Провести продажу</button>
          </div>
        )}
      </div>

      <h3>История продаж</h3>
      {isLoading ? <p>Загрузка...</p> : (
        <>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Дата</th>
                  <th>Кассир</th>
                  <th>Льгота</th>
                  <th style={{ textAlign: 'right' }}>Сумма</th>
                </tr>
              </thead>
              <tbody>
                {data?.content.map((s) => (
                  <tr key={s.id}>
                    <td>{s.id}</td>
                    <td>{new Date(s.createdAt).toLocaleString()}</td>
                    <td>{s.username}</td>
                    <td>{s.benefitCode ?? '—'}</td>
                    <td style={{ textAlign: 'right' }}>{s.totalAmount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {data && data.totalPages > 1 && (
            <div className="pagination">
              <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Назад</button>
              <span>Стр. {page + 1} из {data.totalPages}</span>
              <button className="btn btn-secondary" disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Вперёд</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
