export interface LoginRequest {
    username: string;
    password: string;
}

export interface CurrentUser {
    id: number;
    username: string;
    nickname: string;
    department: number;
    departmentName: string;
    roles: string[]
    permissions: string[]
}

export interface LoginResponse {
    accessToken: string
    tokenType: string
    expiresIn: number
    user: CurrentUser
}