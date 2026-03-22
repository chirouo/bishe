<script setup>
import { computed, onMounted, ref } from 'vue'
import ChartCard from '../../components/ChartCard.vue'
import { fetchTeacherStatisticsAnalysis } from '../../api/teacher'

const loading = ref(false)
const analysis = ref({
  submittedExamCount: 0,
  studentCount: 0,
  averageScoreRate: 0,
  weakKnowledgePointCount: 0,
  suggestion: '',
  papers: [],
  knowledgePoints: []
})

function buildEmptyChartOption(text) {
  return {
    title: {
      text,
      left: 'center',
      top: 'middle',
      textStyle: {
        color: '#94a3b8',
        fontSize: 16,
        fontWeight: 500
      }
    }
  }
}

const paperChartOption = computed(() => {
  if (!analysis.value.papers.length) {
    return buildEmptyChartOption('暂无试卷提交数据')
  }

  return {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: analysis.value.papers.map((item) => item.paperTitle)
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: [
      {
        type: 'bar',
        data: analysis.value.papers.map((item) => Number(item.averageScoreRate || 0)),
        itemStyle: {
          color: '#1b5fa6'
        }
      }
    ]
  }
})

const radarChartOption = computed(() => {
  if (!analysis.value.knowledgePoints.length) {
    return buildEmptyChartOption('暂无知识点掌握度数据')
  }

  return {
    tooltip: { trigger: 'item' },
    radar: {
      indicator: analysis.value.knowledgePoints.map((item) => ({
        name: item.pointName,
        max: 100
      }))
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: analysis.value.knowledgePoints.map((item) => Number(item.masteryRate || 0)),
            name: '班级掌握度'
          }
        ]
      }
    ]
  }
})

function formatPercent(value) {
  return `${Number(value || 0).toFixed(2)}%`
}

async function loadStatistics() {
  loading.value = true
  try {
    const result = await fetchTeacherStatisticsAnalysis()
    analysis.value = result.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div class="page-grid" v-loading="loading">
    <div class="metric-row">
      <el-card shadow="hover">
        <div class="metric">
          <span>已提交记录</span>
          <strong>{{ analysis.submittedExamCount }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>参与学生</span>
          <strong>{{ analysis.studentCount }}</strong>
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

    <ChartCard :option="paperChartOption" />
    <ChartCard :option="radarChartOption" />

    <el-card shadow="hover">
      <template #header>试卷提交情况</template>
      <el-table :data="analysis.papers" empty-text="暂无提交数据">
        <el-table-column prop="paperTitle" label="试卷名称" min-width="220" />
        <el-table-column prop="submittedCount" label="提交人数" width="120" />
        <el-table-column label="平均得分率" width="160">
          <template #default="{ row }">
            {{ formatPercent(row.averageScoreRate) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover">
      <template #header>当前分析建议</template>
      <el-alert
        :title="analysis.suggestion || '当前暂无统计建议。'"
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
  grid-template-columns: repeat(4, 1fr);
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
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
