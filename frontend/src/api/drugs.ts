import { api } from './client'

export interface DrugDto {
  id: number
  name: string
  categoryId: number
  categoryName: string
  supplierId: number
  supplierName: string
  minQuantity: number
  unit: string
  basePrice: number
  stockQuantity: number
}

export interface DrugCreateUpdate {
  name: string
  categoryId: number
  supplierId: number
  minQuantity: number
  unit: string
  basePrice: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export function drugsApi(params: { name?: string; categoryId?: number; supplierId?: number; page?: number; size?: number }) {
  const search = new URLSearchParams()
  if (params.name) search.set('name', params.name)
  if (params.categoryId != null) search.set('categoryId', String(params.categoryId))
  if (params.supplierId != null) search.set('supplierId', String(params.supplierId))
  if (params.page != null) search.set('page', String(params.page))
  if (params.size != null) search.set('size', String(params.size))
  return api.get<PageResponse<DrugDto>>(`/drugs?${search}`).then((r) => r.data)
}

export function drugById(id: number) {
  return api.get<DrugDto>(`/drugs/${id}`).then((r) => r.data)
}

export function createDrug(body: DrugCreateUpdate) {
  return api.post<DrugDto>('/drugs', body).then((r) => r.data)
}

export function updateDrug(id: number, body: DrugCreateUpdate) {
  return api.put<DrugDto>(`/drugs/${id}`, body).then((r) => r.data)
}

export function deleteDrug(id: number) {
  return api.delete(`/drugs/${id}`)
}
