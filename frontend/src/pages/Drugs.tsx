import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { drugsApi, createDrug, updateDrug, deleteDrug, type DrugDto, type DrugCreateUpdate } from '../api/drugs'
import { categoriesList } from '../api/categories'
import { suppliersList } from '../api/suppliers'

export default function Drugs() {
  const [page, setPage] = useState(0)
  const [name, setName] = useState('')
  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [supplierId, setSupplierId] = useState<number | ''>('')
  const [editing, setEditing] = useState<DrugDto | null>(null)
  const [form, setForm] = useState<DrugCreateUpdate | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['drugs', page, name, categoryId, supplierId],
    queryFn: () => drugsApi({ page, size: 10, name: name || undefined, categoryId: categoryId || undefined, supplierId: supplierId || undefined }),
  })

  const { data: categories } = useQuery({ queryKey: ['categories'], queryFn: () => categoriesList(0, 200) })
  const { data: suppliers } = useQuery({ queryKey: ['suppliers'], queryFn: () => suppliersList(0, 200) })

  const createMu = useMutation({ mutationFn: createDrug, onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['drugs'] }); setForm(null) } })
  const updateMu = useMutation({ mutationFn: ({ id, body }: { id: number; body: DrugCreateUpdate }) => updateDrug(id, body), onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['drugs'] }); setEditing(null) } })
  const deleteMu = useMutation({ mutationFn: deleteDrug, onSuccess: () => queryClient.invalidateQueries({ queryKey: ['drugs'] }) })

  const openCreate = () => setForm({ name: '', categoryId: 0, supplierId: 0, minQuantity: 10, unit: 'шт', basePrice: 0 })
  const openEdit = (d: DrugDto) => {
    setEditing(d)
    setForm({ name: d.name, categoryId: d.categoryId, supplierId: d.supplierId, minQuantity: d.minQuantity, unit: d.unit, basePrice: d.basePrice })
  }

  return (
    <div>
      <h1>Лекарства</h1>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
        <input placeholder="Поиск по названию" value={name} onChange={(e) => setName(e.target.value)} style={{ padding: 8 }} />
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value === '' ? '' : Number(e.target.value))} style={{ padding: 8 }}>
          <option value="">Все категории</option>
          {categories?.content.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={supplierId} onChange={(e) => setSupplierId(e.target.value === '' ? '' : Number(e.target.value))} style={{ padding: 8 }}>
          <option value="">Все поставщики</option>
          {suppliers?.content.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        <button onClick={openCreate} style={{ padding: '8px 16px' }}>Добавить</button>
      </div>

      {form && (
        <div style={{ background: '#fff', padding: 16, marginBottom: 16, borderRadius: 8 }}>
          <h3>{editing ? 'Редактирование' : 'Новое лекарство'}</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, maxWidth: 500 }}>
            <input placeholder="Название" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <input type="number" placeholder="Мин. остаток" value={form.minQuantity} onChange={(e) => setForm({ ...form, minQuantity: Number(e.target.value) })} />
            <input placeholder="Ед." value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })} />
            <input type="number" step="0.01" placeholder="Цена" value={form.basePrice || ''} onChange={(e) => setForm({ ...form, basePrice: Number(e.target.value) })} />
            <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value) })}>
              <option value={0}>Категория</option>
              {categories?.content.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select value={form.supplierId} onChange={(e) => setForm({ ...form, supplierId: Number(e.target.value) })}>
              <option value={0}>Поставщик</option>
              {suppliers?.content.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div style={{ marginTop: 12 }}>
            <button onClick={() => editing ? updateMu.mutate({ id: editing.id, body: form }) : createMu.mutate(form)} disabled={createMu.isPending || updateMu.isPending}>Сохранить</button>
            <button onClick={() => { setForm(null); setEditing(null) }} style={{ marginLeft: 8 }}>Отмена</button>
          </div>
        </div>
      )}

      {isLoading ? <p>Загрузка...</p> : (
        <>
          <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee' }}>
                <th style={{ textAlign: 'left', padding: 12 }}>Название</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Категория</th>
                <th style={{ textAlign: 'left', padding: 12 }}>Поставщик</th>
                <th style={{ textAlign: 'right', padding: 12 }}>Остаток</th>
                <th style={{ textAlign: 'right', padding: 12 }}>Мин.</th>
                <th style={{ textAlign: 'right', padding: 12 }}>Цена</th>
                <th style={{ padding: 12 }}></th>
              </tr>
            </thead>
            <tbody>
              {data?.content.map((d) => (
                <tr key={d.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: 12 }}>{d.name}</td>
                  <td style={{ padding: 12 }}>{d.categoryName}</td>
                  <td style={{ padding: 12 }}>{d.supplierName}</td>
                  <td style={{ padding: 12, textAlign: 'right' }}>{d.stockQuantity}</td>
                  <td style={{ padding: 12, textAlign: 'right' }}>{d.minQuantity}</td>
                  <td style={{ padding: 12, textAlign: 'right' }}>{d.basePrice}</td>
                  <td style={{ padding: 12 }}>
                    <button onClick={() => openEdit(d)} style={{ marginRight: 8 }}>Изменить</button>
                    <button onClick={() => deleteMu.mutate(d.id)}>Удалить</button>
                  </td>
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
