/** 后端统一响应包装 BaseResponse<T> */
export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

/** MyBatis-Plus 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
