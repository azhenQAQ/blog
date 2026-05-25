/** 访问记录视图 */
export interface VisitRecordVO {
  id: number
  path: string
  ip: string | null
  userAgent: string | null
  referer: string | null
  articleId: number | null
  createdAt: string
}

/** 访问记录查询请求 */
export interface VisitRecordQueryRequest {
  path?: string
  ip?: string
  referer?: string
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}
