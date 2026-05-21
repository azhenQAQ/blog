import request from '@/api/request'
import type { MusicVO, MusicQueryRequest } from '@/types/music'
import type { PageResult } from '@/types/api'

/** 公开接口 - 获取歌单 */
export function listMusicPublic(): Promise<MusicVO[]> {
  return request.get('/music/public/list') as Promise<MusicVO[]>
}

/** 添加音乐 */
export function addMusic(data: FormData): Promise<number> {
  return request.post('/music/add', data) as Promise<number>
}

/** 更新音乐 */
export function updateMusic(data: FormData): Promise<boolean> {
  return request.put('/music/edit', data) as Promise<boolean>
}

/** 删除音乐 */
export function deleteMusic(id: number): Promise<boolean> {
  return request.delete(`/music/delete/${id}`) as Promise<boolean>
}

/** 分页查询音乐列表 */
export function listMusicByPage(
  params: MusicQueryRequest,
): Promise<PageResult<MusicVO>> {
  return request.get('/music/list/page', { params }) as Promise<PageResult<MusicVO>>
}

/** 获取单个音乐 */
export function getMusic(id: number): Promise<MusicVO> {
  return request.get(`/music/${id}`) as Promise<MusicVO>
}
