<script setup>
import ChartCard from '../../components/ChartCard.vue'

const chartOption = {
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: ['测试一', '测试二', '测试三']
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100
  },
  series: [
    {
      type: 'bar',
      data: [72, 78, 84],
      itemStyle: {
        color: '#1b5fa6'
      }
    }
  ]
}
</script>

<template>
  <div class="page-grid">
    <div class="metric-row">
      <el-card shadow="hover">
        <div class="metric">
          <span>已完成考试</span>
          <strong>3</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>当前平均分</span>
          <strong>78</strong>
        </div>
      </el-card>
      <el-card shadow="hover">
        <div class="metric">
          <span>薄弱知识点</span>
          <strong>2</strong>
        </div>
      </el-card>
    </div>
    <ChartCard :option="chartOption" />
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

