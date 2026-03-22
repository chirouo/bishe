<script setup>
import { computed, onMounted, ref } from 'vue'
import ChartCard from '../../components/ChartCard.vue'
import { fetchStudentResultAnalysis } from '../../api/student'

const loading = ref(false)
const analysis = ref({
  completedExamCount: 0,
  averageScoreRate: 0,
  weakKnowledgePointCount: 0,
  suggestion: '',
  trends: [],
  knowledgePoints: []
})

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: analysis.value.trends.map((item) => item.paperTitle)
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: analysis.value.trends.map((item) => scoreRate(item)),
      itemStyle: {
        color: '#1b5fa6'
      },
      areaStyle: {
        color: 'rgba(27, 95, 166, 0.16)'
      }
    }
  ]
}))

function scoreRate(item) {
  if (!item || !item.paperTotalScore) {
    return 0
  }
  return Number(((item.score / item.paperTotalScore) * 100).toFixed(2))
}

function formatPercent(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ') : '--'
}

async function loadAnalysis() {
  loading.value = true
  try {
    const result = await fetchStudentResultAnalysis()
    analysis.value = result.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAnalysis()
})
</script>

<template>
  <div class="page-grid" v-loading="loading">
    <div class="metric-row">
      <el-card shadow="hover">
        <div class="metric">
          <span>已完成考试</span>
          <strong>{{ analysis.completedExamCount }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>平均得分率</span>
          <strong>{{ formatPercent(analysis.averageScoreRate) }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>薄弱知识点</span>
          <strong>{{ analysis.weakKnowledgePointCount }}</strong>
        </div>
      </el-card>
    </div>

    <ChartCard :option="trendOption" />

    <el-card shadow="hover">
      <template #header>知识点掌握度</template>
      <el-table :data="analysis.knowledgePoints" empty-text="暂无分析数据">
        <el-table-column prop="pointName" label="知识点" min-width="180" />
        <el-table-column label="掌握度" width="140">
          <template #default="{ row }">
            <el-tag :type="row.masteryRate < 60 ? 'danger' : row.masteryRate < 80 ? 'warning' : 'success'">
              {{ formatPercent(row.masteryRate) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover">
      <template #header>考试趋势</template>
      <el-table :data="analysis.trends" empty-text="暂无考试记录">
        <el-table-column prop="paperTitle" label="试卷名称" min-width="200" />
        <el-table-column label="得分" width="140">
          <template #default="{ row }">
            {{ row.score }} / {{ row.paperTotalScore }}
          </template>
        </el-table-column>
        <el-table-column label="得分率" width="140">
          <template #default="{ row }">
            {{ formatPercent(scoreRate(row)) }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submittedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover">
      <template #header>学习建议</template>
      <el-alert
        :title="analysis.suggestion || '当前还没有学习建议。'"
        :type="analysis.weakKnowledgePointCount > 0 ? 'warning' : 'success'"
        :closable="false"
        show-icon
      />
    </el-card>
  </div>
</template>

<style scoped>
.page-grid {
  display: grid;
  gap: 20px;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  .metric-row {
    grid-template-columns: 1fr;
  }
}
</style>
