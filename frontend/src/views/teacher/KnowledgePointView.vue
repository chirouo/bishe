<script setup>
import { onMounted, ref } from 'vue'
import { fetchCourses, fetchKnowledgePoints } from '../../api/teacher'

const loading = ref(false)
const courseLoading = ref(false)
const courses = ref([])
const selectedCourseId = ref()
const knowledgePoints = ref([])

const difficultyMap = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

async function loadCourses() {
  courseLoading.value = true
  try {
    const result = await fetchCourses()
    courses.value = result.data
    if (!selectedCourseId.value && courses.value.length > 0) {
      selectedCourseId.value = courses.value[0].id
    }
  } finally {
    courseLoading.value = false
  }
}

async function loadKnowledgePoints() {
  loading.value = true
  try {
    const result = await fetchKnowledgePoints(selectedCourseId.value)
    knowledgePoints.value = result.data
  } finally {
    loading.value = false
  }
}

function formatDifficulty(difficulty) {
  return difficultyMap[difficulty] || difficulty
}

async function handleCourseChange() {
  await loadKnowledgePoints()
}

onMounted(async () => {
  await loadCourses()
  await loadKnowledgePoints()
})
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <div class="header-row">
        <span>知识点管理</span>
        <el-select
          v-model="selectedCourseId"
          placeholder="请选择课程"
          clearable
          filterable
          :loading="courseLoading"
          style="width: 260px"
          @change="handleCourseChange"
        >
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.courseName"
            :value="course.id"
          />
        </el-select>
      </div>
    </template>

    <el-table v-loading="loading" :data="knowledgePoints">
      <el-table-column prop="courseName" label="所属课程" width="180" />
      <el-table-column prop="pointName" label="知识点名称" width="180" />
      <el-table-column label="默认难度" width="120">
        <template #default="{ row }">
          {{ formatDifficulty(row.difficulty) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
    </el-table>
  </el-card>
</template>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

@media (max-width: 960px) {
  .header-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
