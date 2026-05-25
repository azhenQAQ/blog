import request from '@/api/request'
import type { VisitRecordVO, VisitRecordQueryRequest } from '@/types/visitRecord'
import type { PageResult } from '@/types/api'

/** 分页查询访问记录 */
export function listVisitRecordByPage(
  params: VisitRecordQueryRequest,
): Promise<PageResult<VisitRecordVO>> {
  return request.get('/visitRecord/list/page', { params }) as Promise<PageResult<VisitRecordVO>>
}
