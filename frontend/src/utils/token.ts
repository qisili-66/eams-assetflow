const TOKEN_STORAGE_KEY = 'eams_access_token'

export function getAccessToken(): string{
    return localStorage.getItem(TOKEN_STORAGE_KEY) ?? ''
}

export function saveAccessToken(token: string){
    localStorage.setItem(TOKEN_STORAGE_KEY, token)
}

export function clearAccessToken(){
    localStorage.removeItem(TOKEN_STORAGE_KEY)
}