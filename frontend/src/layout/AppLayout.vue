<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.meta.activeMenu || route.path)

const menuItems = computed(() => {
  if (authStore.userInfo?.role === 'TEACHER') {
    return [
      { path: '/teacher/dashboard', label: '教师首页' },
      { path: '/teacher/knowledge-points', label: '知识点管理' },
      { path: '/teacher/questions', label: '题库管理' },
      { path: '/teacher/papers', label: '智能组卷' },
      { path: '/teacher/statistics', label: '班级统计' }
    ]
  }

  return [
    { path: '/student/dashboard', label: '学生首页' },
    { path: '/student/exams', label: '我的考试' },
    { path: '/student/results', label: '学习分析' }
  ]
})

const pageTitle = computed(() => route.meta.title || '离散数学阶段性测试平台')

function handleSelect(path) {
  router.push(path)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="brand">
        <h1>离散数学</h1>
        <p>阶段性测试平台</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        @select="handleSelect"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-panel">
      <header class="topbar">
        <div>
          <h2>{{ pageTitle }}</h2>
          <p>{{ authStore.userInfo?.realName }} / {{ authStore.userInfo?.role === 'TEACHER' ? '教师' : '学生' }}</p>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  background: #f2f5f9;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #10345f 0%, #1a5697 100%);
  color: #fff;
  padding: 24px 0;
}

.brand {
  padding: 0 24px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  margin-bottom: 12px;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
}

.brand p {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.75);
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.84);
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.main-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  background: #fff;
  box-shadow: 0 6px 24px rgba(16, 52, 95, 0.08);
}

.topbar h2 {
  margin: 0;
  font-size: 24px;
  color: #10345f;
}

.topbar p {
  margin: 6px 0 0;
  color: #6b7280;
}

.content {
  padding: 24px 32px 32px;
}

@media (max-width: 960px) {
  .app-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .topbar {
    padding: 20px;
  }

  .content {
    padding: 20px;
  }
}
</style>
