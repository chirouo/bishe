<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchStudentExamDetail, submitStudentExam } from '../../api/student'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const examDetail = ref(null)
const answerMap = ref({})
const loadErrorMessage = ref('')

const readonlyMode = computed(() => examDetail.value?.examStatus === 'SUBMITTED')

const statusMap = {
  NOT_STARTED: '未作答',
  PENDING: '待完成',
  SUBMITTED: '已提交'
}

const questionTypeMap = {
  SINGLE_CHOICE: '单选题',
  SHORT_ANSWER: '简答题',
  TRUE_FALSE: '判断题'
}

function formatStatus(status) {
  return statusMap[status] || status
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ') : '--'
}

function formatQuestionType(value) {
  return questionTypeMap[value] || value
}

function isObjectiveQuestion(questionType) {
  return questionType === 'SINGLE_CHOICE' || questionType === 'TRUE_FALSE'
}

function formatOptionText(question, option) {
  if (question.questionType === 'TRUE_FALSE') {
    return option.content
  }
  return `${option.label}. ${option.content}`
}

function buildAnswerMap(questions = []) {
  const nextMap = {}
  questions.forEach((question) => {
    nextMap[question.questionId] = question.answerContent || ''
  })
  answerMap.value = nextMap
}

async function loadExamDetail() {
  const paperId = Number(route.params.paperId)
  if (!Number.isInteger(paperId) || paperId <= 0) {
    examDetail.value = null
    buildAnswerMap([])
    loadErrorMessage.value = '试卷参数无效，请返回列表后重新进入'
    return
  }

  loading.value = true
  try {
    loadErrorMessage.value = ''
    const result = await fetchStudentExamDetail(paperId)
    examDetail.value = result.data
    buildAnswerMap(result.data.questions)
  } catch (error) {
    examDetail.value = null
    buildAnswerMap([])
    loadErrorMessage.value = error?.response?.data?.message || error?.message || '试卷暂时不可用，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/student/exams')
}

async function handleSubmit() {
  try {
    await ElMessageBox.confirm('提交后本次答案将不能再次修改，是否继续？', '提交确认', {
      type: 'warning'
    })
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const payload = {
      answers: (examDetail.value?.questions || []).map((question) => ({
        questionId: question.questionId,
        answerContent: answerMap.value[question.questionId] || ''
      }))
    }
    const result = await submitStudentExam(Number(route.params.paperId), payload)
    ElMessage.success(
      `提交成功，客观题 ${result.data.autoScore} 分，主观题 ${result.data.subjectiveScore} 分，总分 ${result.data.totalScore} 分`
    )
    await loadExamDetail()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadExamDetail()
})
</script>

<template>
  <div class="exam-take-page" v-loading="loading">
    <el-card v-if="examDetail" class="exam-header" shadow="hover">
      <div class="header-top">
        <div>
          <h3>{{ examDetail.title }}</h3>
          <p>{{ examDetail.courseName }} / 卷面总分 {{ examDetail.paperTotalScore }} 分</p>
        </div>
        <div class="header-actions">
          <el-tag :type="readonlyMode ? 'success' : 'warning'">
            {{ formatStatus(examDetail.examStatus) }}
          </el-tag>
          <el-button plain @click="goBack">返回考试列表</el-button>
        </div>
      </div>
      <p v-if="examDetail.submittedAt" class="submitted-at">
        提交时间：{{ formatDateTime(examDetail.submittedAt) }}
      </p>
    </el-card>

    <el-empty v-else-if="!loading" :description="loadErrorMessage || '试卷不存在或暂不可用'">
      <el-button type="primary" @click="loadExamDetail">重新加载</el-button>
      <el-button @click="goBack">返回考试列表</el-button>
    </el-empty>

    <template v-if="examDetail">
      <el-card v-if="readonlyMode" class="score-summary" shadow="never">
        <div class="summary-grid">
          <div>
            <span>总分</span>
            <strong>{{ examDetail.studentScore ?? 0 }}</strong>
          </div>
          <div>
            <span>客观题</span>
            <strong>{{ examDetail.autoScore ?? 0 }}</strong>
          </div>
          <div>
            <span>主观题</span>
            <strong>{{ examDetail.subjectiveScore ?? 0 }}</strong>
          </div>
        </div>
      </el-card>

      <el-card
        v-for="question in examDetail.questions"
        :key="question.questionId"
        class="question-card"
        shadow="never"
      >
        <div class="question-title">
          <span>第 {{ question.questionOrder }} 题</span>
          <el-tag size="small" type="info">{{ formatQuestionType(question.questionType) }}</el-tag>
          <span>{{ question.score }} 分</span>
        </div>

        <p class="question-stem">{{ question.stem }}</p>

        <el-radio-group
          v-if="isObjectiveQuestion(question.questionType)"
          v-model="answerMap[question.questionId]"
          :disabled="readonlyMode"
          class="option-group"
        >
          <el-radio
            v-for="option in question.options"
            :key="`${question.questionId}-${option.label}`"
            :label="option.label"
            border
          >
            {{ formatOptionText(question, option) }}
          </el-radio>
        </el-radio-group>

        <el-input
          v-else
          v-model="answerMap[question.questionId]"
          :disabled="readonlyMode"
          type="textarea"
          :rows="4"
          placeholder="请输入你的答案"
        />

        <div v-if="readonlyMode" class="answer-feedback">
          <div class="feedback-row">
            <span>本题得分</span>
            <strong>{{ question.gainedScore ?? 0 }} / {{ question.score }}</strong>
          </div>
          <div class="feedback-row">
            <span>系统反馈</span>
            <span>{{ question.feedback || '暂无反馈' }}</span>
          </div>
        </div>
      </el-card>

      <div class="footer-actions">
        <el-button @click="goBack">返回考试列表</el-button>
        <el-button
          v-if="!readonlyMode"
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交试卷
        </el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.exam-take-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.exam-header h3 {
  margin: 0;
  font-size: 22px;
  color: #10345f;
}

.exam-header p {
  margin: 8px 0 0;
  color: #6b7280;
}

.header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.submitted-at {
  margin-top: 12px;
}

.question-card {
  border-radius: 16px;
}

.score-summary {
  border-radius: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.summary-grid div {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.summary-grid span {
  color: #6b7280;
}

.summary-grid strong {
  font-size: 24px;
  color: #10345f;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #4b5563;
  font-weight: 600;
}

.question-stem {
  margin: 16px 0;
  line-height: 1.7;
  color: #111827;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.answer-feedback {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feedback-row {
  display: flex;
  gap: 12px;
  line-height: 1.6;
}

.feedback-row span:first-child {
  min-width: 72px;
  color: #6b7280;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 960px) {
  .header-top {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
