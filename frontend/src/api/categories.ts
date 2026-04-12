import { api } from './client'
import type { PageResponse } from './drugs'

export interface CategoryDto {
  id: number
  name: string
  description: string | null
}

export interface CategoryCreateUpdate {
  name: string
  description: string | null
}

export function categoriesList(page = 0, size = 100) {
  return api.get<PageResponse<CategoryDto>>('/categories', { params: { page, size } }).then((r) => r.data)
}

export function createCategory(body: CategoryCreateUpdate) {
  return api.post<CategoryDto>('/categories', body).then((r) => r.data)
}

export function updateCategory(id: number, body: CategoryCreateUpdate) {
  return api.put<CategoryDto>(`/categories/${id}`, body).then((r) => r.data)
}

export function deleteCategory(id: number) {
  return api.delete(`/categories/${id}`)
}
