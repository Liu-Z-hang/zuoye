import request from './request'

export interface PricingRule {
  id?: number
  startTime: string
  endTime: string
  pricePerHour: number
  priority?: number
}

export const pricingApi = {
  getList() {
    return request.get<PricingRule[]>('/pricing')
  },
  getById(id: number) {
    return request.get<PricingRule>(`/pricing/${id}`)
  },
  create(data: PricingRule) {
    return request.post('/pricing', data)
  },
  update(data: PricingRule) {
    return request.put(`/pricing/${data.id}`, data)
  },
  delete(id: number) {
    return request.delete(`/pricing/${id}`)
  },
}
