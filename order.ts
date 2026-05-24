import request from './request'

export interface OrderInfo {
  id?: number
  userId?: number
  seatId?: number
  areaId?: number
  startTime?: string
  endTime?: string
  actualStartTime?: string
  actualEndTime?: string
  duration?: number
  price?: number
  status?: number
  createTime?: string
}

export interface Statistics {
  totalRevenue: number
  todayRevenue: number
  totalOrders: number
  todayOrders: number
  seatUsageRate: number
}

export interface SeatRanking {
  seatId: number
  seatNumber: string
  usageCount: number
}

export interface UserRanking {
  userId: number
  username: string
  consumption: number
}

export const orderApi = {
  getList() {
    return request.get<OrderInfo[]>('/admin/orders')
  },
  getById(id: number) {
    return request.get<OrderInfo>(`/order/${id}`)
  },
  cancel(id: number) {
    return request.post(`/order/${id}/cancel`)
  },
  getStatistics() {
    return request.get<Statistics>('/admin/revenue')
  },
  getSeatRanking() {
    return request.get<{ ranking: SeatRanking[] }>('/admin/seat-ranking')
  },
  getUserRanking() {
    return request.get<{ ranking: UserRanking[] }>('/admin/user-ranking')
  },
}
