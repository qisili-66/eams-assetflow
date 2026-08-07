import { del, get, patch, post, put } from './request'
import type {
  AssetItem,
  AssetPage,
  AssetQuery,
  AssetSaveReq,
  FileUploadResult
} from '../types/asset'

export function getAssets(params: AssetQuery): Promise<AssetPage> {
  return get<AssetPage>('/assets', { params })
}

export function getAsset(id: number): Promise<AssetItem> {
  return get<AssetItem>(`/assets/${id}`)
}

export function createAsset(data: AssetSaveReq): Promise<number> {
  return post<number>('/assets', data)
}

export function updateAsset(id: number, data: AssetSaveReq): Promise<void> {
  return put<void>(`/assets/${id}`, data)
}

export function deleteAsset(id: number): Promise<void> {
  return del<void>(`/assets/${id}`)
}

export function scrapAsset(id: number, reason: string): Promise<void> {
  return patch<void>(`/assets/${id}/scrap`, { reason })
}

export function uploadImage(file: File): Promise<FileUploadResult> {
  const data = new FormData()
  data.append('file', file)
  return post<FileUploadResult>('/files/images', data)
}
