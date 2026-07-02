// 通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 用户
export interface User {
  id?: number
  username: string
  phone: string
  nickname: string
  avatar: string
  gender: number
  role?: string
  cardType?: string
  cardStartDate?: string
  cardEndDate?: string
  createdAt?: string
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
  coachName?: string
  courseDate: string
  startTime: string
  endTime: string
  maxCapacity: number
  currentCount: number
  price?: number
  description: string
  location?: string
  status: number
  registered?: boolean
}

// 签到
export interface Checkin {
  id?: number
  userId: number
  courseId: number
  courseName?: string
  checkinTime?: string
  status: number
  remark?: string
}

// 首页数据
export interface CardInfo {
  cardType?: string
  cardStartDate?: string
  cardEndDate?: string
}

export interface HomeData {
  myCourseCount: number
  monthCheckins: number
  checkinDates?: string[]
  myCourses: Course[]
  upcomingCourses: Course[]
  coaches: Coach[]
  card?: CardInfo
}

// 登录表单
export interface LoginForm {
  username: string
  password: string
}

// 注册表单
export interface RegisterForm {
  username: string
  phone: string
  password: string
  role?: string
}

// 登录/注册响应
export interface AuthResult {
  token: string
  nickname: string
  avatar: string
  userId: string
  role: string
}

// 管理员仪表盘
export interface AdminDashboard {
  totalUsers: number
  totalCourses: number
  totalCoaches: number
  todayCheckins: number
  monthlyRevenue: number
}

// 教练日程项
export interface CoachScheduleItem {
  courseId: number
  courseName: string
  courseDate: string
  startTime: string
  endTime: string
  location: string
  price: number
  maxCapacity: number
  currentCount: number
  memberName: string
  memberPhone: string
}

// 图表统计数据
export interface DashboardStats {
  enrollmentTrend: { month: string; count: number }[]
  revenueByMonth: { month: string; amount: number }[]
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
    showTabBar?: boolean
    requiresAuth?: boolean
    roles?: string[]
    icon?: string
    transition?: string
  }
}

export {}
