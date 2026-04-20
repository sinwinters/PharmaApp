import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  drugsApi,
  createDrug,
  updateDrug,
  deleteDrug,
  type DrugDto,
  type DrugCreateUpdate,
} from '../api/drugs'
import { categoriesList } from '../api/categories'
import { suppliersList } from '../api/suppliers'
import { getApiErrorMessage } from '../api/client'
import { useToastStore } from '../store/toastStore'

export default function Drugs() {
  const [page, setPage] = useState(0)
  const [name, setName] = useState('')
  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [supplierId, setSupplierId] = useState<number | ''>('')
  const [editing, setEditing] = useState<DrugDto | null>(null)
  const [form, setForm] = useState<DrugCreateUpdate | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const queryClient = useQueryClient()
  const pushToast = useToastStore((s) => s.push)

  useEffect(() => {
    setPage(0)
  }, [name, categoryId, supplierId])

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: ['drugs', page, name, categoryId, supplierId],
    queryFn: () =>
      drugsApi({
        page,
        size: 10,
        name: name || undefined,
        categoryId: categoryId !== '' ? categoryId : undefined,
        supplierId: supplierId !== '' ? supplierId : undefined,
      }),
  })

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoriesList(0, 200),
  })

  const { data: suppliers } = useQuery({
    queryKey: ['suppliers'],
    queryFn: () => suppliersList(0, 200),
  })

  const createMu = useMutation({
    mutationFn: createDrug,
    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['drugs'] })
      setForm(null)
      pushToast('Лекарство успешно создано')
    },
    onError: (e) =>
      setActionError(getApiErrorMessage(e, 'Не удалось создать лекарство.')),
  })

  const updateMu = useMutation({
    mutationFn: ({ id, body }: { id: number; body: DrugCreateUpdate }) =>
      updateDrug(id, body),
    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['drugs'] })
      setEditing(null)
      setForm(null)
      pushToast('Лекарство успешно обновлено')
    },
    onError: (e) =>
      setActionError(getApiErrorMessage(e, 'Не удалось обновить лекарство.')),
  })

  const deleteMu = useMutation({
    mutationFn: deleteDrug,
    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['drugs'] })
      pushToast('Лекарство удалено')
    },
    onError: (e) =>
      setActionError(getApiErrorMessage(e, 'Не удалось удалить лекарство.')),
  })

  const openCreate = () => {
    setEditing(null)
    setForm({
      name: '',
      categoryId: categories?.content[0]?.id ?? 0,
      supplierId: suppliers?.content[0]?.id ?? 0,
      minQuantity: 10,
      unit: 'шт',
      basePrice: 0,
    })
  }

  const openEdit = (d: DrugDto) => {
    setEditing(d)
    setForm({
      name: d.name,
      categoryId: d.categoryId,
      supplierId: d.supplierId,
      minQuantity: d.minQuantity,
      unit: d.unit,
      basePrice: d.basePrice,
    })
  }

  const requestDelete = (id: number) => {
    if (!window.confirm('Удалить лекарство? Это действие нельзя отменить.')) return
    deleteMu.mutate(id)
  }

  const saveForm = () => {
    if (!form || !form.name.trim() || form.categoryId <= 0 || form.supplierId <= 0) {
      setActionError('Заполните название, категорию и поставщика.')
      return
    }

    if (editing) {
      updateMu.mutate({ id: editing.id, body: form })
      return
    }

    createMu.mutate(form)
  }

  return (
    <div>
      <h1 className="page-header">Лекарства</h1>

      {actionError && <p className="error-text">{actionError}</p>}
      {isFetching && !isLoading && <p>Обновляем список...</p>}

      <div className="page-actions">
        <input className="input" placeholder="Поиск" value={name} onChange={(e) => setName(e.target.value)} />

        <select className="select" value={categoryId} onChange={(e) => setCategoryId(e.target.value === '' ? '' : Number(e.target.value))}>
          <option value="">Все категории</option>
          {categories?.content.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        <select className="select" value={supplierId} onChange={(e) => setSupplierId(e.target.value === '' ? '' : Number(e.target.value))}>
          <option value="">Все поставщики</option>
          {suppliers?.content.map((s) => (
            <option key={s.id} value={s.id}>{s.name}</option>
          ))}
        </select>

        <button className="btn" onClick={openCreate}>Добавить</button>
      </div>

      {form && (
        <div className="card" style={{ marginBottom: 16 }}>
          <h3>{editing ? 'Редактирование лекарства' : 'Новое лекарство'}</h3>
          <div className="page-actions">
            <input className="input" value={form.name} placeholder="Название" onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <select className="select" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value) })}>
              {categories?.content.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select className="select" value={form.supplierId} onChange={(e) => setForm({ ...form, supplierId: Number(e.target.value) })}>
              {suppliers?.content.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
            <input className="input" type="number" min={0} value={form.minQuantity} onChange={(e) => setForm({ ...form, minQuantity: Number(e.target.value) })} placeholder="Мин. остаток" />
            <input className="input" value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })} placeholder="Ед. измерения" />
            <input className="input" type="number" min={0} step="0.01" value={form.basePrice} onChange={(e) => setForm({ ...form, basePrice: Number(e.target.value) })} placeholder="Цена" />
            <button className="btn" onClick={saveForm} disabled={createMu.isPending || updateMu.isPending}>Сохранить</button>
            <button className="btn btn-secondary" onClick={() => { setForm(null); setEditing(null) }}>Отмена</button>
          </div>
        </div>
      )}

      {isLoading ? (
        <p>Загрузка...</p>
      ) : isError ? (
        <p className="error-text">{getApiErrorMessage(error, 'Ошибка загрузки лекарств')}</p>
      ) : (
        <>
          {data?.content.length === 0 ? (
            <p>Ничего не найдено</p>
          ) : (
            <div className="table-wrap">
              <table className="table">
                <tbody>
                  {data?.content.map((d) => (
                    <tr key={d.id}>
                      <td>{d.name}</td>
                      <td>{d.categoryName}</td>
                      <td>{d.supplierName}</td>
                      <td>{d.stockQuantity}</td>
                      <td>{d.basePrice}</td>
                      <td>
                        <button className="btn btn-secondary" onClick={() => openEdit(d)}>Изменить</button>{' '}
                        <button className="btn btn-danger" onClick={() => requestDelete(d.id)}>Удалить</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {data && data.totalPages > 1 && (
        <div className="pagination">
          <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Назад</button>
          <span>Стр. {page + 1} из {data.totalPages}</span>
          <button className="btn btn-secondary" disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Вперёд</button>
        </div>
      )}
    </div>
  )
}
