<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
  type UploadProps
} from 'element-plus'
import {
  createAsset,
  deleteAsset,
  getAsset,
  getAssets,
  scrapAsset,
  updateAsset,
  uploadImage
} from '../../api/asset'
import { useAuthStore } from '../../stores/auth'
import type { AssetItem, AssetQuery, AssetSaveReq, AssetStatus } from '../../types/asset'

interface AssetForm {
  assetNo: string
  name: string
  category: string
  price?: number
  purchaseDate: string
  imageUrl: string
  remark: string
}

const statusOptions: { value: AssetStatus; label: string }[] = [
  { value: 'FREE', label: '空闲' },
  { value: 'USING', label: '使用中' },
  { value: 'REPAIR', label: '维修中' },
  { value: 'SCRAP', label: '已报废' }
]

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const uploadLoading = ref(false)
const open = ref(false)
const editId = ref<number>()
const formRef = ref<FormInstance>()
const records = ref<AssetItem[]>([])
const total = ref(0)

const query = reactive<AssetQuery>({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  status: undefined
})

const form = reactive<AssetForm>({
  assetNo: '',
  name: '',
  category: '',
  price: undefined,
  purchaseDate: '',
  imageUrl: '',
  remark: ''
})

const formTitle = computed(() => (editId.value === undefined ? '新增资产' : '编辑资产'))

