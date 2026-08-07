import { get, patch, post, put } from './request'
import type {
  CreateUserReq,
  UpdateUserReq,
  UserItem,
  UserPage,
  UserQuery,
  UserStatus
} from '../types/user'

export function getUsers(params: UserQuery): Promise<UserPage> {
  return get<UserPage>('/users', { params })
}

export function createUser(data: CreateUserReq): Promise<number> {
  return post<number>('/users', data)
}

export function updateUser(id: number, data: UpdateUserReq): Promise<void> {
  return put<void>(`/users/${id}`, data)
}

export function updateUserStatus(id: number, status: UserStatus): Promise<void> {
  return patch<void>(`/users/${id}/status`, { status })
}

export function resetUserPassword(id: number, newPassword: string): Promise<void> {
  return put<void>(`/users/${id}/password`, { newPassword })
}

export function getUser(id: number): Promise<UserItem> {
  return get<UserItem>(`/users/${id}`)
}
