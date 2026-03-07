import { api } from './client'
import type { PageResponse } from './drugs'

export interface CategoryDto {
  id: number
  name: string
  description: string | null
}

export function categoriesList(page = 0, size = 100) {
  return api.get<PageResponse<CategoryDto>>('/categories', { params: { page, size } }).then((r) => r.data)
}
