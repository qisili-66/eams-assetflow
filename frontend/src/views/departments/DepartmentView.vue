<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules
} from 'element-plus'
import {
  createDepartment,
  deleteDepartment,
  updateDepartment,
  getDepartmentTree
} from '../../api/department'
import type { CreateDepartmentReq, DepartmentTreeNode } from '../../types/department'

const loading = ref(false)
const departments = ref<DepartmentTreeNode[]>([])
const open = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const editId = ref<number>()
const formTitle = computed(() => (editId.value === undefined ? '新增部门' : '编辑部门'))

const form = reactive<CreateDepartmentReq>({
  name: '',
  parentId: 0,
  sortNo: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择父部门', trigger: 'change' }],
  sortNo: [{ required: true, message: '请输入排序值', trigger: 'change' }]
}

function excludeNode(nodes: DepartmentTreeNode[], id: number): DepartmentTreeNode[] {
  return nodes
    .filter((node) => node.id !== id)
    .map((node) => ({
      ...node,
      children: excludeNode(node.children, id)
    }))
}

const parentOptions = computed<DepartmentTreeNode[]>(() => [
  {
    id: 0,
    name: '作为一级部门',
    parentId: 0,
    sortNo: 0,
    children:
      editId.value === undefined
        ? departments.value
        : excludeNode(departments.value, editId.value)
  }
])

async function loadDepartments(): Promise<void> {
  loading.value = true

  try {
    departments.value = await getDepartmentTree()
  } catch {
    // Axios 请求层负责显示错误消息。
  } finally {
    loading.value = false
  }
}

async function openCreate(): Promise<void> {
  editId.value = undefined
  Object.assign(form, { name: '', parentId: 0, sortNo: 0 })
  open.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(dept: DepartmentTreeNode): Promise<void> {
  editId.value = dept.id
  Object.assign(form, {
    name: dept.name,
    parentId: dept.parentId,
    sortNo: dept.sortNo
  })
  open.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function save(): Promise<void> {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const data = {
      ...form,
      name: form.name.trim()
    }

    if (editId.value === undefined) {
      await createDepartment(data)
      ElMessage.success('新增成功')
    } else {
      await updateDepartment(editId.value, data)
      ElMessage.success('修改成功')
    }

    open.value = false
    await loadDepartments()
  } finally {
    saving.value = false
  }
}

async function remove(dept: DepartmentTreeNode): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定删除部门“${dept.name}”吗？`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteDepartment(dept.id)
    ElMessage.success('删除成功')
    await loadDepartments()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

onMounted(loadDepartments)
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>部门管理</h1>
        <p>组织结构</p>
      </div>

      <el-space>
        <el-button :loading="loading" @click="loadDepartments">刷新</el-button>
        <el-button type="primary" @click="openCreate">新增部门</el-button>
      </el-space>
    </div>

    <el-tree
      v-loading="loading"
      :data="departments"
      :props="{ label: 'name', children: 'children' }"
      default-expand-all
      empty-text="暂无部门数据"
      node-key="id"
    >
      <template #default="{ data }">
        <span class="tree-node">
          <span>{{ data.name }}</span>

          <span class="tree-actions">
            <el-button link type="primary" @click.stop="openEdit(data)">
              编辑
            </el-button>
            <el-button link type="danger" @click.stop="remove(data)">
              删除
            </el-button>
          </span>
        </span>
      </template>
    </el-tree>

    <el-dialog
      v-model="open"
      :title="formTitle"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="父部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            check-strictly
            default-expand-all
          />
        </el-form-item>

        <el-form-item label="排序值" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="saving" @click="open = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.page {
  padding: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

h1 {
  margin: 0 0 8px;
  color: var(--ink-1);
  font-size: 24px;
}

p {
  margin: 0;
  color: var(--ink-3);
  font-size: 14px;
}

.tree-node {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
}

.tree-actions {
  display: flex;
  gap: 8px;
}
</style>