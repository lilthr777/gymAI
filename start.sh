#!/bin/bash
# 启动全部服务

ROOT="$(cd "$(dirname "$0")" && pwd)"
MAVEN="$HOME/apache-maven-3.9.9/bin/mvn"

echo "=== 启动 gym-server (8080) ==="
cd "$ROOT/backend/gym-server" && nohup "$MAVEN" spring-boot:run > /tmp/gym-server.log 2>&1 &

echo "=== 启动 springai-model (8088) ==="
cd "$ROOT/backend/springai-model" && nohup "$MAVEN" spring-boot:run > /tmp/gymai-ai.log 2>&1 &

echo "=== 启动前端 (3000) ==="
cd "$ROOT/frontend" && nohup npm run dev > /tmp/gymai-frontend.log 2>&1 &

sleep 3
echo ""
echo "=== 服务启动中,请稍候... ==="
echo "日志文件: /tmp/gym-server.log  /tmp/gymai-ai.log  /tmp/gymai-frontend.log"
echo "前端: http://localhost:3000"
echo "Swagger: http://localhost:8080/swagger-ui.html"
