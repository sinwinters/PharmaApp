import { api } from './client'
import type { PageResponse } from './drugs'

export interface SaleItemDto {
  drugId: number
  drugName: string
  quantity: number
  unitPrice: number
  total: number
}

export interface SaleDto {
  id: number
  userId: number
  username: string
  totalAmount: number
  createdAt: string
  items: SaleItemDto[]
}

export interface SaleItemRequest {
  drugId: number
  quantity: number
}

export function salesList(page = 0, size = 20) {
  return api.get<PageResponse<SaleDto>>('/sales', { params: { page, size } }).then((r) => r.data)
}

export function createSale(items: SaleItemRequest[]) {
  return api.post<SaleDto>('/sales', { items }).then((r) => r.data)
}
