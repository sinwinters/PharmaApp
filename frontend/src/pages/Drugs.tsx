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

export default function Drugs() {
  const [page, setPage] = useState(0)
  const [name, setName] = useState('')
  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [supplierId, setSupplierId] = useState<number | ''>('')
  const [editing, setEditing] = useState<DrugDto | null>(null)
  const [form, setForm] = useState<DrugCreateUpdate | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const queryClient = useQueryClient()

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
      alert('Лекарство создано 💊')
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
      alert('Обновлено ✅')
    },
    onError: (e) =>
      setActionError(getApiErrorMessage(e, 'Не удалось обновить лекарство.')),
  })

  const deleteMu = useMutation({
    mutationFn: deleteDrug,
    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['drugs'] })
      alert('Удалено 🗑️')
    },
    onError: (e) =>
      setActionError(getApiErrorMessage(e, 'Не удалось удалить лекарство.')),
  })

  const openCreate = () =>
    setForm({
      name: '',
      categoryId: 0,
      supplierId: 0,
      minQuantity: 10,
      unit: 'шт',
      basePrice: 0,
    })

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

  return (
    <div>
      <h1 className="page-header">Лекарства</h1>

      {actionError && <p style={{ color: '#b42318' }}>{actionError}</p>}
      {isFetching && !isLoading && <p>Обновляем список...</p>}

      {/* FILTERS */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
        <input
          placeholder="Поиск"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value === '' ? '' : Number(e.target.value))}>
          <option value="">Все категории</option>
          {categories?.content.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>

        <select value={supplierId} onChange={(e) => setSupplierId(e.target.value === '' ? '' : Number(e.target.value))}>
          <option value="">Все поставщики</option>
          {suppliers?.content.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>

        <button onClick={openCreate}>Добавить</button>
      </div>

      {/* LIST */}
      {isLoading ? (
        <p>Загрузка...</p>
      ) : isError ? (
        <p style={{ color: '#b42318' }}>
          {getApiErrorMessage(error, 'Ошибка загрузки лекарств')}
        </p>
      ) : (
        <>
          {data?.content.length === 0 ? (
            <p>Ничего не найдено</p>
          ) : (
            <table style={{ width: '100%', background: '#fff' }}>
              <tbody>
                {data?.content.map((d) => (
                  <tr key={d.id}>
                    <td>{d.name}</td>
                    <td>{d.categoryName}</td>
                    <td>{d.supplierName}</td>
                    <td>{d.stockQuantity}</td>
                    <td>{d.basePrice}</td>
                    <td>
                      <button onClick={() => openEdit(d)}>Изменить</button>
                      <button onClick={() => deleteMu.mutate(d.id)}>Удалить</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  )
}