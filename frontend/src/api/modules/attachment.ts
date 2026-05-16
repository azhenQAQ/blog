import request from '@/api/request'
import type { AttachmentVO, AttachmentQueryRequest } from '@/types/attachment'
import type { PageResult } from '@/types/api'

/** 上传附件 */
export function uploadAttachment(file: File): Promise<AttachmentVO> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/attachment/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }) as Promise<AttachmentVO>
}

/** 分页查询附件列表 */
export function listAttachmentByPage(
  params: AttachmentQueryRequest,
): Promise<PageResult<AttachmentVO>> {
  return request.get('/attachment/list/page', { params }) as Promise<PageResult<AttachmentVO>>
}

/** 获取单个附件详情 */
export function getAttachment(id: number): Promise<AttachmentVO> {
  return request.get(`/attachment/${id}`) as Promise<AttachmentVO>
}

/** 删除附件 */
export function deleteAttachment(id: number): Promise<boolean> {
  return request.delete(`/attachment/delete/${id}`) as Promise<boolean>
}
