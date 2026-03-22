import request from '../utils/request'

export function fetchAiSettings() {
  return request({
    url: '/teacher/ai/settings',
    method: 'get'
  })
}

export function switchAiModel(model) {
  return request({
    url: '/teacher/ai/settings/model',
    method: 'put',
    data: { model }
  })
}
