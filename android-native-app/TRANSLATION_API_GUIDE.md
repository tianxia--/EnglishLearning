# 翻译API申请指南

本文档介绍如何为英语学习应用申请和集成翻译API。

## 🌐 推荐的翻译API服务

### 1. **Google Cloud Translation API** ⭐ 推荐
- **官网**: https://cloud.google.com/translate
- **免费额度**: 每月 50万字符免费
- **价格**: 超出后 $20/百万字符
- **支持语言**: 100+ 种语言
- **优点**: 准确度高，支持多种格式，API稳定
- **申请步骤**:
  1. 访问 https://console.cloud.google.com
  2. 创建新项目或选择现有项目
  3. 启用 "Cloud Translation API"
  4. 创建凭据 → API密钥
  5. 限制API密钥（推荐）：
     - 应用限制：Android应用
     - 包名：`com.englishlearning`
     - SHA-1指纹：从你的签名证书获取

### 2. **Microsoft Translator Text API**
- **官网**: https://azure.microsoft.com/services/cognitive-services/translator/
- **免费额度**: 每月 200万字符免费
- **价格**: 超出后 $10/百万字符
- **支持语言**: 90+ 种语言
- **优点**: 文档详细，提供自定义翻译功能
- **申请步骤**:
  1. 访问 https://portal.azure.com
  2. 创建 "Translator" 资源
  3. 选择定价层（免费F0或付费）
  4. 获取密钥和终端URL

### 3. **DeepL API**
- **官网**: https://www.deepl.com/pro-api
- **免费额度**: 每月 50万字符免费
- **价格**: $4.99-49.99/月（不同套餐）
- **支持语言**: 30+ 种语言
- **优点**: 翻译质量极高，尤其适合欧洲语言
- **申请步骤**:
  1. 访问 https://www.deepl.com/pro-api
  2. 注册账号
  3. 选择免费或付费计划
  4. 获取API密钥

### 4. **百度翻译API** - 国内选择
- **官网**: https://fanyi-api.baidu.com/
- **免费额度**: 标准版每月 5万字符免费
- **价格**: ¥49-149/月（不同套餐）
- **支持语言**: 200+ 种语言
- **优点**: 国内访问快，价格便宜，中文翻译好
- **申请步骤**:
  1. 访问 https://fanyi-api.baidu.com/
  2. 登录/注册百度账号
  3. 进入管理控制台
  4. 开通翻译服务
  5. 获取APP ID和密钥

### 5. **腾讯云翻译API**
- **官网**: https://cloud.tencent.com/product/tmt
- **免费额度**: 每月 5万字符免费
- **价格**: ¥58/百万字符起
- **支持语言**: 80+ 种语言
- **优点**: 与微信生态集成，价格合理
- **申请步骤**:
  1. 访问 https://console.cloud.tencent.com/tmt
  2. 开通机器翻译服务
  3. 创建密钥
  4. 获取SecretId和SecretKey

### 6. **有道翻译API**
- **官网**: https://ai.youdao.com/product-fanyi.s
- **免费额度**: 每月 100万字符免费
- **价格**: ¥0.0059/字符（标准版）
- **支持语言**: 100+ 种语言
- **优点**: 有道词典背书，中英文翻译质量好
- **申请步骤**:
  1. 访问 https://ai.youdao.com
  2. 注册/登录账号
  3. 创建应用
  4. 获取应用ID和密钥

## 💡 推荐选择

### 个人开发者/学习用途：
⭐ **推荐顺序**:
1. **Google Cloud Translation API** - 免费额度高，质量好
2. **百度翻译API** - 国内访问快，申请简单
3. **DeepL API** - 翻译质量最佳（但支持语言较少）

### 商业应用：
1. **Google Cloud Translation API** - 最稳定可靠
2. **Microsoft Translator** - 企业级支持
3. **腾讯云翻译** - 国内服务器，访问稳定

## 🔑 API密钥安全最佳实践

### 1. **不要将API密钥硬编码在代码中**
```kotlin
// ❌ 错误做法
const val API_KEY = "YOUR_API_KEY_HERE"
```

### 2. **使用本地配置文件**
在 `local.properties` 中存储（不要提交到Git）:
```properties
# local.properties
TRANSLATION_API_KEY=your_api_key_here
```

### 3. **在build.gradle中读取**
```kotlin
// build.gradle.kts
android {
    defaultConfig {
        val localProperties = Properties()
        localProperties.load(rootProject.file("local.properties").inputStream())
        val apiKey = localProperties.getProperty("TRANSLATION_API_KEY", "")

        buildConfigField("String", "TRANSLATION_API_KEY", "\"$apiKey\"")
    }
}
```

### 4. **在代码中使用**
```kotlin
val apiKey = BuildConfig.TRANSLATION_API_KEY
```

## 📝 集成示例（以Google Cloud Translation为例）

### 1. 添加依赖
```gradle
dependencies {
    implementation("com.google.cloud:google-cloud-translate:2.30.0")
}
```

### 2. 创建TranslationService
```kotlin
@Singleton
class TranslationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val apiKey = BuildConfig.TRANSLATION_API_KEY

    suspend fun translate(text: String, targetLang: String = "zh-CN"): Result<String> {
        return try {
            // 使用API进行翻译
            val translator = TranslateOptions.newBuilder()
                .setApiKey(apiKey)
                .build()
                .service

            val translation = translator.translate(
                text,
                Translate.TranslateOption.targetLanguage(targetLang)
            )

            Result.success(translation.translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🎯 下一步

选择好API服务商后，告诉我：
1. 你选择了哪个服务
2. 已经获取的API密钥

我会帮你完成集成代码！

## ⚠️ 注意事项

1. **免费额度限制**: 注意各服务的免费额度，超出会产生费用
2. **请求频率限制**: 不要过于频繁调用，添加缓存机制
3. **API密钥安全**: 绝对不要将API密钥提交到GitHub等公开仓库
4. **遵守服务条款**: 按照服务提供商的条款使用API
