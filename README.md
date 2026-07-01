# gymAI - 你的专属健身伙伴

基于 **Vue 3 + Spring Boot 3** 的全栈 C端健身应用，集成 DeepSeek AI 健身助手。

> 支持用户注册登录、浏览课程教练、在线预约、签到打卡、AI 健身指导。

## 功能

- **首页** — 个人运动数据概览、推荐课程、推荐教练
- **课程浏览** — 搜索课程、查看详情、一键预约/签到
- **教练浏览** — 教练列表、查看教练详情及其课程
- **我的课程** — 已报名课程管理与签到
- **签到记录** — 训练打卡历史
- **AI 健身助手** — SSE 流式对话，支持中止生成与历史持久化
- **个人中心** — 资料编辑、会员卡信息
- **登录鉴权** — JWT 认证 + 路由守卫

## 技术栈

| 前端                       | 后端                  |
| -------------------------- | --------------------- |
| Vue 3 · Composition API    | Spring Boot 3         |
| TypeScript                 | MyBatis-Plus          |
| Element Plus               | Spring Security + JWT |
| Pinia + persistedstate     | MySQL 8.0             |
| Axios · SSE ReadableStream | Spring AI + DeepSeek  |
| Vite · SCSS · ESLint       | Maven                 |

## 项目结构

```
gymAI/
├── frontend/           # Vue 3 前端（端口 3000）
│   └── src/
│       ├── api/        # API 层（Axios 实例 + 拦截器）
│       ├── composables/ # useRequest / useSSE
│       ├── stores/      # Pinia 状态管理
│       ├── views/       # 页面组件
│       └── components/  # Layout / CourseCard / CoachCard
├── backend/
│   ├── gym-server/      # 业务服务（端口 8080）
│   └── springai-model/  # AI 服务（端口 8088）
└── docker-compose.yml   # 一键部署
```

## 快速开始

### 本地开发

```bash
# 一键启动
./start.sh
```

```bash
# 停止所有服务
./stop.sh
```

> `start.sh` 通过 nohup 分别启动后端（8080 / 8088）与前端（3000），日志写入 `/tmp/`。

### 手动启动

```bash
# 后端
cd backend/gym-server && mvn spring-boot:run      # 端口 8080
cd backend/springai-model && mvn spring-boot:run  # 端口 8088

# 前端
cd frontend && npm install && npm run dev          # 端口 3000
```

### Docker（可选）

```bash
export DEEPSEEK_API_KEY=your_key
docker compose up -d
```

访问 `http://localhost:3000`，默认测试账号 `zhangsan` / `admin123`。Swagger 文档 `http://localhost:8080/swagger-ui.html`。

### 停止8080

```
powershell -Command "Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess -Force"
```

## 实现亮点

- **Axios 请求去重**: 基于 `method+url+params` 生成请求指纹，自动取消重复请求
- **SSE 流式解析**: 手动处理 `ReadableStream` 分片，逐字符打字机效果输出，支持中途 Abort
- **Pinia 持久化**: 登录态与 AI 对话记录通过 `pinia-plugin-persistedstate` 自动写入 localStorage
