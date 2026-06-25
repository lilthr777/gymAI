# gymAI · 智能健身房管理系统

基于 **Vue 3 + Spring Boot 3** 的全栈健身房管理系统，集成 DeepSeek 大模型 AI 健身助手。

> 前端开发练习项目，涵盖 B 端后台常见的 CRUD 闭环、数据可视化、权限控制与流式 AI 对话。

![screenshot](screenshots/dashboard.png)

## 功能

- **仪表盘看板** — 会员增长趋势、课程分布、周签到统计（ECharts 可视化）
- **会员管理** — 姓名/手机号搜索，会员卡类型与有效期管理
- **教练管理** — 教练信息维护，擅长领域标签
- **课程管理** — 课程排期、教练分配、容量管理
- **签到记录** — 打卡记录查询，远程搜索快捷签到
- **AI 健身助手** — SSE 流式对话，支持中止生成与历史持久化
- **登录鉴权** — JWT 认证 + 路由守卫 + 自定义权限指令

## 技术栈

| 前端 | 后端 |
|------|------|
| Vue 3 · Composition API | Spring Boot 3 |
| TypeScript | MyBatis-Plus |
| Element Plus | Spring Security + JWT |
| Pinia + persistedstate | MySQL 8.0 |
| ECharts · vue-echarts | Spring AI + DeepSeek |
| Axios · SSE ReadableStream | Docker Compose |
| Vite · SCSS · ESLint | Maven |

## 项目结构

```
gymAI/
├── frontend/           # Vue 3 前端（端口 3000）
│   └── src/
│       ├── api/        # API 层（Axios 实例 + 拦截器）
│       ├── composables/ # useChart / useSSE
│       ├── directives/  # v-permission 权限指令
│       ├── stores/      # Pinia 状态管理
│       ├── views/       # 页面组件
│       └── components/  # Layout / TableWrapper
├── backend/
│   ├── gym-server/      # 业务服务（端口 8080）
│   └── springai-model/  # AI 服务（端口 8088）
└── docker-compose.yml   # 一键部署
```

## 快速开始

### 本地开发

```bash
# 一键启动（需要 Git Bash / WSL）
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

访问 `http://localhost:3000`，默认账号 `admin` / `admin123`。Swagger 文档 `http://localhost:8080/swagger-ui.html`。

## 实现亮点

- **Axios 请求去重**: 基于 `method+url+params` 生成请求指纹，自动取消重复请求，避免表单重复提交
- **SSE 流式解析**: 手动处理 `ReadableStream` 分片，逐 3 字符打字机效果输出，支持中途 Abort
- **Pinia 持久化**: 登录态与 AI 对话记录通过 `pinia-plugin-persistedstate` 自动写入 localStorage
- **权限指令**: `v-permission` 自定义指令根据用户角色控制 DOM 显隐
