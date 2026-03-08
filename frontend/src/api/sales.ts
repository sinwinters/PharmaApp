import { api } from './client'
import type { PageResponse } from './drugs'

export interface SaleItemDto {
  drugId: number
  drugName: string
  quantity: number
  unitPrice: number
  totalBeforeDiscount: number
  discountPercent: number
  total: number
}

export interface SaleDto {
  id: number
  userId: number
  username: string
  totalAmount: number
  totalBeforeDiscount: number
  discountAmount: number
  benefitCode?: string | null
  benefitLawReference?: string | null
  edsRequired?: boolean
  edsValidated?: boolean
  edsProvider?: string | null
  prescriptionNumber?: string | null
  createdAt: string
  items: SaleItemDto[]
}

export interface SaleItemRequest {
  drugId: number
  quantity: number
}

export interface CreateSaleRequest {
  items: SaleItemRequest[]
  benefitCode?: string
  prescriptionNumber?: string
  edsSignature?: string
  edsProvider?: string
}

export interface BenefitProgramDto {
  code: string
  title: string
  lawReference: string
  discountPercent: number
  description: string
}

export function salesList(page = 0, size = 20) {
  return api.get<PageResponse<SaleDto>>('/sales', { params: { page, size } }).then((r) => r.data)
}

export function benefitsRbList() {
  return api.get<BenefitProgramDto[]>('/sales/benefits/rb').then((r) => r.data)
}

export function createSale(body: CreateSaleRequest) {
  return api.post<SaleDto>('/sales', body).then((r) => r.data)
}
