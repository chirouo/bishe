import { createRouter, createWebHistory } from 'vue-router'
import pinia from '../stores'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/teacher',
    component: () => import('../layout/AppLayout.vue'),
    meta: { role: 'TEACHER' },
    children: [
      {
        path: 'dashboard',
        name: 'teacher-dashboard',
        component: () => import('../views/teacher/DashboardView.vue'),
        meta: { title: '教师首页' }
      },
      {
        path: 'knowledge-points',
        name: 'teacher-knowledge-points',
        component: () => import('../views/teacher/KnowledgePointView.vue'),
        meta: { title: '知识点管理' }
      },
      {
        path: 'questions',
        name: 'teacher-questions',
        component: () => import('../views/teacher/QuestionBankView.vue'),
        meta: { title: '题库管理' }
      },
      {
        path: 'papers',
        name: 'teacher-papers',
        component: () => import('../views/teacher/PaperGeneratorView.vue'),
        meta: { title: '智能组卷' }
      },
      {
        path: 'papers/:paperId/results',
        name: 'teacher-paper-results',
        component: () => import('../views/teacher/PaperResultView.vue'),
        meta: { title: '试卷成绩明细', activeMenu: '/teacher/papers' }
      },
      {
        path: 'papers/:paperId/results/:studentId',
        name: 'teacher-paper-student-answer',
        component: () => import('../views/teacher/PaperStudentAnswerView.vue'),
        meta: { title: '学生答卷详情', activeMenu: '/teacher/papers' }
      },
      {
        path: 'statistics',
        name: 'teacher-statistics',
        component: () => import('../views/teacher/StatisticsView.vue'),
        meta: { title: '班级统计' }
      }
    ]
  },
  {
    path: '/student',
    component: () => import('../layout/AppLayout.vue'),
    meta: { role: 'STUDENT' },
    children: [
      {
        path: 'dashboard',
        name: 'student-dashboard',
        component: () => import('../views/student/DashboardView.vue'),
        meta: { title: '学生首页' }
      },
      {
        path: 'exams',
        name: 'student-exams',
        component: () => import('../views/student/ExamListView.vue'),
        meta: { title: '我的考试' }
      },
      {
        path: 'exams/:paperId',
        name: 'student-exam-detail',
        component: () => import('../views/student/ExamTakeView.vue'),
        meta: { title: '在线答题', activeMenu: '/student/exams' }
      },
      {
        path: 'results',
        name: 'student-results',
        component: () => import('../views/student/ResultView.vue'),
        meta: { title: '学习分析' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore(pinia)

  if (to.meta.public) {
    next()
    return
  }

  if (!authStore.isLoggedIn) {
    next('/login')
    return
  }

  if (!authStore.userInfo) {
    try {
      await authStore.refreshUser()
    } catch (error) {
      authStore.logout()
      next('/login')
      return
    }
  }

  if (to.meta.role && authStore.userInfo?.role !== to.meta.role) {
    next(authStore.userInfo?.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard')
    return
  }

  next()
})

export default router
