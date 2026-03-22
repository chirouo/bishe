<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCourses, fetchKnowledgePoints } from '../../api/teacher'
import { createSingleChoiceQuestion, fetchQuestions, generateAiQuestionDraft } from '../../api/question'
import { fetchAiSettings, switchAiModel } from '../../api/ai'

const loading = ref(false)
const submitLoading = ref(false)
const generateLoading = ref(false)
const switchModelLoading = ref(false)
const confirmedModel = ref('')
const dialogVisible = ref(false)
const courses = ref([])
const filterKnowledgePoints = ref([])
const formKnowledgePoints = ref([])
const questions = ref([])
const aiSettings = reactive({
  provider: '',
  currentModel: '',
  availableModels: []
})

const filters = reactive({
  courseId: undefined,
  knowledgePointId: undefined,
  questionType: ''
})

const createForm = reactive({
  courseId: undefined,
  knowledgePointId: undefined,
  questionType: 'SINGLE_CHOICE',
  stem: '',
  difficulty: 'EASY',
  answer: 'A',
  analysis: '',
  source: 'MANUAL',
  aiRequirements: '',
  options: [
    { label: 'A', content: '' },
    { label: 'B', content: '' },
    { label: 'C', content: '' },
    { label: 'D', content: '' }
  ]
})

const difficultyMap = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

const questionTypeMap = {
  SINGLE_CHOICE: '单选题',
  SHORT_ANSWER: '简答题'
}

const sourceMap = {
  MANUAL: '手动录入',
  AI_MOCK: 'AI草稿(Mock)',
  AI_GENERATED: 'AI生成'
}

async function loadCourses() {
  const result = await fetchCourses()
  courses.value = result.data
}

async function loadFilterKnowledgePoints() {
  const result = await fetchKnowledgePoints(filters.courseId)
  filterKnowledgePoints.value = result.data
  if (
    filters.knowledgePointId &&
    !filterKnowledgePoints.value.some((item) => item.id === filters.knowledgePointId)
  ) {
    filters.knowledgePointId = undefined
  }
}

async function loadFormKnowledgePoints() {
  const result = await fetchKnowledgePoints(createForm.courseId)
  formKnowledgePoints.value = result.data
  if (
    createForm.knowledgePointId &&
    !formKnowledgePoints.value.some((item) => item.id === createForm.knowledgePointId)
  ) {
    createForm.knowledgePointId = undefined
  }
}

async function loadQuestions() {
  loading.value = true
  try {
    const result = await fetchQuestions({
      courseId: filters.courseId,
      knowledgePointId: filters.knowledgePointId,
      questionType: filters.questionType || undefined
    })
    questions.value = result.data
  } finally {
    loading.value = false
  }
}

async function loadAiSettings() {
  const result = await fetchAiSettings()
  aiSettings.provider = result.data.provider
  aiSettings.currentModel = result.data.currentModel
  aiSettings.availableModels = result.data.availableModels || []
  confirmedModel.value = result.data.currentModel || ''
}

async function handleFilterCourseChange() {
  await loadFilterKnowledgePoints()
  await loadQuestions()
}

async function handleCreateCourseChange() {
  await loadFormKnowledgePoints()
}

function resetCreateForm() {
  createForm.courseId = courses.value[0]?.id
  createForm.knowledgePointId = undefined
  createForm.questionType = 'SINGLE_CHOICE'
  createForm.stem = ''
  createForm.difficulty = 'EASY'
  createForm.answer = 'A'
  createForm.analysis = ''
  createForm.source = 'MANUAL'
  createForm.aiRequirements = ''
  createForm.options = [
    { label: 'A', content: '' },
    { label: 'B', content: '' },
    { label: 'C', content: '' },
    { label: 'D', content: '' }
  ]
}

async function openCreateDialog() {
  resetCreateForm()
  dialogVisible.value = true
  if (createForm.courseId) {
    await loadFormKnowledgePoints()
  }
}

async function submitCreateForm() {
  submitLoading.value = true
  try {
    await createSingleChoiceQuestion(createForm)
    ElMessage.success('题目新增成功')
    dialogVisible.value = false
    await loadQuestions()
  } finally {
    submitLoading.value = false
  }
}

