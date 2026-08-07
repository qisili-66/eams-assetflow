<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const pageTitle = computed(() => String(route.meta.title ?? '主页'))
const userInitials=computed(()=>authStore.user?.nickname.slice(0,1)??'U')

const menuItems = computed(() => {
const items = [
  { path: '/dashboard', label: '主页' ,roles: ['ADMIN'] },
  { path: '/users', label: '用户管理' ,roles: ['ADMIN'] },
  { path: '/departments', label: '部门管理' ,roles: ['ADMIN'] },
  { path: '/assets', label: '资产管理' },
  { path: '/applications', label: '申请审批' },
  { path: '/repairs', label: '维修管理' },
  { path: '/operation-logs', label: '操作日志', roles: ['ADMIN'] },
]
 return items.filter((item) => !item.roles || authStore.hasAnyRole(item.roles))
})

function logout(): void {
  authStore.logout()
  router.replace('/login')
}

</script>

<template>
  <el-container class="app-shell">
    <el-aside class="sidebar" width="224px">
      <div class="brand">
        <strong>EAMS</strong>
        <span>企业资产管理系统</span>
      </div>

      <el-menu
        :default-active="route.path"
        :router="true"
        class="side-menu"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>企业资产管理系统</el-breadcrumb-item>
          <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

          <div class="user-area">
          <el-avatar :size="32">{{ userInitials }}</el-avatar>
          <span>{{ authStore.user?.nickname ?? authStore.user?.username }}</span>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="content">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--page);
}

.sidebar {
  min-height: 100vh;
  background: var(--sidebar);
}

.brand {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 24px 20px;
  color: var(--on-accent);
}

.brand strong {
  font-size: 22px;
}

.brand span {
  color: var(--sidebar-muted);
  font-size: 13px;
}

.side-menu {
  border-right: 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  color: var(--sidebar-muted);
}

.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-menu-item.is-active) {
  color: var(--on-accent);
  background: var(--accent);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-2);
  font-size: 14px;
}

.content {
  padding: 24px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 168px !important;
  }

  .brand {
    padding: 20px 14px;
  }

  .content {
    padding: 16px;
  }

  .user-area span {
    display: none;
  }
}

</style>