<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchStudentExams } from '../../api/student'

const router = useRouter()
const loading = ref(false)
const exams = ref([])

const statusMap = {
  NOT_STARTED: '未作答',
  PENDING: '待完成',
  SUBMITTED: '已提交'
}

function formatStatus(status) {
  return statusMap[status] || status
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ') : '--'
}

function formatScore(value) {
  return value == null ? '--' : value
}

function handleOpenExam(row) {
  const paperId = Number(row.paperId)
  if (!Number.isInteger(paperId) || paperId <= 0) {
    return
  }
  router.push({
    name: 'student-exam-detail',
    params: { paperId }
  })
}

function actionText(row) {
  if (row.examStatus === 'SUBMITTED') {
    return '查看作答'
  }
  if (row.examStatus === 'PENDING') {
    return '继续答题'
  }
  return '开始考试'
}

async function loadExams() {
  loading.value = true
  try {
    const result = await fetchStudentExams()
    exams.value = result.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadExams()
})
</script>

<template>
  <el-card shadow="hover">
    <template #header>我的考试</template>
    <el-table v-loading="loading" :data="exams">
      <el-table-column prop="title" label="试卷名称" min-width="220" />
      <el-table-column prop="courseName" label="课程" width="160" />
      <el-table-column prop="paperTotalScore" label="卷面总分" width="100" />
      <el-table-column label="我的状态" width="120">
        <template #default="{ row }">
          {{ formatStatus(row.examStatus) }}
        </template>
      </el-table-column>
      <el-table-column label="我的得分" width="100">
        <template #default="{ row }">
          {{ formatScore(row.studentScore) }}
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.submittedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenExam(row)">
            {{ actionText(row) }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
