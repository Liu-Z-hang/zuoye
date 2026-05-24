import request from './request'

export interface Area {
  id?: number
  name: string
  description?: string
  status?: string
  createTime?: string
}

export const areaApi = {
  getList() {
    return request.get<Area[]>('/area')
  },
  getById(id: number) {
    return request.get<Area>(`/area/${id}`)
  },
  create(data: Area) {
    return request.post('/area', data)
  },
  update(data: Area) {
    return request.put(`/area/${data.id}`, data)
  },
  delete(id: number) {
    return request.delete(`/area/${id}`)
  },
}
