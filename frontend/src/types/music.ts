/** 音乐视图 */
export interface MusicVO {
  id: number
  title: string
  artist: string
  coverUrl: string
  audioUrl: string
  lrcUrl: string
  duration: number
  sortOrder: number
  createdAt: string
}

/** 歌词行 */
export interface LRCLine {
  time: number
  text: string
}

/** 创建音乐请求 */
export interface MusicAddRequest {
  title: string
  artist?: string
  sortOrder?: number
}

/** 更新音乐请求 */
export interface MusicEditRequest {
  id: number
  title?: string
  artist?: string
  sortOrder?: number
}

/** 音乐查询请求 */
export interface MusicQueryRequest {
  keyword?: string
  current?: number
  pageSize?: number
}
