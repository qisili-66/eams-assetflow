export interface DepartmentTreeNode{
    id: number;
    name: string;
    parentId: number;
    sortNo: number;
    children: DepartmentTreeNode[];
}
export interface CreateDepartmentReq {
  name: string
  parentId: number
  sortNo: number
}