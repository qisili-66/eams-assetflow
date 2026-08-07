import { post, get } from './request'
import type{ LoginRequest, LoginResponse, CurrentUser } from '../types/auth'

export function login(data:LoginRequest): Promise<LoginResponse>{
    return post<LoginResponse>('/auth/login', data)
}

export function getCurrentUser(): Promise<CurrentUser>{
    return get<CurrentUser>('/auth/me')
}