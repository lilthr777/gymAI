<template>
  <div class="coach-detail" v-if="coach">
    <div class="page-nav">
      <button class="back-btn" @click="$router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
    </div>

    <div class="coach-hero">
      <el-avatar :size="96" icon="UserFilled" :src="coach.avatar" />
      <h2>{{ coach.name }}</h2>
      <button v-if="userStore.isLoggedIn()" class="fav-btn" :class="{ favorited }" @click="toggleFav">
        {{ favorited ? '♥' : '♡' }}
      </button>
      <div class="coach-tags">
        <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>

    <div class="coach-desc">
      <p>{{ coach.description || '暂无简介' }}</p>
    </div>

    <section class="section">
      <h3>该教练课程</h3>
      <div class="course-list">
        <CourseCard
          v-for="course in courses"
          :key="course.id"
          :course="course"
          @click="$router.push(`/courses/${course.id}`)"
        />
      </div>
      <el-empty v-if="!courses.length" description="暂无课程" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { coachApi, courseApi, favoriteApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Coach, Course } from '@/types'
import CourseCard from '@/components/CourseCard.vue'

const route = useRoute()
const userStore = useUserStore()
const coach = ref<Coach | null>(null)
const favorited = ref(false)

const toggleFav = async () => {
  if (!coach.value?.id) return
  try {
    const res = await favoriteApi.toggle(coach.value.id)
    favorited.value = res.data.favorited
  } catch { /* handled */ }
}

const checkFav = async (id: number) => {
  try {
    const res = await favoriteApi.ids()
    favorited.value = res.data.includes(id)
  } catch { /* handled */ }
}

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
    checkFav(id)
  } catch {
    // handled
  }
})
</script>

<style scoped lang="scss">
.coach-detail {
  padding: 24px 20px;
  max-width: 680px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border: none;
  background: none;
  color: $color-accent;
  font-size: $font-size-base;
  font-family: $font-family;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.coach-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 32px;
  position: relative;

  h2 {
    font-size: $font-size-2xl;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: $color-text-primary;
    margin: 20px 0 12px;
  }
}

.fav-btn {
  border: none;
  background: none;
  font-size: 24px;
  cursor: pointer;
  color: $color-border;
  position: absolute;
  top: 8px;
  right: 8px;
  transition: color $transition-fast;

  &:hover {
    color: $color-danger;
  }

  &.favorited {
    color: $color-danger;
  }
}

.coach-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.tag {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  background: $color-bg-secondary;
  padding: 4px 12px;
  border-radius: $radius-pill;
  font-weight: 500;
}

.coach-desc {
  padding: 24px;
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  margin-bottom: 32px;

  p {
    font-size: $font-size-base;
    color: $color-text-primary;
    line-height: 1.6;
  }
}

.section {
  h3 {
    font-size: $font-size-xl;
    font-weight: 600;
    letter-spacing: -0.01em;
    color: $color-text-primary;
    margin: 0 0 16px;
  }
}

.course-list {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  overflow: hidden;
}

html.dark {
  .coach-hero h2,
  .section h3 {
    color: $dark-text;
  }

  .coach-desc {
    background: $dark-bg-secondary;
    border-color: $dark-border;

    p {
      color: $dark-text;
    }
  }

  .tag {
    background: $dark-bg-secondary;
  }

  .course-list {
    background: $dark-bg;
    border-color: $dark-border;
  }
}
</style>
