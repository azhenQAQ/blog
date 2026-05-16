/** 附件视图 */
export interface AttachmentVO {
  id: number
  userId: number
  username?: string
  originalName: string
  storedName: string
  storedPath: string
  thumbnailPath?: string
  fileSize: number
  fileType: string
  fileExt: string
  createdAt: string
  /** 完整访问 URL（后端拼接好，前端直接用） */
  url: string
  /** 缩略图完整访问 URL */
  thumbnailUrl?: string
}

/** 附件查询请求 */
export interface AttachmentQueryRequest {
  fileName?: string
  fileType?: string
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}
