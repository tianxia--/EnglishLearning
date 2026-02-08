#!/bin/bash

# 修复gradle wrapper并编译项目

PROJECT_DIR="/Users/pengfei.chen/Desktop/privateWork/android-native-app"
cd "$PROJECT_DIR"

# 检查gradle wrapper jar是否存在
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Gradle wrapper JAR missing, downloading..."

    # 创建临时目录
    mkdir -p /tmp/gradle-wrapper

    # 下载gradle wrapper
    curl -L -o /tmp/gradle-wrapper/gradle-wrapper.jar \
        "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"

    # 复制到项目目录
    cp /tmp/gradle-wrapper/gradle-wrapper.jar gradle/wrapper/

    # 设置权限
    chmod +x gradlew

    echo "Gradle wrapper downloaded successfully!"
fi

# 运行编译
echo "Starting compilation..."
./gradlew compileDebugKotlin

# 检查编译结果
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
else
    echo "❌ Compilation failed!"
    exit 1
fi
