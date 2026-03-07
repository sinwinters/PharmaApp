import { api } from './client'
import type { PageResponse } from './drugs'

export interface SupplierDto {
  id: number
  name: string
  contactInfo: string | null
  email: string | null
  phone: string | null
}

export function suppliersList(page = 0, size = 100) {
  return api.get<PageResponse<SupplierDto>>('/suppliers', { params: { page, size } }).then((r) => r.data)
}
