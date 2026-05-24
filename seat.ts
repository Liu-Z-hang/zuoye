import request from './request'

export interface Seat {
  id?: number
  areaId?: number
  seatNumber: string
  type?: number
  priceCoefficient?: number
  status?: number
  description?: string
  createTime?: string
}

export const seatApi = {
  getList(areaId?: number) {
    if (areaId) {
      return request.get<Seat[]>(`/seat/area/${areaId}`)
    }
    return request.get<Seat[]>('/seat')
  },
  getById(id: number) {
    return request.get<Seat>(`/seat/${id}`)
  },
  create(data: Seat) {
    return request.post('/seat', data)
  },
  update(data: Seat) {
    return request.put(`/seat/${data.id}`, data)
  },
  delete(id: number) {
    return request.delete(`/seat/${id}`)
  },
  updateStatus(id: number, status: string) {
    return request.put(`/seat/${id}/status`, null, { params: { status } })
  },
}
