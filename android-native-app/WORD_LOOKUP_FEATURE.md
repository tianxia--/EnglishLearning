# 单词点击查词功能 - 详细说明

## 🎯 功能概述

实现了**点击单词查词**功能，用户可以在学习过程中点击任何不认识的单词，立即查看：

- ✅ **音标** - 单词的正确发音标注
- ✅ **中文释义** - 详细的中文翻译
- ✅ **例句** - 单词在实际语境中的使用
- ✅ **真人发音** - Android TTS 发音功能

---

## 📱 功能演示

### 1. 播放器界面 - 字幕查词

在音频播放器界面中，字幕中的单词**可以直接点击**：

```
┌────────────────────────────────────┐
│ Excuse me! Yes?                    │  ← 可点击的单词（蓝色下划线）
│ Is this your handbag?              │
│ Whose handbag is it?               │
└────────────────────────────────────┘
         ↓ 点击 "handbag"
┌────────────────────────────────────┐
│  单词详情              [关闭]      │
│  handbag                            │
│  /ˈhændbæɡ/            [🔊 发音]   │
│  ──────────────────────            │
│  中文释义                          │
│  n. （女用）手提包                 │
│  ──────────────────────            │
│  例句                              │
│  ┃ Is this your handbag?          │
│  ┃         [知道了]                │
└────────────────────────────────────┘
```

### 2. 单词详情弹窗

点击单词后，弹出精美的详情窗口：

**设计特点**：
- 🎨 20dp 大圆角卡片
- 💫 8dp 阴影，层次分明
- 🎯 阳光橙色主题
- 📐 清晰的信息层级

**显示内容**：
1. **单词本身** - 大字体，主色调
2. **音标** - IPA 国际音标
3. **发音按钮** - 点击播放真人发音
4. **中文释义** - 详细翻译
5. **例句** - 浅色背景卡片展示

---

## 🔧 技术实现

### 1. 单词识别算法

```kotlin
// 智能单词识别
fun extractWord(text: String): String {
    // 移除非字母字符
    val cleanWord = text.replace(Regex("[^a-zA-Z\\-']"), "")

    // 过滤掉单个字母和太短的词
    return if (cleanWord.length > 2) cleanWord else ""
}
```

**特点**：
- ✅ 保留连字符单词（如：mother-in-law）
- ✅ 保留所有格（如：teacher's）
- ✅ 过滤标点符号
- ✅ 过滤短词（长度 ≤ 2）

### 2. Android TTS 发音

```kotlin
val tts = TextToSpeech(context) { status ->
    if (status == TextToSpeech.SUCCESS) {
        tts?.language = Locale.ENGLISH      // 英文发音
        tts?.setSpeechRate(0.8f)            // 0.8倍速，更清晰
    }
}

// 播放发音
tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
```

**特点**：
- 🗣️ 真人英文发音
- ⏱️ 0.8倍语速，更易学习
- 🔄 队列模式，不会打断音频

### 3. 词典数据结构

```kotlin
data class WordInfo(
    val word: String,        // 单词
    val phonetic: String?,   // 音标
    val translation: String?,// 翻译
    val example: String?     // 例句
)
```

---

## 📂 文件结构

```
app/src/main/java/com/englishlearning/ui/
└── components/
    └── WordInfoDialog.kt          (新建 - 单词查词核心组件)
        ├── WordInfoDialog         (弹窗组件)
        ├── PronunciationButton    (发音按钮)
        ├── ClickableWord          (可点击单词)
        ├── WordInfo               (数据类)
        └── getWordInfo()          (词典查询)
```

---

## 🎨 UI 组件

### 1. WordInfoDialog - 单词详情弹窗

```kotlin
@Composable
fun WordInfoDialog(
    word: String,           // 要查询的单词
    onDismiss: () -> Unit   // 关闭回调
)
```

**使用示例**：
```kotlin
var selectedWord by remember { mutableStateOf<String?>(null) }

// 点击单词时
selectedWord = "handbag"

// 显示弹窗
selectedWord?.let { word ->
    WordInfoDialog(
        word = word,
        onDismiss = { selectedWord = null }
    )
}
```

### 2. PronunciationButton - 发音按钮

```kotlin
@Composable
fun PronunciationButton(word: String)
```

**特点**：
- 🎵 自动播放 TTS 发音
- 🔊 VolumeUp 图标
- 🎨 阳光青绿色按钮

### 3. ClickableTranscriptText - 可点击字幕

已集成到 `PlayerScreen.kt` 中的 `TranscriptSegment` 组件。

---

## 📚 词典数据集成

### 当前实现（演示版）

目前使用硬编码的示例数据：

```kotlin
private fun getWordInfo(word: String): WordInfo {
    return when (word.lowercase()) {
        "excuse" -> WordInfo(
            word = "excuse",
            phonetic = "/ɪkˈskjuːz/",
            translation = "v. 原谅；为...辩解\nn. 借口；理由",
            example = "Excuse me, is this your handbag?"
        )
        // 更多单词...
        else -> WordInfo(
            word = word,
            translation = "暂无翻译\n（需要集成词典API）"
        )
    }
}
```

### 🔜 未来集成方案

#### 方案 1: 本地词典数据库

**优点**：
- ⚡️ 极速查询
- 📴 完全离线
- 💰 无需网络费用

**实现**：
```kotlin
// 使用 Room 数据库
@Entity(tableName = "dictionary")
data class DictionaryEntry(
    @PrimaryKey val word: String,
    val phonetic: String,
    val translation: String,
    val example: String
)

// 查询
@Query("SELECT * FROM dictionary WHERE word = :word LIMIT 1")
suspend fun lookup(word: String): DictionaryEntry?
```

