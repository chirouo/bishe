import request from '../utils/request'

const AI_REQUEST_TIMEOUT_MS = 60000

export function fetchPapers(courseId) {
  return request({
    url: '/teacher/papers',
    method: 'get',
    params: courseId ? { courseId } : {}
  })
}

export function createPaper(data) {
  return request({
    url: '/teacher/papers',
    method: 'post',
    data
  })
}

export function publishPaper(paperId) {
  return request({
    url: `/teacher/papers/${paperId}/publish`,
    method: 'post'
  })
}

export function generateAiPaperDraft(data) {
  return request({
    url: '/teacher/papers/ai-draft',
    method: 'post',
    data,
    timeout: AI_REQUEST_TIMEOUT_MS
  })
}

export function fetchPaperResults(paperId) {
  return request({
    url: `/teacher/papers/${paperId}/results`,
    method: 'get'
  })
}

export function fetchPaperStudentAnswerDetail(paperId, studentId) {
  return request({
    url: `/teacher/papers/${paperId}/results/${studentId}`,
    method: 'get'
  })
}
