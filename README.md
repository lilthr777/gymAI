# gymAI — 智能健身房管理系统

基于 **Vue 3 + Spring Boot 3** 的全栈 C 端健身应用，集成 **DeepSeek AI** 健身助手，Apple 风格 UI。

## 功能模块

| 模块 | 功能 |
|------|------|
| **首页** | 个人运动数据概览、会员卡状态、近期课程、课程/教练推荐 |
| **课程** | 搜索课程、分类筛选（全部/可报名/已报名）、课程详情、一键报名/取消/签到 |
| **教练** | 教练列表、分类筛选（瑜伽/增肌/搏击/舞蹈）、教练详情、收藏切换 |
| **AI 助手** | SSE 流式对话、打字机效果、Markdown 渲染、快速提示、历史持久化 |
| **个人中心** | 资料编辑、头像上传、我的课程、签到记录、会员卡购买/续费 |
| **认证** | JWT 登录/注册、路由守卫、记住密码 |

## 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 前端框架 | Vue 3 Composition API + TypeScript | 3.4 |
| 构建工具 | Vite | 5 |
| UI 库 | Element Plus | 2.7 |
| 状态管理 | Pinia + persistedstate | 2.1 |
| HTTP | Axios（请求去重 + FormData 支持） | 1.7 |
| 样式 | SCSS + Apple 设计 Token 系统 | 1.77 |
| 后端框架 | Spring Boot 3 + MyBatis-Plus | 3.2 |
| 安全 | Spring Security + JWT（jjwt 0.12） | |
| AI | Spring AI + DeepSeek Function Calling | 1.0 M4 |
| 数据库 | MySQL | 8.0 |
| API 文档 | Knife4j / Swagger | 4.3 |

## 项目结构

```
gymAI/
├── frontend/                  # Vue 3 SPA（端口 3000）
│   └── src/
│       ├── api/index.ts       # 全部 API 调用（含上传）
│       ├── utils/request.ts   # Axios 实例、去重、JWT 注入
│       ├── stores/            # Pinia：user / chat / app
│       ├── views/             # 13 个页面组件
│       ├── components/        # Layout、CourseCard、CoachCard、StarRating
│       ├── assets/styles/     # variables.scss（Token）+ global.scss
│       ├── types/index.ts     # TypeScript 类型定义
│       ├── composables/       # useRequest
│       └── directives/        # v-focus
├── backend/
│   ├── gym-server/            # 业务 API（端口 8080）
│   │   └── src/main/java/com/gymai/
│   │       ├── controller/    # 11 个 Controller
│   │       ├── service/       # 5 个 Service + Impl
│   │       ├── entity/        # 8 个实体（User/Coach/Course/Checkin/CardOrder 等）
│   │       ├── mapper/        # 8 个 MyBatis-Plus Mapper
│   │       ├── config/        # Security、JWT、CORS、Web、MyBatis-Plus
│   │       └── common/        # Result、GlobalExceptionHandler
│   └── springai-model/        # AI 服务（端口 8088）
│       └── src/main/java/com/gymai/
│           ├── controller/    # AiChatController（SSE 流式）
│           ├── config/        # CORS
│           └── tools/         # GymDataService + ToolConfig（Function Calling）
├── docker-compose.yml         # MySQL + gym-server + springai-model
├── start.sh                   # 一键启动
└── stop.sh                    # 停止所有服务
```

## API 端点一览

### 公开接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |
| GET | `/api/courses` | 课程列表（分页） |
| GET | `/api/courses/{id}` | 课程详情 |
| GET | `/api/coaches` | 教练列表（分页） |
| GET | `/api/coaches/{id}` | 教练详情 |
| GET | `/api/reviews/course/{id}` | 课程评价 |
| GET | `/uploads/**` | 静态文件（头像等） |

### 需认证
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/home` | 首页聚合数据 |
| GET | `/api/user/profile` | 个人资料 |
| PUT | `/api/user/profile` | 更新资料（含头像 URL） |
| POST | `/api/upload/avatar` | 头像上传（≤5MB，仅图片） |
| POST | `/api/courses/{id}/register` | 报名课程 |
| POST | `/api/courses/{id}/cancel` | 取消报名 |
| GET | `/api/courses/my` | 我的课程 |
| POST | `/api/checkins` | 签到 |
| GET | `/api/checkins/my` | 签到记录 |
| POST | `/api/reviews` | 提交评价 |
| POST | `/api/favorites/coach/{id}` | 切换收藏教练 |
| GET | `/api/favorites/coaches` | 收藏列表 |
| GET | `/api/favorites/coach-ids` | 收藏 ID 列表 |
| GET | `/api/card` | 会员卡信息 + 购买记录 |
| POST | `/api/card/renew` | 开卡/续费 |
| POST | `/ai/chat` | AI 对话（SSE 流式，端口 8088） |

## 数据库

8 张表：`user`、`coach`、`course`、`checkin`、`user_course`、`card_order`、`course_review`、`user_favorite_coach`

初始化脚本 `backend/gym-server/src/main/resources/db/init.sql` 含 4 个测试账号（密码 `admin123`）、3 个教练、4 个课程。

## 快速开始

```bash
# 一键启动
./start.sh

# 手动启动
cd backend/gym-server && mvn spring-boot:run       # 8080
cd backend/springai-model && mvn spring-boot:run   # 8088（需 DEEPSEEK_API_KEY）
cd frontend && npm install && npm run dev           # 3000

# Docker
export DEEPSEEK_API_KEY=your_key
docker compose up -d
```

访问 `http://localhost:3000`。测试账号 `zhangsan` / `admin123`。

### 停止

```bash
./stop.sh

# Windows 单独停 8080
powershell -Command "Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess -Force"
```

## 设计系统

Apple 风格 UI，设计 Token 集中在 `variables.scss`：

- **配色**: 纯白底 `#fff` + 浅灰 `#f5f5f7` + 苹果蓝 `#0071e3` + 苹果红 `#ff3b30`
- **字体**: SF Pro 系统字体栈，5 档字号（12/14/17/21/28/40/56px）
- **暗色模式**: 真黑底 `#000` + 深灰卡片 `#1c1c1e`，所有组件完整覆盖
- **磨砂玻璃**: 顶栏/底栏 fixed + `backdrop-filter: blur(20px)`
- **路由过渡**: fade/slide 200ms 动画
- **卡片**: 圆角 12-16px、微阴影、hover 上浮

## 实现亮点

- **请求去重**: Axios 拦截器基于 `method+url+params+data` 生成指纹，自动 Abort 重复请求
- **SSE 流式对话**: 手动解析 `ReadableStream` 分片，3 字符/30ms 打字机效果，支持中途 Abort
- **Spring AI Function Calling**: AI 可实时查询课程与教练数据，上下文感知的健身建议
- **Pinia 持久化**: 登录态、暗色模式、AI 对话历史自动写入 localStorage
- **头像上传**: 点击触发、hover 遮罩预览、FormData 上传、5MB 限制、UUID 命名
- **毛玻璃导航**: fixed 定位 + 半透明背景 + backdrop-filter，内容滚动穿透产生模糊效果
- **会员卡**: Apple Card 风格渐变卡片 + 套餐选择 + 自动计算剩余天数与进度
