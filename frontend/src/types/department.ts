export interface CreateDepartmentReq {
  name: string
  parentId: number
  sortNo: number
}

export type UpdateDepartmentReq = CreateDepartmentReq

export interface DepartmentTreeNode {
  id: number
  name: string
  parentId: number
  sortNo: number
  children: DepartmentTreeNode[]
}