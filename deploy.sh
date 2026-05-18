#!/bin/bash

# 定义项目路径和 JAR 包名称
BASE_DIR=/www/wwwroot/blog
JAR_NAME=backend-0.0.1-SNAPSHOT.jar
TARGET_JAR=$BASE_DIR/backend/target/$JAR_NAME
LOG_FILE=$BASE_DIR/app.log

echo "=========================================="
echo "开始部署项目：$JAR_NAME"
echo "=========================================="

# 1. 停止旧进程
echo "正在检查并停止旧服务..."
# 查找正在运行的 jar 包进程 PID
PID=$(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "发现旧进程 (PID: $PID)，正在关闭..."
    kill -9 $PID
    echo "旧服务已停止"
else
    echo "未运行中的服务"
fi

# 2. Maven 打包
echo "正在 Maven 打包..."
cd $BASE_DIR/backend
# 清理并打包，跳过测试
mvn clean package -DskipTests

# 检查打包是否成功
if [ ! -f "$TARGET_JAR" ]; then
    echo "打包失败！找不到文件：$TARGET_JAR"
    exit 1
fi
echo "打包成功"

# 3. 后台启动
echo "正在后台启动新服务..."
nohup java -jar $TARGET_JAR --spring.profiles.active=prod > $LOG_FILE 2>&1 &

# 等待几秒检查进程是否启动
sleep 2
NEW_PID=$(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}')

if [ -n "$NEW_PID" ]; then
    echo "=========================================="
    echo "部署成功！"
    echo "进程 PID: $NEW_PID"
    echo "日志文件: $LOG_FILE"
    echo "查看日志命令: tail -f $LOG_FILE"
    echo "=========================================="
else
    echo "服务启动失败，请检查日志！"
fi