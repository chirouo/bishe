<script setup>
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  option: {
    type: Object,
    required: true
  },
  height: {
    type: String,
    default: '320px'
  }
})

const chartRef = ref(null)
let chartInstance

function renderChart() {
  if (!chartRef.value) {
    return
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(props.option)
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', renderChart)
})

watch(
  () => props.option,
  () => {
    renderChart()
  },
  { deep: true }
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart)
  chartInstance?.dispose()
})
</script>

<template>
  <el-card shadow="hover">
    <div ref="chartRef" :style="{ height }"></div>
  </el-card>
</template>

