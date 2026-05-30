import type { ArticleVO } from './article'

/** 文章统计 */
export interface ArticleStats {
  total: number
  published: number
  draft: number
  archived: number
}

/** 访问统计 */
export interface VisitStats {
  totalViews: number
  uniqueVisitors: number
}

/** 资源统计 */
export interface ResourceStats {
  tags: number
  comments: number
  attachments: number
  music: number
}

/** 标签文章数统计 */
export interface TagArticleStat {
  tagName: string
  tagColor: string
  articleCount: number
}

/** 控制台聚合响应 */
export interface DashboardVO {
  articleStats: ArticleStats
  visitStats: VisitStats
  resourceStats: ResourceStats
  hotArticles: ArticleVO[]
  draftArticles: ArticleVO[]
  tagArticleStats: TagArticleStat[]
}
