import request from '../utils/request'

const AI_REQUEST_TIMEOUT_MS = 60000

export function fetchQuestions(params) {
  return request({
    url: '/teacher/questions',
    method: 'get',
    params
  })
}

export function createSingleChoiceQuestion(data) {
  return request({
    url: '/teacher/questions',
    method: 'post',
    data
  })
}

export function generateAiQuestionDraft(data) {
  return request({
    url: '/teacher/ai/questions/draft',
    method: 'post',
    data,
    timeout: AI_REQUEST_TIMEOUT_MS
  })
}
