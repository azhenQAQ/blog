/** 访问记录视图 */
export interface VisitRecordVO {
  id: number
  path: string
  ip: string | null
  userAgent: string | null
  referer: string | null
  articleId: number | null
  /** IP 地理位置 */
  location: string | null
  /** 浏览器摘要（浏览器+版本 / 操作系统 / 设备类型） */
  browserSummary: string | null
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
