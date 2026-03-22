<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: 'teacher01',
  password: '123456'
})

async function handleLogin() {
  loading.value = true
  try {
    const userInfo = await authStore.login(form)
    ElMessage.success('登录成功')
    router.push(userInfo.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="intro-panel">
      <div class="badge">本科毕业设计</div>
      <h1>离散数学阶段性测试平台</h1>
      <p>
        面向教师与学生的测试、评阅与学习分析系统。
        当前版本已经预留智能组卷、智能评阅和学习画像扩展位。
      </p>
      <ul>
        <li>教师演示账号：`teacher01 / 123456`</li>
        <li>学生演示账号：`student01 / 123456`</li>
      </ul>
    </div>

    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>系统登录</h2>
          <span>Vue 3 + Spring Boot</span>
        </div>
      </template>

      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background:
    radial-gradient(circle at top left, rgba(28, 103, 179, 0.22), transparent 35%),
    linear-gradient(135deg, #0f2e52 0%, #1b5fa6 50%, #f2f5f9 50%, #f2f5f9 100%);
}

.intro-panel {
  color: #fff;
  padding: 88px 72px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.badge {
  width: fit-content;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  margin-bottom: 20px;
}

.intro-panel h1 {
  font-size: 44px;
  line-height: 1.2;
  margin: 0 0 20px;
}

.intro-panel p {
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.84);
  max-width: 560px;
}

.intro-panel ul {
  margin-top: 24px;
  line-height: 2;
  color: rgba(255, 255, 255, 0.88);
}

.login-card {
  width: min(440px, calc(100% - 40px));
  align-self: center;
  justify-self: center;
  border-radius: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
}

.card-header span {
  color: #6b7280;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .intro-panel {
    padding: 40px 24px 16px;
  }

  .intro-panel h1 {
    font-size: 32px;
  }

  .login-card {
    margin: 0 auto 32px;
  }
}
</style>

