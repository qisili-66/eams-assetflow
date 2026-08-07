<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createDepartment, getDepartmentTree } from '../../api/department'
import type { CreateDepartmentReq, DepartmentTreeNode } from '../../types/department'


const loading = ref(false)
const departments = ref<DepartmentTreeNode[]>([])
const open = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<CreateDepartmentReq>({
  name: '',
  parentId: 0,
  sortNo: 0
})

const parentOptions = computed<DepartmentTreeNode[]>(() => [
  {
    id: 0,
    name: '作为一级部门',
    parentId: 0,
    sortNo: 0,
    children: departments.value
  }
])

const rules: FormRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { max: 100, message: '部门名称不能超过100个字符', trigger: 'blur' }
  ],
  parentId: [{ required: true, message: '请选择父部门', trigger: 'change' }],
  sortNo: [{ required: true, message: '请输入排序值', trigger: 'change' }]
}

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
  Object.assign(form, { name: '', parentId: 0, sortNo: 0 })
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
    await createDepartment({
      ...form,
      name: form.name.trim()
    })
    ElMessage.success('新增成功')
    open.value = false
    await loadDepartments()
  } finally {
    saving.value = false
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
    />
    <el-dialog
  v-model="open"
  title="新增部门"
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
</style>