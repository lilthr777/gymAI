import request from '@/utils/request'
import type { ApiResponse, PageResult, Member, Coach, Course, Checkin, DashboardStats, LoginForm, LoginResult } from '@/types'

// 认证
export const authApi = {
  login: (data: LoginForm) => request.post<any, ApiResponse<LoginResult>>('/api/auth/login', data),
}

// 会员
export const memberApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string }) =>
    request.get<any, ApiResponse<PageResult<Member>>>('/api/members', { params }),
  getById: (id: number) => request.get<any, ApiResponse<Member>>(`/api/members/${id}`),
  create: (data: Member) => request.post<any, ApiResponse<null>>('/api/members', data),
  update: (id: number, data: Member) => request.put<any, ApiResponse<null>>(`/api/members/${id}`, data),
  delete: (id: number) => request.delete<any, ApiResponse<null>>(`/api/members/${id}`),
}

// 教练
export const coachApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string }) =>
    request.get<any, ApiResponse<PageResult<Coach>>>('/api/coaches', { params }),
  getById: (id: number) => request.get<any, ApiResponse<Coach>>(`/api/coaches/${id}`),
  create: (data: Coach) => request.post<any, ApiResponse<null>>('/api/coaches', data),
  update: (id: number, data: Coach) => request.put<any, ApiResponse<null>>(`/api/coaches/${id}`, data),
  delete: (id: number) => request.delete<any, ApiResponse<null>>(`/api/coaches/${id}`),
}

// 课程
export const courseApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string }) =>
    request.get<any, ApiResponse<PageResult<Course>>>('/api/courses', { params }),
  getById: (id: number) => request.get<any, ApiResponse<Course>>(`/api/courses/${id}`),
  create: (data: Course) => request.post<any, ApiResponse<null>>('/api/courses', data),
  update: (id: number, data: Course) => request.put<any, ApiResponse<null>>(`/api/courses/${id}`, data),
  delete: (id: number) => request.delete<any, ApiResponse<null>>(`/api/courses/${id}`),
}

// 签到
export const checkinApi = {
  list: (params: { pageNum: number; pageSize: number; memberId?: number; courseId?: number }) =>
    request.get<any, ApiResponse<PageResult<Checkin>>>('/api/checkins', { params }),
  checkin: (data: Checkin) => request.post<any, ApiResponse<null>>('/api/checkins', data),
}

// 仪表盘
export const dashboardApi = {
  stats: () => request.get<any, ApiResponse<DashboardStats>>('/api/dashboard/stats'),
  memberGrowth: () => request.get<any, ApiResponse<{ month: string; count: number }[]>>('/api/dashboard/member-growth'),
  courseTypeDistribution: () =>
    request.get<any, ApiResponse<{ name: string; value: number }[]>>('/api/dashboard/course-type-distribution'),
  checkinWeekly: () =>
    request.get<any, ApiResponse<{ day: string; count: number }[]>>('/api/dashboard/checkin-weekly'),
}

// AI 聊天（直接 fetch，不走 axios 拦截器）
export const aiChatApi = {
  chat: (message: string, signal?: AbortSignal) =>
    fetch(`${import.meta.env.VITE_AI_BASE_URL}/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message }),
      signal,
    }),
}
