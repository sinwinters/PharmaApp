import { useMemo, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Chart } from 'chart.js/auto'
import {
  type ReportFilterDto,
  type ReportType,
  downloadBlob,
  exportSalesExcel,
  exportSalesWord,
  generateReport,
  getSalesChart,
} from '../api/reports'
import { getApiErrorMessage } from '../api/client'
import { useToastStore } from '../store/toastStore'

const reportTypes: { value: ReportType; label: string }[] = [
  { value: 'sales', label: 'Sales Report' },
  { value: 'orders', label: 'Orders Report' },
  { value: 'writeoffs', label: 'Write-off Report' },
  { value: 'minzdrav', label: 'Minzdrav Report' },
]

export default function Reports() {
  const pushToast = useToastStore((s) => s.push)
  const [reportType, setReportType] = useState<ReportType>('sales')
  const [filter, setFilter] = useState<ReportFilterDto>({})
  const [reportData, setReportData] = useState<any>(null)

  const chartCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const chartInstanceRef = useRef<Chart | null>(null)

  const reportMutation = useMutation({
    mutationFn: () => generateReport(reportType, filter),
    onSuccess: (data) => setReportData(data),
    onError: (e) => pushToast(getApiErrorMessage(e, 'Не удалось сформировать отчёт')),
  })

  const chartMutation = useMutation({
    mutationFn: () => getSalesChart(filter),
    onSuccess: (chart) => {
      if (!chartCanvasRef.current) return
      chartInstanceRef.current?.destroy()
      chartInstanceRef.current = new Chart(chartCanvasRef.current, {
        type: 'bar',
        data: {
          labels: chart.labels,
          datasets: [
            {
              label: 'Revenue',
              data: chart.values,
              backgroundColor: '#2563eb',
            },
          ],
        },
      })
    },
    onError: (e) => pushToast(getApiErrorMessage(e, 'Не удалось построить график')),
  })

  const exportExcelMutation = useMutation({
    mutationFn: () => exportSalesExcel(filter),
    onSuccess: (blob) => downloadBlob(blob, 'sales-report.xlsx'),
    onError: (e) => pushToast(getApiErrorMessage(e, 'Ошибка экспорта Excel')),
  })

  const exportWordMutation = useMutation({
    mutationFn: () => exportSalesWord(filter),
    onSuccess: (blob) => downloadBlob(blob, 'sales-report.docx'),
    onError: (e) => pushToast(getApiErrorMessage(e, 'Ошибка экспорта Word')),
  })

  const rows = useMemo(() => {
    if (!reportData) return []
    if (Array.isArray(reportData.items)) return reportData.items
    if (Array.isArray(reportData.orders)) return reportData.orders
    return []
  }, [reportData])

  return (
    <div>
      <h1 className="page-header">Reports</h1>

      <div className="card report-filter-grid">
        <label>
          Report type
          <select value={reportType} onChange={(e) => setReportType(e.target.value as ReportType)}>
            {reportTypes.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </label>

        <label>
          Date from
          <input type="date" onChange={(e) => setFilter((f) => ({ ...f, dateFrom: e.target.value || undefined }))} />
        </label>

        <label>
          Date to
          <input type="date" onChange={(e) => setFilter((f) => ({ ...f, dateTo: e.target.value || undefined }))} />
        </label>

        <label>
          Category ID
          <input type="number" onChange={(e) => setFilter((f) => ({ ...f, categoryId: e.target.value ? Number(e.target.value) : undefined }))} />
        </label>

        <label>
          Country
          <input type="text" onChange={(e) => setFilter((f) => ({ ...f, country: e.target.value || undefined }))} />
        </label>

        <label className="checkbox-inline">
          <input type="checkbox" onChange={(e) => setFilter((f) => ({ ...f, onlyExpired: e.target.checked || undefined }))} />
          only expired
        </label>

        <label className="checkbox-inline">
          <input type="checkbox" onChange={(e) => setFilter((f) => ({ ...f, onlyDefective: e.target.checked || undefined }))} />
          only defective
        </label>

        <button className="btn" onClick={() => reportMutation.mutate()}>
          Generate Report
        </button>
      </div>

      {reportData && (
        <div style={{ marginTop: 16, display: 'flex', gap: 12 }}>
          <button className="btn" onClick={() => exportExcelMutation.mutate()} disabled={reportType !== 'sales'}>
            Export to Excel
          </button>
          <button className="btn btn-secondary" onClick={() => exportWordMutation.mutate()} disabled={reportType !== 'sales'}>
            Export to Word
          </button>
          <button className="btn" onClick={() => chartMutation.mutate()} disabled={reportType !== 'sales'}>
            Build Chart
          </button>
        </div>
      )}

      {rows.length > 0 && (
        <div className="table-wrap" style={{ marginTop: 16 }}>
          <table className="table">
            <thead>
              <tr>
                {Object.keys(rows[0]).map((k) => (
                  <th key={k}>{k}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row: any, idx: number) => (
                <tr key={idx}>
                  {Object.values(row).map((v: any, i: number) => (
                    <td key={i}>{typeof v === 'object' ? JSON.stringify(v) : String(v)}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="card" style={{ marginTop: 16 }}>
        <h3 style={{ marginTop: 0 }}>Chart</h3>
        <canvas ref={chartCanvasRef} height={120} />
      </div>
    </div>
  )
}
