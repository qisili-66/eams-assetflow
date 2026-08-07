import { del, get, post, put } from './request'
import type {
  CreateDepartmentReq,
  DepartmentTreeNode,
  UpdateDepartmentReq
} from '../types/department'

export function getDepartmentTree():Promise<DepartmentTreeNode[]>{
    return get<DepartmentTreeNode[]>('/departments/tree')
}

export function createDepartment(data: CreateDepartmentReq): Promise<number> {
  return post<number>('/departments', data)
}


export function updateDepartment(
  id: number,
  data: UpdateDepartmentReq
): Promise<void> {
  return put<void>(`/departments/${id}`, data)
}

export function deleteDepartment(id: number): Promise<void> {
  return del<void>(`/departments/${id}`)
}