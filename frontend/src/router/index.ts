import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { pinia } from '../stores'


const MainLayout = () => import('../layouts/MainLayout.vue')
const LoginView = () => import('../views/auth/LoginView.vue')
const DashboardView = () => import('../views/dashboard/DashboardView.vue')
const PlaceholderView = () => import('../views/common/PlaceholderView.vue')
const NotFoundView = () => import('../views/common/NotFoundView.vue')
const DepartmentView = () => import('../views/departments/DepartmentView.vue')
const UserView = () => import('../views/users/UserView.vue')
const AssetView = () => import('../views/assets/AssetView.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta:{title:'登录'}
  },
  {
    path:'/',
    component: MainLayout,
    redirect:'/dashboard',
    children:[
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta:{title:'主页',requiresAuth: true, roles: ['ADMIN']}
      },
      {
        path: 'users',
        name: 'users',
        component: UserView,
        meta:{title:'用户管理',requiresAuth: true, roles: ['ADMIN']},
      },
        {
        path: 'departments',
        name: 'departments',
        component: DepartmentView,
         props:{title:'部门管理'},
        meta:{title:'部门管理',requiresAuth: true, roles: ['ADMIN']}
      },
        {
        path: 'assets',
        name: 'assets',
        component: AssetView,
        meta:{title:'资产管理',requiresAuth: true}
      },
        {
        path: 'applications',
        name: 'applications',
        component: PlaceholderView,
         props:{title:'申请审批'},
        meta:{title:'申请审批',requiresAuth: true}
      },
        {
        path: 'repairs',
        name: 'repairs',
        component: PlaceholderView,
         props:{title:'维修管理'},
        meta:{title:'维修管理',requiresAuth: true}
      },
        {
        path: 'operation-logs',
        name: 'operation-logs',
        component: PlaceholderView,
         props:{title:'操作日志'},
        meta:{title:'操作日志',requiresAuth: true, roles: ['ADMIN']}
      }
    ]
  },
  {
    path: '/:pathMatch(.*)',
    name: 'notfound',
    component: NotFoundView,
    meta:{title:'404'}
  }
]
const router= createRouter({
  history: createWebHistory(),
  routes
})
router.beforeEach(async(to)=>{
  const authStore = useAuthStore(pinia)
  const requiredRoles = to.meta.roles as string[] | undefined
  
  if(to.name=='login'){
    if(authStore.token&&!authStore.initialized){
      await authStore.initialize()
    }
    return authStore.isLoggedIn ?
    {path:authStore.homePath} : true
  }
  if(!to.meta.requiresAuth){
    return true
  }
  if(!authStore.initialized){
    await authStore.initialize()
  }
  if(!authStore.isLoggedIn){
    return {
      name:'login',
      query:{
        redirect:to.fullPath
      },
    }
  }
  if(requiredRoles && !authStore.hasAnyRole(requiredRoles)){
    return{ path:authStore.homePath}
}
  return true

})
  
export default router

