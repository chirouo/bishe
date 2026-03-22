import request from '../utils/request'

export function fetchTeacherDashboardOverview() {
  return request({
    url: '/teacher/dashboard/overview',
    method: 'get'
  })
}

export function fetchCourses() {
  return request({
    url: '/teacher/courses',
    method: 'get'
  })
}

export function fetchKnowledgePoints(courseId) {
  return request({
    url: '/teacher/knowledge-points',
    method: 'get',
    params: courseId ? { courseId } : {}
  })
}

export function fetchTeacherStatisticsAnalysis() {
  return request({
    url: '/teacher/statistics/analysis',
    method: 'get'
  })
}
