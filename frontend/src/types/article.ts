import type { TagVO } from './tag'

/** 文章状态常量 */
export const ARTICLE_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  ARCHIVED: 2,
} as const

export type ArticleStatus = (typeof ARTICLE_STATUS)[keyof typeof ARTICLE_STATUS]

export const articleStatusLabel: Record<number, string> = {
  [ARTICLE_STATUS.DRAFT]: '草稿',
  [ARTICLE_STATUS.PUBLISHED]: '已发布',
  [ARTICLE_STATUS.ARCHIVED]: '已归档',
}

/** 文章视图（列表用，不含 content） */
export interface ArticleVO {
  id: number
  title: string
  summary?: string
  coverImage?: string
  status: number
  isTop: number
  viewCount: number
  userId: number
  authorName?: string
  authorAvatar?: string
  tags?: TagVO[]
  createdAt: string
  updatedAt: string
}

/** 文章详情（编辑用，含 content） */
export interface ArticleDetailVO extends ArticleVO {
  content: string
}

/** 文章查询请求 */
export interface ArticleQueryRequest {
  title?: string
  status?: number
  isRecycleBin?: boolean
  sortField?: string
  sortOrder?: string
  current?: number
  pageSize?: number
}

/** 创建文章请求 */
export interface ArticleAddRequest {
  title: string
  content: string
  summary?: string
  coverImage?: string
  status: number
  isTop?: number
  tagIds?: number[]
}

/** 编辑文章请求 */
export interface ArticleEditRequest {
  id: number
  title?: string
  content?: string
  summary?: string
  coverImage?: string
  status?: number
  isTop?: number
  tagIds?: number[]
}
