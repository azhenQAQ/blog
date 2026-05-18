import request from '@/api/request'
import type { CommentVO, CommentAddRequest, CommentQueryRequest } from '@/types/comment'
import type { PageResult } from '@/types/api'

/** 提交评论/留言 */
export function addComment(data: CommentAddRequest): Promise<number> {
  return request.post('/comment/public/add', data) as Promise<number>
}

/** 获取文章全量评论 */
export function listArticleComments(articleId: number): Promise<CommentVO[]> {
  return request.get(`/comment/public/article/${articleId}`) as Promise<CommentVO[]>
}

/** 获取留言板全量留言 */
export function listGuestbook(): Promise<CommentVO[]> {
  return request.get('/comment/public/guestbook') as Promise<CommentVO[]>
}

/** 分页查询评论列表 */
export function listCommentByPage(
  params: CommentQueryRequest,
): Promise<PageResult<CommentVO>> {
  return request.get('/comment/list/page', { params }) as Promise<PageResult<CommentVO>>
}

/** 删除评论（软删除） */
export function deleteComment(id: number): Promise<boolean> {
  return request.delete(`/comment/delete/${id}`) as Promise<boolean>
}

/** 永久删除评论 */
export function permanentDeleteComment(id: number): Promise<boolean> {
  return request.delete(`/comment/permanent/${id}`) as Promise<boolean>
}
