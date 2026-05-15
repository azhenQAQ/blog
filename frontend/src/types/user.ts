/** 登录用户视图（脱敏） */
export interface LoginUserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  userProfile: string
  role: string
  createdAt: string
  updatedAt: string
}

/** 用户视图（脱敏，供管理员展示） */
export interface UserVO extends LoginUserVO {}

/** 用户注册请求 */
export interface UserRegisterRequest {
  username: string
  password: string
  checkPassword: string
}

/** 用户登录请求 */
export interface UserLoginRequest {
  username: string
  password: string
}

/** 更新个人信息请求 */
export interface UserUpdateMyRequest {
  nickname?: string
  userProfile?: string
}

/** 修改密码请求 */
export interface UserUpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}

/** 用户查询请求（管理员） */
export interface UserQueryRequest {
  id?: number
  username?: string
  nickname?: string
  userProfile?: string
  role?: string
  sortField?: string
  sortOrder?: string
  current?: number
  pageSize?: number
}
