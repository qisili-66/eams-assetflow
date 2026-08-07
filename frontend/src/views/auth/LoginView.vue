<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import type { LoginRequest } from '../../types/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<LoginRequest>({
  username: '',
  password: '',
})

const rules: FormRules<LoginRequest> = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { max: 50, message: '账号不能超过 50 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度应为 6 至 50 个字符', trigger: 'blur' },
  ],
}

async function submit(): Promise<void> {
  if (!formRef.value) {
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    await authStore.loginByPassword(form)

    const redirect = typeof route.query.redirect === 'string'
      ? route.query.redirect
      : authStore.homePath

    await router.replace(redirect)
  } catch {
    // Axios 请求层负责显示后端错误信息。
  } finally {
    submitting.value = false
  }
}
</script>


<template>
    <main class="login-page">
     <section class="login-content">
  <p class="brand">EAMS</p>
  <h1>登录</h1>
  <p>企业资产管理系统</p>

  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="login-form"
    label-position="top"
    @submit.prevent="submit"
  >
    <el-form-item label="账号" prop="username">
      <el-input
        v-model="form.username"
        autocomplete="username"
        placeholder="请输入账号"
      />
    </el-form-item>

    <el-form-item label="密码" prop="password">
      <el-input
        v-model="form.password"
        autocomplete="current-password"
        placeholder="请输入密码"
        show-password
        type="password"
      />
    </el-form-item>

    <el-button
      :loading="submitting"
      class="submit-button"
      native-type="submit"
      type="primary"
    >
      登录
    </el-button>
  </el-form>
</section>
    </main>
  </template>




<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: #f5f7fa;
}

.login-content {
  width: min(100%, 360px);
  padding: 32px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.brand {
  margin: 0 0 16px;
  color: #0f766e;
  font-weight: 700;
}

h1 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 24px;
}

p {
  margin: 0;
  color: #64748b;
}
</style>