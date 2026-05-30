import request from '@/api/request'
import type { DashboardVO } from '@/types/dashboard'

/** 获取控制台数据 */
export function getDashboard(): Promise<DashboardVO> {
  return request.get('/dashboard') as Promise<DashboardVO>
}
