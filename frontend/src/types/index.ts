// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 管理员
export interface Admin {
  id: number
  username: string
  nickname: string
  avatar: string
  role: string
}

// 会员
export interface Member {
  id?: number
  name: string
  phone: string
  gender: number
  cardType: string
  cardStartDate?: string
  cardEndDate?: string
  status: number
  remark?: string
  createdAt?: string
  updatedAt?: string
}

// 教练
export interface Coach {
  id?: number
  name: string
  phone: string
  gender: number
  specialty: string
  description: string
  avatar: string
  status: number
  createdAt?: string
  updatedAt?: string
}

// 课程
export interface Course {
  id?: number
  name: string
  coachId: number
  courseDate: string
  startTime: string
  endTime: string
  maxCapacity: number
  currentCount: number
  description: string
  status: number
  createdAt?: string
  updatedAt?: string
}

// 签到记录
export interface Checkin {
  id?: number
  memberId: number
  courseId: number
  checkinTime?: string
  status: number
  remark?: string
}

// 仪表盘统计
export interface DashboardStats {
  totalMembers: number
  totalCoaches: number
  totalCourses: number
  todayCheckins: number
}

// 图表数据
export interface ChartDataItem {
  name: string
  value: number
  [key: string]: any
}

// 登录请求
export interface LoginForm {
  username: string
  password: string
}

// 登录响应
export interface LoginResult {
  token: string
  nickname: string
  avatar: string
  role: string
}

// 聊天消息
export interface ChatMessage {
  id?: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

// 路由 meta
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
  }
}

export {}
