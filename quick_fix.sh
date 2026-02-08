#!/bin/bash

echo "🔧 快速修复 New Concept English App"
echo "================================"

# 1. 创建assets文件夹
echo "1️⃣ 创建assets文件夹..."
mkdir -p android-native-app/app/src/main/assets

# 2. 复制内容（带路径修复）
echo "2️⃣ 复制课程内容..."
cp -r shared-content/book* android-native-app/app/src/main/assets/

# 3. 修复JSON文件中的路径
echo "3️⃣ 修复文件路径..."

cd android-native-app/app/src/main/assets

# 修复所有book文件夹中的JSON文件
for book in book1 book2 book3 book4; do
  if [ -d "$book" ]; then
    # 使用Python快速替换路径
    python3 << EOF
import json
import os
from pathlib import Path

book_dir = Path('$book')
for json_file in book_dir.glob('*.json'):
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            lesson = json.load(f)

        # 移除绝对路径，只保留文件名
        if 'audioFile' in lesson:
            old_path = lesson['audioFile']
            filename = os.path.basename(old_path)
            lesson['audioFile'] = filename

        if 'lrcFile' in lesson:
            old_path = lesson['lrcFile']
            filename = os.path.basename(old_path)
            lesson['lrcFile'] = filename

        with open(json_file, 'w', encoding='utf-8') as f:
            json.dump(lesson, f, ensure_ascii=False, indent=2)

        print(f"✅ {json_file.name}")
    except Exception as e:
        print(f"❌ {json_file.name}: {e}")
EOF
  fi
done

echo ""
echo "✅ 修复完成！"
echo ""
echo "现在回到Android Studio，点击："
echo "1. File → Sync Project with Gradle Files"
echo "2. 等待同步完成"
echo "3. 点击 Run 按钮 (绿色▶️)"
echo ""
echo "📱 应用将显示560个课程！"
