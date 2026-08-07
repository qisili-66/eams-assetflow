import axios, { AxiosError,type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getAccessToken, clearAccessToken } from '../utils/token'
import type { ApiResponse } from '../types/api'

const request = axios.create({
    baseURL:'/api',
    timeout: 10000,
})

request.interceptors.request.use((config)=>{
    const token = getAccessToken()
    if(token){
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

request.interceptors.response.use(
    (response) => response,
      
(error:AxiosError< {message ?: string } >)=>{
    const status= error.response?.status 
    const message= error.response?.data?.message
    if (status === 401) {
      const isLoginRequest = error.config?.url === '/auth/login'
      clearAccessToken()
      ElMessage.error(message ?? '登录状态已失效')

      if (!isLoginRequest && window.location.pathname !== '/login') {
        window.location.replace('/login')
      }
    } else if(status===403){
        ElMessage.error(message ?? '没有权限')
    }else if(status===404){
        ElMessage.error(message ?? '请求资源不存在')
    }else{
        ElMessage.error(message ?? '请求错误，请稍后再试')
    }
    return Promise.reject(error)
}
)


function unwrap<T>(result: ApiResponse<T>): T {
    if (result.code === 0) {
        return result.data;
    }
    ElMessage.error(result.message);
    throw new Error(result.message);
}

async function send<T>(config: AxiosRequestConfig): Promise<T> {
    const response = await request(config);
    return unwrap(response.data);
}

export function get<T>(
    url: string,
    config?: AxiosRequestConfig
): Promise<T> {
    return send<T>({
         url, method: 'get', ...config 
        });
}
export function post<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
): Promise<T> {
    return send<T>({
        url, method: 'post', data, ...config
    });
}

export function put<T>(
    url: string,    

    data?: unknown,
    config?: AxiosRequestConfig
): Promise<T> { 
    return send<T>({
        url, method: 'put', data, ...config
    });
}

export function patch<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
): Promise<T> {
    return send<T>({
        url, method: 'patch', data, ...config
    });
}

export function del<T>(
    url: string,
    config?: AxiosRequestConfig
): Promise<T> {
    return send<T>({
        url, method: 'delete', ...config
    });
}
