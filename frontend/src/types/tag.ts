/** 标签视图 */
export interface TagVO {
  id: number
  name: string
  color?: string
  articleCount: number
  createdAt: string
  updatedAt: string
}

/** 创建标签请求 */
export interface TagAddRequest {
  name: string
  color?: string
}

/** 更新标签请求 */
export interface TagUpdateRequest {
  id: number
  name?: string
  color?: string
}

/** 标签查询请求 */
export interface TagQueryRequest {
  keyword?: string
  sortField?: string
  sortOrder?: string
  current?: number
  pageSize?: number
}
