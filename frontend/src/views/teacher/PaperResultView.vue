<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPaperResults } from '../../api/paper'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref({
  paperId: undefined,
  title: '',
  courseName: '',
  status: '',
  totalScore: 0,
  submittedCount: 0,
  averageScoreRate: 0,
  records: []
})

const paperStatusMap = {
  DRAFT: '草稿',
  PUBLISHED: '已发布'
}

const examRecordStatusMap = {
  PENDING: '待完成',
  SUBMITTED: '已提交'
}

function formatPercent(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ') : '--'
}

function formatPaperStatus(value) {
  return paperStatusMap[value] || value
}

function formatExamRecordStatus(value) {
  return examRecordStatusMap[value] || value
}

async function loadDetail() {
  loading.value = true
  try {
    const result = await fetchPaperResults(route.params.paperId)
    detail.value = result.data
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/teacher/papers')
}

function handleViewAnswer(row) {
  router.push(`/teacher/papers/${route.params.paperId}/results/${row.studentId}`)
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="paper-result-page" v-loading="loading">
    <el-card shadow="hover">
      <div class="header-row">
        <div>
          <h3>{{ detail.title }}</h3>
          <p>{{ detail.courseName }} / {{ formatPaperStatus(detail.status) }} / 卷面总分 {{ detail.totalScore }} 分</p>
        </div>
        <el-button plain @click="goBack">返回试卷列表</el-button>
      </div>
    </el-card>

    <div class="metric-row">
      <el-card shadow="hover">
        <div class="metric">
          <span>已提交人数</span>
          <strong>{{ detail.submittedCount }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>平均得分率</span>
          <strong>{{ formatPercent(detail.averageScoreRate) }}</strong>
        </div>
      </el-card>
    </div>

    <el-card shadow="hover">
      <template #header>学生成绩明细</template>
      <el-table :data="detail.records" empty-text="当前试卷还没有提交记录">
        <el-table-column prop="studentName" label="学生姓名" width="140" />
        <el-table-column label="总分" width="120">
          <template #default="{ row }">
            {{ row.totalScore }} / {{ detail.totalScore }}
          </template>
        </el-table-column>
        <el-table-column prop="autoScore" label="客观题得分" width="120" />
        <el-table-column prop="subjectiveScore" label="主观题得分" width="120" />
        <el-table-column label="得分率" width="120">
          <template #default="{ row }">
            {{ formatPercent(row.scoreRate) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            {{ formatExamRecordStatus(row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewAnswer(row)">
              查看答卷
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.paper-result-page {
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

@media (max-width: 960px) {
  .header-row {
    flex-direction: column;
  }

  .metric-row {
    grid-template-columns: 1fr;
  }
}
</style>
