/** 评论类型常量 */
export const COMMENT_TYPE = {
  COMMENT: 'comment',
  GUESTBOOK: 'guestbook',
} as const

export type CommentType = (typeof COMMENT_TYPE)[keyof typeof COMMENT_TYPE]

/** 评论/留言视图 */
export interface CommentVO {
  id: number
  type: CommentType
  articleId: number | null
  parentId: number | null
  replyToName: string | null
  nickname: string
  email: string
  content: string
  createdAt: string
}

/** 提交评论/留言请求 */
export interface CommentAddRequest {
  type: CommentType
  articleId?: number | null
  parentId?: number | null
  replyToName?: string | null
  nickname: string
  email: string
  content: string
}

/** 评论查询请求 */
export interface CommentQueryRequest {
  type?: CommentType
  nickname?: string
  email?: string
  content?: string
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}