async function handleGenerateDraft() {
  generateLoading.value = true
  try {
    const result = await generateAiQuestionDraft({
      courseId: createForm.courseId,
      knowledgePointId: createForm.knowledgePointId,
      questionType: createForm.questionType,
      difficulty: createForm.difficulty,
      requirements: createForm.aiRequirements
    })

    createForm.stem = result.data.stem
    createForm.answer = result.data.answer
    createForm.analysis = result.data.analysis
    createForm.source = result.data.source
    createForm.options = (result.data.options || []).map((item) => ({
      label: item.label,
      content: item.content
    }))
    ElMessage.success(`AI 草稿已生成，当前模型：${aiSettings.currentModel || '未设置'}`)
  } finally {
    generateLoading.value = false
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

function formatDifficulty(value) {
  return difficultyMap[value] || value
}

function formatQuestionType(value) {
  return questionTypeMap[value] || value
}

function formatSource(value) {
  return sourceMap[value] || value
}

onMounted(async () => {
  await loadCourses()
  await loadAiSettings()
  if (courses.value.length > 0) {
    filters.courseId = courses.value[0].id
    await loadFilterKnowledgePoints()
  }
  await loadQuestions()
})
</script>

<template>
  <div class="page-grid">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <span>题库管理</span>
          <el-button type="primary" @click="openCreateDialog">新增单选题</el-button>
        </div>
      </template>

      <div class="filter-row">
        <el-select v-model="filters.courseId" placeholder="按课程筛选" clearable @change="handleFilterCourseChange">
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
        <el-select
          v-model="filters.knowledgePointId"
          placeholder="按知识点筛选"
          clearable
          @change="loadQuestions"
        >
          <el-option
            v-for="item in filterKnowledgePoints"
            :key="item.id"
            :label="item.pointName"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="filters.questionType" placeholder="按题型筛选" clearable @change="loadQuestions">
          <el-option label="单选题" value="SINGLE_CHOICE" />
          <el-option label="简答题" value="SHORT_ANSWER" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="questions">
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="knowledgePointName" label="知识点" width="160" />
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            {{ formatQuestionType(row.questionType) }}
          </template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            {{ formatDifficulty(row.difficulty) }}
          </template>
        </el-table-column>
        <el-table-column prop="stem" label="题干" min-width="260" />
        <el-table-column label="选项" min-width="240">
          <template #default="{ row }">
            <div class="option-list">
              <span v-for="item in row.options" :key="`${row.id}-${item.label}`">
                {{ item.label }}. {{ item.content }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" width="80" />
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            {{ formatSource(row.source) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增单选题" width="720px">
      <el-form label-width="110px">
        <el-form-item label="课程">
          <el-select v-model="createForm.courseId" placeholder="请选择课程" @change="handleCreateCourseChange">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-select v-model="createForm.knowledgePointId" placeholder="请选择知识点">
            <el-option
              v-for="item in formKnowledgePoints"
              :key="item.id"
              :label="item.pointName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题干">
          <el-input v-model="createForm.stem" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="createForm.difficulty">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="AI补充要求">
          <el-input
            v-model="createForm.aiRequirements"
            type="textarea"
            :rows="2"
            placeholder="可选，例如：强调定义辨析、避免过难、贴近阶段测试风格"
          />
        </el-form-item>
        <el-form-item label="AI模型">
          <el-select
            v-model="aiSettings.currentModel"
            placeholder="请选择模型"
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
        </el-form-item>
        <el-form-item label="AI草稿">
          <div class="ai-action-row">
            <el-button type="success" plain :loading="generateLoading" @click="handleGenerateDraft">
              AI生成草稿
            </el-button>
            <span class="ai-tip">当前 provider：{{ aiSettings.provider }}，当前模型：{{ aiSettings.currentModel || '未设置' }}</span>
          </div>
        </el-form-item>
        <el-form-item label="正确答案">
          <el-select v-model="createForm.answer">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目解析">
          <el-input v-model="createForm.analysis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="选项">
          <div class="option-form-list">
            <div v-for="option in createForm.options" :key="option.label" class="option-form-item">
              <span class="option-label">{{ option.label }}</span>
              <el-input v-model="option.content" :placeholder="`请输入选项 ${option.label}`" />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreateForm">保存题目</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-grid {
  display: grid;
  gap: 20px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-form-list {
  width: 100%;
  display: grid;
  gap: 12px;
}

.option-form-item {
  display: grid;
  grid-template-columns: 28px 1fr;
  align-items: center;
  gap: 10px;
}

.option-label {
  font-weight: 600;
  color: #10345f;
}

.ai-action-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-tip {
  color: #6b7280;
  font-size: 13px;
}

@media (max-width: 960px) {
  .header-row {
    flex-direction: column;
    align-items: stretch;
  }

  .ai-action-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
