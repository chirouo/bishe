import request from '../utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function fetchCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

