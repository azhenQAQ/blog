#!/bin/bash

# 定义项目路径
BASE_DIR=/www/wwwroot/blog
FRONTEND_DIR=$BASE_DIR/frontend
DIST_DIR=$FRONTEND_DIR/dist

echo "=========================================="
echo "开始部署前端项目"
echo "=========================================="

# 1. 删除旧的 dist 目录
if [ -d "$DIST_DIR" ]; then
    echo "正在删除旧的 dist 目录..."
    # 解除 .user.ini 不可变属性（CloudLinux/CageFS 保护），否则 rm -rf 会失败
    if [ -f "$DIST_DIR/.user.ini" ]; then
        chattr -i "$DIST_DIR/.user.ini" 2>/dev/null || true
    fi
    rm -rf "$DIST_DIR"
    echo "旧 dist 目录已删除"
else
    echo "未找到旧的 dist 目录"
fi

# 2. 执行 npm run build
echo "正在执行 npm run build..."
cd "$FRONTEND_DIR"
npm run build

# 检查构建是否成功
if [ ! -d "$DIST_DIR" ]; then
    echo "构建失败！找不到目录：$DIST_DIR"
    exit 1
fi
echo "构建成功"

echo "=========================================="
echo "前端部署成功！"
echo "dist 目录: $DIST_DIR"
echo "=========================================="
