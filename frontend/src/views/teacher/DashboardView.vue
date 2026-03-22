<script setup>
import { onMounted, reactive, ref } from 'vue'
import ChartCard from '../../components/ChartCard.vue'
import { fetchTeacherDashboardOverview } from '../../api/teacher'

const chartOption = {
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: ['第1周', '第2周', '第3周', '第4周']
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100
  },
  series: [
    {
      name: '班级平均分',
      type: 'line',
      smooth: true,
      data: [68, 74, 79, 82],
      areaStyle: {}
    }
  ]
}

const loading = ref(false)
const overview = reactive({
  courseCount: 0,
  knowledgePointCount: 0,
  questionCount: 0,
  publishedPaperCount: 0
})

async function loadOverview() {
  loading.value = true
  try {
    const result = await fetchTeacherDashboardOverview()
    Object.assign(overview, result.data)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <div v-loading="loading" class="page-grid">
    <div class="metric-row">
      <el-card shadow="hover">
        <div class="metric">
          <span>课程数</span>
          <strong>{{ overview.courseCount }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>知识点数</span>
          <strong>{{ overview.knowledgePointCount }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>题库题目</span>
          <strong>{{ overview.questionCount }}</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>已发布试卷</span>
          <strong>{{ overview.publishedPaperCount }}</strong>
        </div>
      </el-card>
    </div>

    <ChartCard :option="chartOption" />

    <el-card shadow="hover">
      <template #header>当前阶段开发建议</template>
      <el-timeline>
        <el-timeline-item timestamp="第 1 阶段">完善知识点管理与题库录入</el-timeline-item>
        <el-timeline-item timestamp="第 2 阶段">实现试卷创建、考试与自动判分</el-timeline-item>
        <el-timeline-item timestamp="第 3 阶段">接入大模型组卷与主观题评阅</el-timeline-item>
      </el-timeline>
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
  font-size: 30px;
  color: #10345f;
}

@media (max-width: 960px) {
  .metric-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
