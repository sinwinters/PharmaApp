import { api } from './client'
import type { PageResponse } from './drugs'

export interface OrderItemDto {
  id: number
  drugId: number
  drugName: string
  quantity: number
  unitPrice: number
}

export interface OrderDto {
  id: number
  supplierId: number
  supplierName: string
  status: string
  createdBy: number | null
  createdAt: string
  items: OrderItemDto[]
}

export interface OrderItemRequest {
  drugId: number
  quantity: number
}

export function ordersList(page = 0, size = 20) {
  return api.get<PageResponse<OrderDto>>('/orders', { params: { page, size } }).then((r) => r.data)
}

export function createOrder(supplierId: number, items: OrderItemRequest[]) {
  return api.post<OrderDto>('/orders', { supplierId, items }).then((r) => r.data)
}
