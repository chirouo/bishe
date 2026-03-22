<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchPapers, createPaper, generateAiPaperDraft, publishPaper } from '../../api/paper'
import { fetchCourses } from '../../api/teacher'
import { fetchQuestions } from '../../api/question'
import { fetchAiSettings, switchAiModel } from '../../api/ai'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const aiLoading = ref(false)
const switchModelLoading = ref(false)
const publishingPaperId = ref()
const confirmedModel = ref('')
const papers = ref([])
const courses = ref([])
const availableQuestions = ref([])
const aiSettings = reactive({
  provider: '',
  currentModel: '',
  availableModels: []
})

const filters = reactive({
  courseId: undefined
})

const form = reactive({
  courseId: undefined,
  title: '',
  description: '',
  status: 'DRAFT',
  aiQuestionCount: 2,
  aiScorePerQuestion: 10,
  aiRequirements: '',
  aiStrategySummary: '',
  questions: []
})

const questionTypeMap = {
  SINGLE_CHOICE: '单选题',
  SHORT_ANSWER: '简答题'
}

const paperStatusMap = {
  DRAFT: '草稿',
  PUBLISHED: '已发布'
}

const totalScore = computed(() =>
  form.questions
    .filter((item) => item.selected)
    .reduce((sum, item) => sum + Number(item.score || 0), 0)
)

async function loadCourses() {
  const result = await fetchCourses()
  courses.value = result.data
  if (!filters.courseId && courses.value.length > 0) {
    filters.courseId = courses.value[0].id
  }
  if (!form.courseId && courses.value.length > 0) {
    form.courseId = courses.value[0].id
  }
}

async function loadPapers() {
  loading.value = true
  try {
    const result = await fetchPapers(filters.courseId)
    papers.value = result.data
  } finally {
    loading.value = false
  }
}

async function loadAvailableQuestions() {
  if (!form.courseId) {
    availableQuestions.value = []
    form.questions = []
    return
  }
  const result = await fetchQuestions({ courseId: form.courseId })
  availableQuestions.value = result.data
  form.questions = availableQuestions.value.map((item) => ({
    questionId: item.id,
    stem: item.stem,
    knowledgePointName: item.knowledgePointName,
    questionType: item.questionType,
    selected: false,
    score: 10
  }))
}

async function loadAiSettings() {
  const result = await fetchAiSettings()
  aiSettings.provider = result.data.provider
  aiSettings.currentModel = result.data.currentModel
  aiSettings.availableModels = result.data.availableModels || []
  confirmedModel.value = result.data.currentModel || ''
}

function formatQuestionType(value) {
  return questionTypeMap[value] || value
}

function formatPaperStatus(value) {
  return paperStatusMap[value] || value
}

function paperStatusTagType(value) {
  if (value === 'PUBLISHED') {
    return 'success'
  }
  if (value === 'DRAFT') {
    return 'warning'
  }
  return 'info'
}

function handleViewResults(row) {
  router.push(`/teacher/papers/${row.id}/results`)
}

async function handleFilterCourseChange() {
  await loadPapers()
}

async function handleFormCourseChange() {
  await loadAvailableQuestions()
}

