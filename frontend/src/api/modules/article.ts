import request from '@/api/request'
import type {
  ArticleVO,
  ArticleDetailVO,
  ArticleAddRequest,
  ArticleEditRequest,
  ArticleQueryRequest,
} from '@/types/article'
import type { PageResult } from '@/types/api'

/** 创建文章 */
export function addArticle(data: ArticleAddRequest): Promise<number> {
  return request.post('/article/add', data) as Promise<number>
}

/** 编辑文章 */
export function editArticle(data: ArticleEditRequest): Promise<boolean> {
  return request.put('/article/edit', data) as Promise<boolean>
}

/** 分页查询文章列表 */
export function listArticleByPage(
  params: ArticleQueryRequest,
): Promise<PageResult<ArticleVO>> {
  return request.get('/article/list/page', { params }) as Promise<PageResult<ArticleVO>>
}

/** 获取文章详情 */
export function getArticle(id: number): Promise<ArticleDetailVO> {
  return request.get(`/article/${id}`) as Promise<ArticleDetailVO>
}

/** 删除文章（软删除） */
export function deleteArticle(id: number): Promise<boolean> {
  return request.delete(`/article/delete/${id}`) as Promise<boolean>
}

/** 恢复文章（从回收站） */
export function restoreArticle(id: number): Promise<boolean> {
  return request.put(`/article/restore/${id}`) as Promise<boolean>
}

/** 永久删除文章 */
export function permanentDeleteArticle(id: number): Promise<boolean> {
  return request.delete(`/article/permanent/${id}`) as Promise<boolean>
}
