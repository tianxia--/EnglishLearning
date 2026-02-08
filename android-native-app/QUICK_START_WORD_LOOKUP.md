# 单词查词功能 - 快速开始

## ✅ 功能已完成！

现在你可以在应用中点击任何单词查看详情了。

---

## 🚀 立即测试

### 步骤 1: 构建应用

```bash
# 在 Android Studio 中
1. Build → Clean Project
2. Build → Rebuild Project
3. 点击 Run 按钮
```

### 步骤 2: 进入播放器

1. 应用打开后，点击任意一本书
2. 点击任意课程
3. 选择 "Audio Player" 进入播放器

### 步骤 3: 测试查词功能

在字幕中，你会看到：

```
Excuse me! Yes?
Is this your handbag?
^^^^^^             ← 蓝色下划线，表示可点击
```

**点击 "handbag"**，会弹出：

```
┌────────────────────────────────┐
│ 单词详情              [X]      │
│                                │
│ handbag                         │
│ /ˈhændbæɡ/        [🔊 发音]    │
│ ───────────────────            │
│ 中文释义                        │
│ n. （女用）手提包              │
│ ───────────────────            │
│ 例句                           │
│ ┃ Is this your handbag?       │
│         [知道了]               │
└────────────────────────────────┘
```

---

## 🎯 支持的单词

当前已添加示例数据：
- excuse
- please
- handbag
- lesson
- listen
- tape
- question

其他单词会显示"暂无翻译"提示。

---

## 📚 如何添加更多单词？

### 方法 1: 临时测试（快速）

编辑 `WordInfoDialog.kt` 文件：

```kotlin
private fun getWordInfo(word: String): WordInfo {
    return when (word.lowercase()) {
        "excuse" -> WordInfo(
            word = "excuse",
            phonetic = "/ɪkˈskjuːz/",
            translation = "v. 原谅",
            example = "Excuse me!"
        )
        // 添加更多单词...
        "myword" -> WordInfo(
            word = "myword",
            phonetic = "/maɪwɜːd/",
            translation = "n. 我的单词",
            example = "This is myword"
        )
        else -> WordInfo(word = word, translation = "暂无翻译")
    }
}
```

### 方法 2: 导入完整词典（推荐）⭐

**使用 ECDICT 开源词典**（13万+词条）：

1. **下载数据库**
```bash
# 访问 GitHub
https://github.com/skywind3000/ECDICT

# 下载 stardict.csv 文件
wget https://github.com/skywind3000/ECDICT/releases/download/v1.0/stardict.csv
```

2. **导入到应用**
```kotlin
// 创建 DictionaryRepository
class DictionaryRepository(private val context: Context) {

    fun loadDictionary(): Map<String, WordInfo> {
        val dictionary = mutableMapOf<String, WordInfo>()

        context.assets.open("stardict.csv")
            .bufferedReader()
            .use { reader ->
                reader.lines().forEach { line ->
                    val parts = line.split("\t")
                    if (parts.size >= 3) {
                        dictionary[parts[0]] = WordInfo(
                            word = parts[0],
                            phonetic = parts.getOrNull(1),
                            translation = parts[2],
                            example = parts.getOrNull(3)
                        )
                    }
                }
            }

        return dictionary
    }
}
```

3. **替换 getWordInfo 函数**
```kotlin
private val dictionary = DictionaryRepository(context).loadDictionary()

private fun getWordInfo(word: String): WordInfo {
    return dictionary[word.lowercase()]
        ?: WordInfo(word = word, translation = "暂无翻译")
}
```

---

## 🔊 测试发音功能

点击弹窗中的 **"🔊 发音"** 按钮，会播放：
- Android 系统 TTS 发音
- 0.8倍语速（更清晰）
- 英文发音

**注意**：首次使用可能需要下载 TTS 数据包。

---

## 🎨 UI 特性

### 阳光风格设计
- 🎨 橙色主题色（#FF6B35）
- 📐 20dp 大圆角
- 💫 8dp 阴影
- 🎯 清晰的信息层级

### 交互细节
- 单词带蓝色下划线
- 点击立即弹出
- 背景遮罩效果
- 流畅的动画过渡

---

## 📊 当前状态

✅ **完全可用**：
- ✅ 单词点击识别
- ✅ 详情弹窗 UI
- ✅ TTS 发音
- ✅ 示例数据

⏳ **待完善**：
- ⏳ 完整词典数据
- ⏳ 生词本功能
- ⏳ 高质量发音包

---

## 🎯 推荐工作流

### 当前（临时方案）
1. 在播放器中学习
2. 点击遇到的生词
3. 查看音标和翻译
4. 听发音
5. 手动记录到笔记本

### 改进后（完整方案）⭐
1. 在播放器中学习
2. 点击生词查看详情
3. ⭐ 一键收藏到生词本
4. ⭐ 自动添加到复习计划
5. ⭐ 闪卡模式复习
6. ⭐ 导出为 Anki 卡组

---

## 💡 使用技巧

### 1. 快速查词
- 长按单词也可以触发（已预留给未来功能）
- 长度 > 2 的单词才可点击
- 自动过滤标点符号

### 2. 发音练习
- 先听发音
- 跟读几遍
- 再看音标
- 最后看例句

### 3. 记忆方法
- 结合例句记忆
- 联想上下文
- 定期复习

---

## 🐛 常见问题

### Q: 点击没反应？
A: 确保单词长度 > 2，且只包含英文字母。

### Q: 发音没有声音？
A: 检查系统 TTS 设置：
设置 → 系统 → 语言和输入 → 语音输出

### Q: 显示"暂无翻译"？
A: 当前只有7个示例单词，需要导入完整词典。

### Q: 如何添加更多单词？
A: 参考上面的"方法1"或"方法2"。

---

## 🎉 开始使用

现在就运行应用，体验单词查词功能！

```
运行应用 → 选择书籍 → 选择课程 → 进入播放器 → 点击字幕中的单词
```

**期待你的反馈！** 📚✨
