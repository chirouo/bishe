<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPaperStudentAnswerDetail } from '../../api/paper'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref({
  paperId: undefined,
  title: '',
  courseName: '',
  totalScore: 0,
  studentId: undefined,
  studentName: '',
  status: '',
  submittedAt: '',
  studentTotalScore: 0,
  autoScore: 0,
  subjectiveScore: 0,
  questions: []
})

function formatDateTime(value) {
  return value ? value.replace('T', ' ') : '--'
}

function formatQuestionType(value) {
  if (value === 'SINGLE_CHOICE') {
    return '单选题'
  }
  if (value === 'SHORT_ANSWER') {
    return '简答题'
  }
  return value
}

function goBack() {
  router.push(`/teacher/papers/${route.params.paperId}/results`)
}

async function loadDetail() {
  loading.value = true
  try {
    const result = await fetchPaperStudentAnswerDetail(route.params.paperId, route.params.studentId)
    detail.value = result.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="paper-student-answer-page" v-loading="loading">
    <el-card shadow="hover">
      <div class="header-row">
        <div>
          <h3>{{ detail.title }} / {{ detail.studentName }}</h3>
          <p>
            {{ detail.courseName }} / 总分 {{ detail.studentTotalScore }} / {{ detail.totalScore }} /
            提交时间 {{ formatDateTime(detail.submittedAt) }}
          </p>
        </div>
        <el-button plain @click="goBack">返回成绩明细</el-button>
      </div>
    </el-card>

    <div class="metric-row">
      <el-card shadow="hover">
        <div class="metric">
          <span>客观题得分</span>
          <strong>{{ detail.autoScore }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>主观题得分</span>
          <strong>{{ detail.subjectiveScore }}</strong>
        </div>
      </el-card>
    </div>

    <el-card
      v-for="question in detail.questions"
      :key="question.questionId"
      shadow="hover"
      class="question-card"
    >
      <div class="question-title">
        <span>第 {{ question.questionOrder }} 题</span>
        <el-tag size="small" type="info">{{ formatQuestionType(question.questionType) }}</el-tag>
        <span>{{ question.gainedScore }} / {{ question.fullScore }} 分</span>
      </div>

      <p class="question-stem">{{ question.stem }}</p>

      <div v-if="question.options?.length" class="option-list">
        <div v-for="option in question.options" :key="`${question.questionId}-${option.label}`" class="option-item">
          {{ option.label }}. {{ option.content }}
        </div>
      </div>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生答案">{{ question.studentAnswer || '未作答' }}</el-descriptions-item>
        <el-descriptions-item label="标准答案">{{ question.correctAnswer || '无' }}</el-descriptions-item>
        <el-descriptions-item label="教师/系统反馈">{{ question.feedback || '暂无反馈' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.paper-student-answer-page {
  display: grid;
  gap: 20px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.header-row h3 {
  margin: 0;
  font-size: 22px;
  color: #10345f;
}

.header-row p {
  margin: 8px 0 0;
  color: #6b7280;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric span {
  color: #6b7280;
}

.metric strong {
  font-size: 28px;
  color: #10345f;
}

.question-card {
  display: grid;
  gap: 16px;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #4b5563;
  font-weight: 600;
}

.question-stem {
  margin: 0;
  line-height: 1.7;
  color: #111827;
}

.option-list {
  display: grid;
  gap: 8px;
}

.option-item {
  padding: 10px 12px;
  border-radius: 10px;
  background: #f8fafc;
  color: #475569;
}

@media (max-width: 960px) {
  .header-row {
    flex-direction: column;
  }

  .metric-row {
    grid-template-columns: 1fr;
  }
}
</style>
