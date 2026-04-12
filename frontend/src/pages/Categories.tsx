import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { categoriesList, createCategory, updateCategory, deleteCategory, type CategoryDto } from '../api/categories'
import { getApiErrorMessage } from '../api/client'
import { useAuthStore } from '../store/authStore'
import Spinner from '../components/Spinner'

export default function Categories() {
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<CategoryDto | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [actionError, setActionError] = useState<string | null>(null)
  const roleName = useAuthStore((s) => s.roleName)
  const canManage = roleName === 'ADMIN'
  const queryClient = useQueryClient()

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: ['categories', page],
    queryFn: () => categoriesList(page, 10),
  })

  const createMu = useMutation({
    mutationFn: createCategory,
    onSuccess: () => {
      setActionError(null)
      setName('')
      setDescription('')
      queryClient.invalidateQueries({ queryKey: ['categories'] })
    },
    onError: (e) => setActionError(getApiErrorMessage(e, 'Не удалось создать категорию.')),
  })

  const updateMu = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: { name: string; description: string | null } }) => updateCategory(id, payload),
    onSuccess: () => {
      setActionError(null)
      setEditing(null)
      setName('')
      setDescription('')
      queryClient.invalidateQueries({ queryKey: ['categories'] })
    },
    onError: (e) => setActionError(getApiErrorMessage(e, 'Не удалось обновить категорию.')),
  })

  const deleteMu = useMutation({
    mutationFn: deleteCategory,
    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['categories'] })
    },
    onError: (e) => setActionError(getApiErrorMessage(e, 'Не удалось удалить категорию.')),
  })

  const submit = () => {
    const payload = { name: name.trim(), description: description.trim() || null }
    if (!payload.name) {
      setActionError('Название категории обязательно.')
      return
    }
    if (editing) {
      updateMu.mutate({ id: editing.id, payload })
      return
    }
    createMu.mutate(payload)
  }

  const startEdit = (category: CategoryDto) => {
    setEditing(category)
    setName(category.name)
    setDescription(category.description ?? '')
  }

  return (
    <div>
      <h1 className="page-header">Категории</h1>
      {canManage && (
        <div style={{ background: '#fff', borderRadius: 8, padding: 16, marginBottom: 16 }}>
          <h3>{editing ? 'Редактирование категории' : 'Новая категория'}</h3>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <input placeholder="Название" value={name} onChange={(e) => setName(e.target.value)} style={{ padding: 8, minWidth: 250 }} />
            <input placeholder="Описание" value={description} onChange={(e) => setDescription(e.target.value)} style={{ padding: 8, minWidth: 300 }} />
            <button onClick={submit} disabled={createMu.isPending || updateMu.isPending}>Сохранить</button>
            {editing && <button onClick={() => { setEditing(null); setName(''); setDescription('') }}>Отмена</button>}
          </div>
        </div>
      )}
      {actionError && <p style={{ color: '#b42318' }}>{actionError}</p>}
      {isFetching && !isLoading && <p>Обновляем список...</p>}

      {isLoading ? <Spinner label="Загружаем категории..." /> : isError ? <p style={{ color: '#b42318' }}>{getApiErrorMessage(error, 'Не удалось загрузить категории.')}</p> : (
        <div className="table-wrap"><table className="table">
          <thead>
            <tr style={{ borderBottom: '2px solid #eee' }}>
              <th style={{ textAlign: 'left', padding: 12 }}>ID</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Название</th>
              <th style={{ textAlign: 'left', padding: 12 }}>Описание</th>
              {canManage && <th style={{ textAlign: 'left', padding: 12 }}>Действия</th>}
            </tr>
          </thead>
          <tbody>
            {data?.content.map((c) => (
              <tr key={c.id} style={{ borderBottom: '1px solid #eee' }}>
                <td style={{ padding: 12 }}>{c.id}</td>
                <td style={{ padding: 12 }}>{c.name}</td>
                <td style={{ padding: 12 }}>{c.description ?? '—'}</td>
                {canManage && (
                  <td style={{ padding: 12 }}>
                    <button onClick={() => startEdit(c)} style={{ marginRight: 8 }}>Изменить</button>
                    <button onClick={() => deleteMu.mutate(c.id)}>Удалить</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table></div>
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
