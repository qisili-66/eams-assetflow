export type AssetStatus = 'FREE' | 'USING' | 'REPAIR' | 'SCRAP'

export interface AssetItem {
  id: number
  assetNo: string
  name: string
  category: string
  price: number
  purchaseDate: string
  status: AssetStatus
  imageUrl?: string
  remark?: string
  currentUserName?: string
  scrapReason?: string
  scrappedAt?: string
  createdAt: string
}

export interface AssetPage {
  page: number
  size: number
  total: number
  records: AssetItem[]
}

export interface AssetQuery {
  page: number
  size: number
  keyword?: string
  category?: string
  status?: AssetStatus
}

export interface AssetSaveReq {
  assetNo: string
  name: string
  category: string
  price: number
  purchaseDate: string
  imageUrl?: string
  remark?: string
}

export interface FileUploadResult {
  fileName: string
  url: string
  size: number
}