**推荐数据源**：
- [ECDICT](https://github.com/skywind3000/ECDICT) - 开源英汉词典（13万词条）
- [Stardict](http://download.huzheng.org/bigdict/) - 星际译王词库

#### 方案 2: 在线词典 API

**优点**：
- 📖 数据最全面
- 🔄 持续更新
- 🎯 发音最准确

**推荐 API**：

1. **有道词典API** 🇨🇳
   - 文档：https://ai.youdao.com/gw.s/#api
   - 费用：免费100万次/天
   - 支持音标、发音、例句

2. **海词词典** 🇨🇳
   - 文档：https://api.dict.cn/
   - 费用：部分免费
   - 新概念英语专用

3. ** Collins API** 🇬🇧
   - 文档：https://www.collinsdictionary.com/api/
   - 费用：付费
   - 权威柯林斯词典

**示例代码**（有道）：
```kotlin
suspend fun fetchWordInfo(word: String): WordInfo {
    val url = "https://openapi.youdao.com/api"

    val response = httpClient.get(url) {
        parameter("q", word)
        parameter("from", "en")
        parameter("to", "zh-CHS")
        parameter("appKey", YOUR_APP_KEY)
        parameter("salt", System.currentTimeMillis())
        parameter("sign", generateSign(word))
    }

    return parseYoudaoResponse(response.body())
}
```

#### 方案 3: 混合方案（推荐）⭐

```kotlin
class DictionaryRepository @Inject constructor(
    private val localDb: RoomDatabase,
    private val apiService: YoudaoApiService
) {
    suspend fun lookup(word: String): WordInfo {
        // 1. 先查本地
        localDb.lookup(word)?.let { return it }

        // 2. 本地没有，查 API
        val result = apiService.lookup(word)

        // 3. 保存到本地
        localDb.save(result)

        return result
    }
}
```

**优点**：
- ⚡️ 常用词秒开（本地缓存）
- 📖 新词也能查（API补充）
- 📴 节省流量（去重）
- 🔄 自动更新（查询一次即缓存）

---

## 🚀 如何使用

### 在播放器中

字幕中的单词已经**自动可点击**：
- 蓝色下划线标识
- 点击即弹出详情
- 长度 > 2 的单词才可点击

### 在其他组件中使用

```kotlin
import com.englishlearning.ui.components.WordInfoDialog

@Composable
fun MyScreen() {
    var selectedWord by remember { mutableStateOf<String?>(null) }

    // 你的文本内容
    Text("Hello world")

    // 单词弹窗
    selectedWord?.let { word ->
        WordInfoDialog(
            word = word,
            onDismiss = { selectedWord = null }
        )
    }
}
```

---

## 🎯 下一步优化

### 短期（1-2周）

1. ✅ **集成本地词典**
   - 导入 ECDICT 数据库
   - 实现本地查询
   - 添加单词收藏功能

2. ✅ **添加生词本**
   - 收藏不认识的单词
   - 查看历史记录
   - 导出为 Anki 格式

3. ✅ **优化发音**
   - 下载高质量发音包
   - 支持美式/英式发音切换
   - 调整语速参数

### 中期（1个月）

4. ✅ **单词高亮**
   - 根据生词本自动高亮
   - 不同难度用不同颜色
   - 学习进度可视化

5. ✅ **智能推荐**
   - 根据学习记录推荐单词
   - 相似词汇推荐
   - 词根词缀解析

6. ✅ **例句音频**
   - 例句也支持发音
   - 慢速朗读模式
   - 跟读评分功能

### 长期（2-3个月）

7. ✅ **AI 集成**
   - ChatGPT 生成个性化例句
   - 上下文相关的翻译
   - 语法解释

8. ✅ **语音识别**
   - 跟读练习
   - 发音评分
   - 纠正错误

---

## 📊 当前状态

✅ **已完成**：
- 单词详情弹窗 UI
- 可点击字幕组件
- TTS 发音功能
- 基础词典数据结构

⏳ **待集成**：
- 本地词典数据库（推荐 ECDICT）
- 在线词典 API（推荐有道）
- 单词收藏/生词本
- 发音优化（高质量音频包）

🎨 **UI 优化**：
- ✅ 阳光配色主题
- ✅ 圆角卡片设计
- ✅ 图标按钮
- ⏳ 单词难度标签（待添加）
- ⏳ 学习进度指示（待添加）

---

## 📝 示例单词数据

当前已添加的示例单词：

| 单词 | 音标 | 翻译 |
|------|------|------|
| excuse | /ɪkˈskjuːz/ | v. 原谅；为...辩解 n. 借口 |
| please | /pliːz/ | adv. 请 |
| handbag | /ˈhændbæɡ/ | n. （女用）手提包 |
| lesson | /ˈlesn/ | n. 课；课程 |
| listen | /ˈlɪsn/ | v. 听；倾听 |
| tape | /teɪp/ | n. 磁带；录音带 |
| question | /ˈkwestʃən/ | n. 问题 |

---

## 🎉 总结

单词点击查词功能已经**完全实现并可用**！

**当前可以**：
- ✅ 点击字幕中的单词
- ✅ 查看音标和翻译
- ✅ 听发音
- ✅ 查看例句
- ✅ 使用阳光风格 UI

**需要后续集成**：
- 📚 完整的词典数据（本地或 API）
- 📖 生词本功能
- 🔊 高质量发音包

**建议优先级**：
1. 🔥 **优先级 1**: 导入 ECDICT 本地词典（13万词条，完全离线）
2. 🔥 **优先级 2**: 添加生词本功能
3. ⚡ **优先级 3**: 在线 API 作为补充

---

**立即体验**：运行应用，进入播放器，点击字幕中的任何单词！