async function handleGenerateAiDraft() {
  if (!form.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  if (form.questions.length === 0) {
    await loadAvailableQuestions()
  }

  aiLoading.value = true
  try {
    const result = await generateAiPaperDraft({
      courseId: form.courseId,
      questionCount: form.aiQuestionCount,
      scorePerQuestion: form.aiScorePerQuestion,
      requirements: form.aiRequirements
    })

    form.title = result.data.title
    form.description = result.data.description
    form.aiStrategySummary = result.data.strategySummary

    const selectedQuestionMap = new Map(
      (result.data.questions || []).map((item) => [item.questionId, Number(item.recommendedScore || 0)])
    )

    form.questions = form.questions.map((item) => ({
      ...item,
      selected: selectedQuestionMap.has(item.questionId),
      score: selectedQuestionMap.has(item.questionId)
        ? selectedQuestionMap.get(item.questionId)
        : item.score
    }))

    ElMessage.success(`AI 组卷草稿已生成，当前模型：${aiSettings.currentModel || '未设置'}`)
  } finally {
    aiLoading.value = false
  }
}

async function handleModelChange(model) {
  switchModelLoading.value = true
  try {
    const result = await switchAiModel(model)
    aiSettings.provider = result.data.provider
    aiSettings.currentModel = result.data.currentModel
    aiSettings.availableModels = result.data.availableModels || []
    confirmedModel.value = result.data.currentModel || ''
    ElMessage.success(`已切换到模型：${result.data.currentModel}`)
  } catch (error) {
    aiSettings.currentModel = confirmedModel.value
    throw error
  } finally {
    switchModelLoading.value = false
  }
}

async function handlePublishPaper(row) {
  try {
    await ElMessageBox.confirm(
      `发布后学生即可在“我的考试”中看到《${row.title}》，是否继续？`,
      '发布确认',
      { type: 'warning' }
    )
  } catch (error) {
    return
  }

  publishingPaperId.value = row.id
  try {
    await publishPaper(row.id)
    ElMessage.success('试卷已发布')
    await loadPapers()
  } finally {
    publishingPaperId.value = undefined
  }
}

async function submitPaper() {
  const selectedQuestions = form.questions
    .filter((item) => item.selected)
    .map((item) => ({
      questionId: item.questionId,
      score: item.score
    }))

  if (selectedQuestions.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }

  submitLoading.value = true
  try {
    await createPaper({
      courseId: form.courseId,
      title: form.title,
      description: form.description,
      status: form.status,
      questions: selectedQuestions
    })
    ElMessage.success('试卷创建成功')
    form.title = ''
    form.description = ''
    form.status = 'DRAFT'
    form.aiRequirements = ''
    form.aiStrategySummary = ''
    await loadAvailableQuestions()
    await loadPapers()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadCourses()
  await loadAiSettings()
  await loadPapers()
  await loadAvailableQuestions()
})
</script>

<template>
  <div class="paper-page">
    <el-card shadow="hover">
      <template #header>手动组卷</template>
      <el-form label-width="110px">
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" @change="handleFormCourseChange">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="试卷标题">
          <el-input v-model="form.title" placeholder="请输入试卷标题" />
        </el-form-item>
        <el-form-item label="试卷说明">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可填写本次测试说明" />
        </el-form-item>
        <el-form-item label="AI组卷参数">
          <div class="ai-panel">
            <div class="ai-config-row">
              <div class="ai-field">
                <span class="ai-field-label">题目数量</span>
                <el-input-number v-model="form.aiQuestionCount" :min="1" :max="20" />
              </div>
              <div class="ai-field">
                <span class="ai-field-label">每题分值</span>
                <el-input-number v-model="form.aiScorePerQuestion" :min="1" :max="100" />
              </div>
              <div class="ai-field ai-field-model">
                <span class="ai-field-label">使用模型</span>
                <el-select
                  v-model="aiSettings.currentModel"
                  placeholder="请选择模型"
                  style="width: 220px"
                  :loading="switchModelLoading"
                  @change="handleModelChange"
                >
                  <el-option
                    v-for="item in aiSettings.availableModels"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </div>
              <el-button type="success" plain :loading="aiLoading" @click="handleGenerateAiDraft">
                AI生成组卷草稿
              </el-button>
            </div>
            <el-input
              v-model="form.aiRequirements"
              type="textarea"
              :rows="2"
              placeholder="可选，例如：优先覆盖薄弱知识点、适合阶段测验、控制整体难度"
            />
            <p class="ai-tip">依次设置题目数量、每题分值和 AI 模型；如果不写补充要求，系统会按默认阶段测试策略生成草稿。</p>
            <p class="ai-tip">当前 provider：{{ aiSettings.provider }}，当前模型：{{ aiSettings.currentModel || '未设置' }}</p>
            <el-alert
              v-if="form.aiStrategySummary"
              :title="form.aiStrategySummary"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </el-form-item>
        <el-form-item label="保存状态">
          <el-select v-model="form.status">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="发布" value="PUBLISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择题目">
          <div class="selection-panel">
            <div v-for="item in form.questions" :key="item.questionId" class="selection-item">
              <el-checkbox v-model="item.selected">
                {{ item.knowledgePointName }} / {{ formatQuestionType(item.questionType) }}
              </el-checkbox>
              <div class="selection-stem">{{ item.stem }}</div>
              <div class="selection-score">
                <span class="score-label">本题分值</span>
                <el-input-number v-model="item.score" :min="1" :max="100" />
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="总分">
          <el-tag type="success">{{ totalScore }}</el-tag>
        </el-form-item>
        <el-button type="primary" :loading="submitLoading" @click="submitPaper">保存试卷</el-button>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <span>试卷列表</span>
          <el-select v-model="filters.courseId" placeholder="按课程筛选" clearable style="width: 240px" @change="handleFilterCourseChange">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </div>
      </template>
      <el-table v-loading="loading" :data="papers">
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="title" label="试卷标题" width="220" />
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="paperStatusTagType(row.status)">
              {{ formatPaperStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-row">
              <el-button
                v-if="row.status === 'DRAFT'"
                link
                type="primary"
                :loading="publishingPaperId === row.id"
                @click="handlePublishPaper(row)"
              >
                发布试卷
              </el-button>
              <el-button
                v-if="row.status === 'PUBLISHED'"
                link
                type="primary"
                @click="handleViewResults(row)"
              >
                查看成绩
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover">
      <template #header>当前说明</template>
      <p>当前页面同时支持手动组卷和 AI 辅助生成草稿。</p>
      <p>AI 组卷参数中的三个控件依次表示题目数量、每题分值和当前使用模型。</p>
      <p>保存为草稿后，可以在下方试卷列表里点击“发布试卷”。</p>
      <p>AI 草稿会优先参考班级薄弱知识点，但教师仍然可以继续修改选题、分值和发布状态。</p>
    </el-card>
  </div>
</template>

<style scoped>
.paper-page {
  display: grid;
  gap: 20px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.selection-panel {
  width: 100%;
  display: grid;
  gap: 12px;
}

.ai-panel {
  width: 100%;
  display: grid;
  gap: 12px;
}

.ai-config-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ai-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-field-model {
  min-width: 320px;
}

.ai-field-label {
  color: #4b5563;
  font-size: 14px;
  white-space: nowrap;
}

.ai-tip {
  margin: 0;
  color: #6b7280;
  line-height: 1.6;
}

.selection-item {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

.selection-stem {
  color: #4b5563;
  line-height: 1.6;
}

.selection-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-row {
  display: flex;
  align-items: center;
}

.score-label {
  color: #4b5563;
  font-size: 14px;
}

@media (max-width: 960px) {
  .header-row {
    flex-direction: column;
    align-items: stretch;
  }

  .ai-config-row {
    align-items: stretch;
  }

  .ai-field,
  .selection-score {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
