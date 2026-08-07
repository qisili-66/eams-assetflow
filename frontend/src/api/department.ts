
import { get, post } from './request'
import type { CreateDepartmentReq, DepartmentTreeNode } from '../types/department'

export function getDepartmentTree():Promise<DepartmentTreeNode[]>{
    return get<DepartmentTreeNode[]>('/departments/tree')
}

export function createDepartment(data: CreateDepartmentReq): Promise<number> {
  return post<number>('/departments', data)
}