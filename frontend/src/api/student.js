import request from '../utils/request'

const AI_REQUEST_TIMEOUT_MS = 60000

export function fetchStudentExams() {
  return request({
    url: '/student/exams',
    method: 'get'
  })
}

export function fetchStudentExamDetail(paperId) {
  return request({
    url: `/student/exams/${paperId}`,
    method: 'get'
  })
}

export function submitStudentExam(paperId, data) {
  return request({
    url: `/student/exams/${paperId}/submit`,
    method: 'post',
    data,
    timeout: AI_REQUEST_TIMEOUT_MS
  })
}

export function fetchStudentResultAnalysis() {
  return request({
    url: '/student/results/analysis',
    method: 'get'
  })
}
