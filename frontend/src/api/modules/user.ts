import request from '../request'
import type {
  LoginUserVO,
  UserLoginRequest,
  UserRegisterRequest,
  UserUpdateMyRequest,
  UserUpdatePasswordRequest,
  UserQueryRequest,
} from '@/types/user'
import type { PageResult } from '@/types/api'

export function userRegister(data: UserRegisterRequest): Promise<number> {
  return request.post('/user/register', data) as Promise<number>
}

export function userLogin(data: UserLoginRequest): Promise<LoginUserVO> {
  return request.post('/user/login', data) as Promise<LoginUserVO>
}

export function userLogout(): Promise<boolean> {
  return request.post('/user/logout') as Promise<boolean>
}

export function getCurrentUser(): Promise<LoginUserVO> {
  return request.get('/user/current') as Promise<LoginUserVO>
}

export function updateMyUser(data: UserUpdateMyRequest): Promise<boolean> {
  return request.put('/user/update/my', data) as Promise<boolean>
}

export function updatePassword(data: UserUpdatePasswordRequest): Promise<boolean> {
  return request.put('/user/update/password', data) as Promise<boolean>
}

export function listUserByPage(params: UserQueryRequest): Promise<PageResult<LoginUserVO>> {
  return request.get('/user/list/page', { params }) as Promise<PageResult<LoginUserVO>>
}
