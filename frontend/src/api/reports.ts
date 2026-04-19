import { api } from './client'

export type ReportType = 'sales' | 'orders' | 'writeoffs' | 'minzdrav'

export interface ReportFilterDto {
  dateFrom?: string
  dateTo?: string
  categoryId?: number
  country?: string
  onlyExpired?: boolean
  onlyDefective?: boolean
}

export interface ChartDto {
  labels: string[]
  values: number[]
}

export function generateReport(type: ReportType, filter: ReportFilterDto) {
  return api.post(`/reports/${type}`, filter).then((r) => r.data)
}

export function exportSalesExcel(filter: ReportFilterDto) {
  return api.post('/reports/sales/export/excel', filter, { responseType: 'blob' }).then((r) => r.data as Blob)
}

export function exportSalesWord(filter: ReportFilterDto) {
  return api.post('/reports/sales/export/word', filter, { responseType: 'blob' }).then((r) => r.data as Blob)
}

export function getSalesChart(filter: ReportFilterDto) {
  return api
    .get<ChartDto>('/reports/sales/chart', {
      params: {
        dateFrom: filter.dateFrom,
        dateTo: filter.dateTo,
        categoryId: filter.categoryId,
        country: filter.country,
        onlyExpired: filter.onlyExpired,
        onlyDefective: filter.onlyDefective,
      },
    })
    .then((r) => r.data)
}

export function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  URL.revokeObjectURL(url)
}
