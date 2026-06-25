#!/bin/bash
# 停止全部服务
echo "=== 停止所有服务 ==="
for port in 8080 8088 3000; do
    pid=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}')
    if [ -n "$pid" ]; then
        taskkill -PID $pid -F 2>/dev/null && echo "端口 $port (PID $pid) 已停止"
    else
        echo "端口 $port 没有运行"
    fi
done
echo "=== 完成 ==="
