import request from '@/api/request'
import type { TagVO, TagAddRequest, TagUpdateRequest, TagQueryRequest } from '@/types/tag'
import type { PageResult } from '@/types/api'

/** 添加标签 */
export function addTag(data: TagAddRequest): Promise<number> {
  return request.post('/tag/add', data) as Promise<number>
}

/** 更新标签 */
export function updateTag(data: TagUpdateRequest): Promise<boolean> {
  return request.put('/tag/update', data) as Promise<boolean>
}

/** 删除标签 */
export function deleteTag(id: number, force?: boolean): Promise<boolean> {
  return request.delete(`/tag/delete/${id}`, { params: { force } }) as Promise<boolean>
}

/** 分页查询标签列表 */
export function listTagByPage(params: TagQueryRequest): Promise<PageResult<TagVO>> {
  return request.get('/tag/list/page', { params }) as Promise<PageResult<TagVO>>
}

/** 获取所有标签（文章多选用） */
export function listAllTags(): Promise<TagVO[]> {
  return request.get('/tag/list/all') as Promise<TagVO[]>
}

/** 获取单个标签 */
export function getTag(id: number): Promise<TagVO> {
  return request.get(`/tag/${id}`) as Promise<TagVO>
}

/** 获取所有标签（公开） */
export function listPublicTags(): Promise<TagVO[]> {
  return request.get('/tag/public/list') as Promise<TagVO[]>
}
