import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { categoriesList, createCategory, updateCategory, deleteCategory, type CategoryDto } from '../api/categories'
import { getApiErrorMessage } from '../api/client'
import { useAuthStore } from '../store/authStore'
import { useToastStore } from '../store/toastStore'
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
  const pushToast = useToastStore((s) => s.push)

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
      pushToast('Категория создана')
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
      pushToast('Категория обновлена')
    },
    onError: (e) => setActionError(getApiErrorMessage(e, 'Не удалось обновить категорию.')),
  })

  const deleteMu = useMutation({
    mutationFn: deleteCategory,
    onSuccess: () => {
      setActionError(null)
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      pushToast('Категория удалена')
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

  const requestDelete = (id: number) => {
    if (!window.confirm('Удалить категорию? Это действие нельзя отменить.')) return
    deleteMu.mutate(id)
  }

  return (
    <div>
      <h1 className="page-header">Категории</h1>
      {canManage && (
        <div className="card" style={{ marginBottom: 16 }}>
          <h3>{editing ? 'Редактирование категории' : 'Новая категория'}</h3>
          <div className="page-actions">
            <input className="input" placeholder="Название" value={name} onChange={(e) => setName(e.target.value)} style={{ minWidth: 250 }} />
            <input className="input" placeholder="Описание" value={description} onChange={(e) => setDescription(e.target.value)} style={{ minWidth: 300 }} />
            <button className="btn" onClick={submit} disabled={createMu.isPending || updateMu.isPending}>Сохранить</button>
            {editing && <button className="btn btn-secondary" onClick={() => { setEditing(null); setName(''); setDescription('') }}>Отмена</button>}
          </div>
        </div>
      )}
      {actionError && <p className="error-text">{actionError}</p>}
      {isFetching && !isLoading && <p>Обновляем список...</p>}

      {isLoading ? <Spinner label="Загружаем категории..." /> : isError ? <p className="error-text">{getApiErrorMessage(error, 'Не удалось загрузить категории.')}</p> : (
        <div className="table-wrap"><table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Название</th>
              <th>Описание</th>
              {canManage && <th>Действия</th>}
            </tr>
          </thead>
          <tbody>
            {data?.content.map((c) => (
              <tr key={c.id}>
                <td>{c.id}</td>
                <td>{c.name}</td>
                <td>{c.description ?? '—'}</td>
                {canManage && (
                  <td>
                    <button className="btn btn-secondary" onClick={() => startEdit(c)} style={{ marginRight: 8 }}>Изменить</button>
                    <button className="btn btn-danger" onClick={() => requestDelete(c.id)}>Удалить</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table></div>
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
