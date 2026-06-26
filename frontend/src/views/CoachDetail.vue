<template>
  <div class="coach-detail" v-if="coach">
    <div class="page-nav">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
    </div>
    <div class="coach-hero">
      <el-avatar :size="72" icon="UserFilled" :src="coach.avatar" />
      <h2>{{ coach.name }}</h2>
      <div class="coach-tags">
        <el-tag v-for="tag in tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
      </div>
    </div>

    <div class="coach-desc">
      <p>{{ coach.description || '暂无简介' }}</p>
    </div>

    <section class="section">
      <h3>该教练课程</h3>
      <CourseCard
        v-for="course in courses"
        :key="course.id"
        :course="course"
        @click="$router.push(`/courses/${course.id}`)"
      />
      <el-empty v-if="!courses.length" description="暂无课程" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { coachApi, courseApi } from '@/api'
import type { Coach, Course } from '@/types'
import CourseCard from '@/components/CourseCard.vue'

const route = useRoute()
const coach = ref<Coach | null>(null)
const courses = ref<Course[]>([])

const tags = computed(() => {
  if (!coach.value?.specialty) return []
  return coach.value.specialty.split(',').filter(Boolean)
})

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const [coachRes, courseRes] = await Promise.all([
      coachApi.getById(id),
      courseApi.list({ pageNum: 1, pageSize: 20, coachId: id }),
    ])
    coach.value = coachRes.data
    courses.value = courseRes.data.records
  } catch {
    // handled
  }
})
</script>

<style scoped lang="scss">
.coach-detail {
  padding: 16px;
}

.page-nav {
  margin-bottom: 12px;
}

.coach-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;

  h2 {
    font-size: 22px;
    font-weight: 600;
    color: $color-carbon;
    margin: 12px 0 8px;
  }
}

.coach-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: center;
}

.coach-desc {
  padding: 16px;
  background: $color-sheet;
  border-radius: $radius-lg;
  margin-bottom: 24px;

  p {
    font-size: 14px;
    color: $color-ash;
    line-height: 1.6;
    margin: 0;
  }
}

.section {
  h3 {
    font-size: 17px;
    font-weight: 600;
    color: $color-carbon;
    margin: 0 0 12px;
  }
}

html.dark {
  .coach-hero h2,
  .section h3 {
    color: $dark-text;
  }

  .coach-desc {
    background: $dark-bg-card;
  }

  .coach-desc p {
    color: $dark-text-secondary;
  }
}
</style>
