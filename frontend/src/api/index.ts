import request from '@/utils/request'
import type { ApiResponse, PageResult, User, Coach, Course, Checkin, HomeData, LoginForm, RegisterForm, AuthResult, AdminDashboard, CoachScheduleItem, DashboardStats } from '@/types'

// 认证
export const authApi = {
  login: (data: LoginForm) => request.post<any, ApiResponse<AuthResult>>('/api/auth/login', data),
  register: (data: RegisterForm) => request.post<any, ApiResponse<AuthResult>>('/api/auth/register', data),
  me: () => request.get<any, ApiResponse<{ userId: string; username: string; role: string }>>('/api/auth/me'),
}

// 上传
export const uploadApi = {
  avatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<any, ApiResponse<string>>('/api/upload/avatar', formData)
  },
}

// 首页
export const homeApi = {
  get: () => request.get<any, ApiResponse<HomeData>>('/api/home'),
}

// 用户
export const userApi = {
  getProfile: () => request.get<any, ApiResponse<User>>('/api/user/profile'),
  updateProfile: (data: Partial<User>) => request.put<any, ApiResponse<null>>('/api/user/profile', data),
}

// 教练
export const coachApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string }) =>
    request.get<any, ApiResponse<PageResult<Coach>>>('/api/coaches', { params }),
  getById: (id: number) => request.get<any, ApiResponse<Coach>>(`/api/coaches/${id}`),
}

// 课程
export const courseApi = {
  list: (params: { pageNum: number; pageSize: number; keyword?: string; coachId?: number }) =>
    request.get<any, ApiResponse<PageResult<Course>>>('/api/courses', { params }),
  getById: (id: number) => request.get<any, ApiResponse<Course>>(`/api/courses/${id}`),
  register: (id: number) => request.post<any, ApiResponse<null>>(`/api/courses/${id}/register`),
  cancel: (id: number) => request.post<any, ApiResponse<null>>(`/api/courses/${id}/cancel`),
  myCourses: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResponse<PageResult<Course>>>('/api/courses/my', { params }),
}

// 签到
export const checkinApi = {
  checkin: (courseId: number) => request.post<any, ApiResponse<null>>('/api/checkins', { courseId }),
  myCheckins: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResponse<PageResult<Checkin>>>('/api/checkins/my', { params }),
}

// 评价
export const reviewApi = {
  list: (courseId: number, params?: any) => request.get<any, ApiResponse<PageResult<any>>>(`/api/reviews/course/${courseId}`, { params }),
  submit: (data: { courseId: number; rating: number; comment?: string }) => request.post<any, ApiResponse<any>>('/api/reviews', data),
}

// 收藏教练
export const favoriteApi = {
  toggle: (coachId: number) => request.post<any, ApiResponse<any>>(`/api/favorites/coach/${coachId}`),
  list: () => request.get<any, ApiResponse<any[]>>('/api/favorites/coaches'),
  ids: () => request.get<any, ApiResponse<number[]>>('/api/favorites/coach-ids'),
}

// 会员卡
export const cardApi = {
  info: () => request.get<any, ApiResponse<any>>('/api/card'),
  renew: (cardType: string) => request.post<any, ApiResponse<any>>('/api/card/renew', { cardType }),
}

// 管理员后台
export const adminApi = {
  dashboard: () => request.get<any, ApiResponse<AdminDashboard>>('/api/admin/dashboard'),
  stats: () => request.get<any, ApiResponse<DashboardStats>>('/api/admin/dashboard/stats'),
  users: (params: { pageNum: number; pageSize: number; keyword?: string; role?: string }) =>
    request.get<any, ApiResponse<PageResult<User>>>('/api/admin/users', { params }),
  updateUser: (id: number, data: Partial<User>) =>
    request.put<any, ApiResponse<null>>(`/api/admin/users/${id}`, data),
  deleteUser: (id: number) =>
    request.delete<any, ApiResponse<null>>(`/api/admin/users/${id}`),
  createCoach: (data: Partial<Coach>) => request.post<any, ApiResponse<null>>('/api/admin/coaches', data),
  updateCoach: (id: number, data: Partial<Coach>) =>
    request.put<any, ApiResponse<null>>(`/api/admin/coaches/${id}`, data),
  deleteCoach: (id: number) => request.delete<any, ApiResponse<null>>(`/api/admin/coaches/${id}`),
  adminCourses: (params: { pageNum: number; pageSize: number }) =>
    request.get<any, ApiResponse<PageResult<Course>>>('/api/admin/courses', { params }),
  createCourse: (data: Partial<Course>) => request.post<any, ApiResponse<null>>('/api/admin/courses', data),
  updateCourse: (id: number, data: Partial<Course>) =>
    request.put<any, ApiResponse<null>>(`/api/admin/courses/${id}`, data),
  deleteCourse: (id: number) => request.delete<any, ApiResponse<null>>(`/api/admin/courses/${id}`),
}

// 教练后台
export const coachBackendApi = {
  schedule: () => request.get<any, ApiResponse<CoachScheduleItem[]>>('/api/coach/schedule'),
  dashboard: () => request.get<any, ApiResponse<AdminDashboard>>('/api/coach/dashboard'),
}

// AI 聊天
export const aiChatApi = {
  chat: (message: string, signal?: AbortSignal) =>
    fetch(`${import.meta.env.VITE_AI_BASE_URL}/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message }),
      signal,
    }),
}