const rules: FormRules = {
  assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入资产分类', trigger: 'blur' }],
  price: [{ required: true, message: '请输入资产价格', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购入日期', trigger: 'change' }]
}

function resetForm(): void {
  Object.assign(form, {
    assetNo: '',
    name: '',
    category: '',
    price: undefined,
    purchaseDate: '',
    imageUrl: '',
    remark: ''
  })
}

function imageUrl(url?: string): string {
  return url ?? ''
}

function formatTime(value?: string): string {
  return value ? value.replace('T', ' ') : '-'
}

function formatPrice(value: number): string {
  return value.toFixed(2)
}

function statusType(status: AssetStatus): 'success' | 'warning' | 'danger' | 'info' {
  const types = {
    FREE: 'success',
    USING: 'info',
    REPAIR: 'warning',
    SCRAP: 'danger'
  } as const
  return types[status]
}

function statusLabel(status: AssetStatus): string {
  return statusOptions.find((item) => item.value === status)?.label ?? status
}

async function loadAssets(): Promise<void> {
  loading.value = true
  try {
    const data = await getAssets(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search(): void {
  query.page = 1
  void loadAssets()
}

function resetQuery(): void {
  Object.assign(query, { page: 1, size: 10, keyword: '', category: '', status: undefined })
  void loadAssets()
}

async function openCreate(): Promise<void> {
  editId.value = undefined
  resetForm()
  open.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(row: AssetItem): Promise<void> {
  const asset = await getAsset(row.id)
  editId.value = asset.id
  Object.assign(form, {
    assetNo: asset.assetNo,
    name: asset.name,
    category: asset.category,
    price: asset.price,
    purchaseDate: asset.purchaseDate,
    imageUrl: asset.imageUrl ?? '',
    remark: asset.remark ?? ''
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
  if (!valid || form.price === undefined) {
    return
  }

  saving.value = true
  try {
    const data: AssetSaveReq = {
      assetNo: form.assetNo.trim(),
      name: form.name.trim(),
      category: form.category.trim(),
      price: form.price,
      purchaseDate: form.purchaseDate,
      imageUrl: form.imageUrl || undefined,
      remark: form.remark.trim() || undefined
    }
    if (editId.value === undefined) {
      await createAsset(data)
      ElMessage.success('新增成功')
    } else {
      await updateAsset(editId.value, data)
      ElMessage.success('修改成功')
    }
    open.value = false
    await loadAssets()
  } finally {
    saving.value = false
  }
}

async function remove(row: AssetItem): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定删除资产“${row.name}”吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAsset(row.id)
    ElMessage.success('删除成功')
    await loadAssets()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

async function scrap(row: AssetItem): Promise<void> {
  try {
    const { value } = await ElMessageBox.prompt(
      `请填写资产“${row.name}”的报废原因`,
      '报废资产',
      {
        confirmButtonText: '确认报废',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：主板损坏，维修成本过高',
        inputValidator: (value) => value.trim().length > 0 || '请输入报废原因'
      }
    )
    await scrapAsset(row.id, value.trim())
    ElMessage.success('资产已报废')
    await loadAssets()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const accepted = ['image/jpeg', 'image/png'].includes(file.type)
  if (!accepted) {
    ElMessage.error('仅支持 JPG 或 PNG 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片不能超过5MB')
    return false
  }
  return true
}

async function handleUpload(options: Parameters<NonNullable<UploadProps['httpRequest']>>[0]): Promise<void> {
  uploadLoading.value = true
  try {
    const result = await uploadImage(options.file)
    form.imageUrl = result.url
    options.onSuccess?.(result)
    ElMessage.success('图片上传成功')
  } catch (error) {
    throw error
  } finally {
    uploadLoading.value = false
  }
}

function clearImage(): void {
  form.imageUrl = ''
}

onMounted(() => {
  void loadAssets()
})
</script>

<template>
  <section class="asset-page">
    <header class="page-header">
      <div>
        <h1>资产管理</h1>
        <p>维护固定资产台账、状态和图片资料</p>
      </div>
      <el-button v-if="authStore.isAdmin" type="primary" @click="openCreate">
        新增资产
      </el-button>
    </header>

    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="资产编号或名称"
        @keyup.enter="search"
      />
      <el-input v-model="query.category" clearable placeholder="全部分类" @keyup.enter="search" />
      <el-select v-model="query.status" clearable placeholder="全部状态">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records" class="asset-table" empty-text="暂无资产数据">
      <el-table-column label="图片" width="82" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="imageUrl(row.imageUrl)"
            :preview-src-list="[imageUrl(row.imageUrl)]"
            fit="cover"
            preview-teleported
            class="asset-image"
          />
          <span v-else class="empty-image">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="assetNo" label="资产编号" min-width="150" />
      <el-table-column prop="name" label="资产名称" min-width="160" />
      <el-table-column prop="category" label="分类" min-width="120" />
      <el-table-column label="价格" min-width="110" align="right">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="purchaseDate" label="购入日期" min-width="120" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前使用人" min-width="120">
        <template #default="{ row }">{{ row.currentUserName || '-' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column v-if="authStore.isAdmin" label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" :disabled="row.status !== 'FREE'" @click="scrap(row)">
            报废
          </el-button>
          <el-button link type="danger" :disabled="row.status !== 'FREE'" @click="remove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="loadAssets"
        @size-change="search"
      />
    </div>

    <el-dialog v-model="open" :title="formTitle" width="640px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="资产编号" prop="assetNo">
            <el-input v-model="form.assetNo" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="资产名称" prop="name">
            <el-input v-model="form.name" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="资产分类" prop="category">
            <el-input v-model="form.category" maxlength="30" placeholder="例如：LAPTOP" />
          </el-form-item>
          <el-form-item label="购入价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="100" controls-position="right" />
          </el-form-item>
          <el-form-item label="购入日期" prop="purchaseDate">
            <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </div>
        <el-form-item label="资产图片">
          <div class="upload-row">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/jpeg,image/png"
            >
              <el-button :loading="uploadLoading">上传图片</el-button>
            </el-upload>
            <el-image
              v-if="form.imageUrl"
              :src="imageUrl(form.imageUrl)"
              :preview-src-list="[imageUrl(form.imageUrl)]"
              fit="cover"
              preview-teleported
              class="form-image"
            />
            <el-button v-if="form.imageUrl" link type="danger" @click="clearImage">移除图片</el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
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
.asset-page {
  padding: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 8px;
}

.page-header,
.filter-bar,
.pager,
.upload-row {
  display: flex;
  align-items: center;
}

.page-header {
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h1,
.page-header p {
  margin: 0;
}

.page-header h1 {
  color: var(--ink-1);
  font-size: 24px;
}

.page-header p {
  margin-top: 8px;
  color: var(--ink-3);
  font-size: 14px;
}

.filter-bar {
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-bar :deep(.el-input),
.filter-bar :deep(.el-select) {
  width: 200px;
}

.asset-table {
  width: 100%;
}

.asset-image,
.form-image {
  border: 1px solid var(--border);
  border-radius: 6px;
}

.asset-image {
  width: 42px;
  height: 42px;
}

.form-image {
  width: 72px;
  height: 72px;
}

.empty-image {
  color: var(--ink-3);
  font-size: 12px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.form-grid :deep(.el-input-number),
.form-grid :deep(.el-date-editor) {
  width: 100%;
}

.upload-row {
  min-height: 72px;
  gap: 12px;
}

@media (max-width: 768px) {
  .asset-page {
    padding: 16px;
  }

  .filter-bar :deep(.el-input),
  .filter-bar :deep(.el-select) {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
