export type UserStatus = 'ENABLED' | 'DISABLED'
export type RoleCode = 'ADMIN' | 'USER'

export interface UserItem {
  id: number
  username: string
  nickname: string
  departmentId: number
  departmentName: string
  status: UserStatus
  roleCodes: RoleCode[]
  createdAt: string
}

export interface UserPage {
  page: number
  size: number
  total: number
  records: UserItem[]
}

export interface UserQuery {
  page: number
  size: number
  keyword?: string
  departmentId?: number
  status?: UserStatus
}

export interface CreateUserReq {
  username: string
  password: string
  nickname: string
  departmentId: number
  roleCodes: RoleCode[]
}

export interface UpdateUserReq {
  nickname: string
  departmentId: number
  roleCodes: RoleCode[]
}
