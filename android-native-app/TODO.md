# 待办事项清单

## 🔴 P0 - 紧急修复（影响核心功能）

### 1. 修复LRC时间同步问题
- [ ] **问题**: 播放时文本高亮与音频不同步
- [ ] **位置**: `LrcSyncManager.kt`
- [ ] **任务**:
  - [ ] 优化`updatePosition`算法，使用时间窗口匹配
  - [ ] 考虑`Segment.endTime`进行更精确判断
  - [ ] 添加时间容差处理（±0.5秒）
  - [ ] 将更新频率从300ms改为100ms
  - [ ] 统一时间单位（确保秒和毫秒转换正确）
  - [ ] 添加同步校准功能（允许用户手动调整偏移）

### 2. 统一时间单位处理
- [ ] **问题**: Segment使用秒，AudioManager混用秒和毫秒
- [ ] **位置**: `LrcSyncManager.kt`, `AudioManager.kt`, `PlayerViewModel.kt`
- [ ] **任务**:
  - [ ] 统一使用毫秒作为内部时间单位
  - [ ] 在LrcSyncManager中统一转换
  - [ ] 添加时间单位转换工具函数
  - [ ] 添加单元测试验证转换正确性

## 🟡 P1 - 高优先级（核心功能完善）

### 3. 实现停止按钮功能
- [ ] **位置**: `PlayerScreen.kt:411`
- [ ] **任务**:
  - [ ] 在`PlayerViewModel`中添加`stop()`方法
  - [ ] 调用`AudioManager.stop()`
  - [ ] 重置播放位置到0
  - [ ] 更新UI状态
  - [ ] 保存当前进度（如果已播放）

### 4. 实现听写练习音频播放
- [ ] **位置**: `TranscriptionViewModel.kt:77-84`
- [ ] **任务**:
  - [ ] 在`TranscriptionViewModel`中注入`AudioManager`
  - [ ] 实现`playSegment()`方法，播放指定时间段
  - [ ] 支持单段循环播放
  - [ ] 添加播放状态指示
  - [ ] 处理播放完成事件

### 5. 实现后台播放服务
- [ ] **位置**: `AndroidManifest.xml`, 新建`AudioPlayerService.kt`
- [ ] **任务**:
  - [ ] 创建`AudioPlayerService`类
  - [ ] 配置MediaSession
  - [ ] 实现前台服务
  - [ ] 处理播放控制命令
  - [ ] 在`AndroidManifest.xml`中启用服务
  - [ ] 连接`AudioManager`和`AudioPlayerService`

### 6. 实现音频分段播放功能
- [ ] **位置**: `AudioManager.kt`
- [ ] **任务**:
  - [ ] 添加`playSegment(startTime: Long, endTime: Long)`方法
  - [ ] 实现A-B重复播放功能
  - [ ] 添加循环播放单段选项
  - [ ] 在播放器UI中添加分段控制按钮

## 🟢 P2 - 中优先级（用户体验增强）

### 7. 实现通知栏播放控制
- [ ] **任务**:
  - [ ] 创建MediaStyle通知
  - [ ] 添加播放/暂停、上一首、下一首按钮
  - [ ] 显示当前播放课程信息
  - [ ] 处理通知点击事件
  - [ ] 更新通知状态

### 8. 实现锁屏播放控制
- [ ] **任务**:
  - [ ] 配置MediaSession元数据
  - [ ] 添加锁屏封面图片
  - [ ] 实现锁屏控制按钮
  - [ ] 处理音频焦点

### 9. 播放器功能增强
- [ ] **任务**:
  - [ ] 添加书签功能（标记重要位置）
  - [ ] 实现播放历史记录
  - [ ] 添加播放列表功能
  - [ ] 实现收藏课程功能
  - [ ] 添加快捷键支持（耳机按钮）

### 10. 学习统计功能
- [ ] **任务**:
  - [ ] 创建统计数据库表
  - [ ] 实现每日学习时长统计
  - [ ] 添加学习曲线图表
  - [ ] 实现词汇掌握率统计
  - [ ] 添加课程完成度统计
  - [ ] 创建统计界面

### 11. 个性化设置
- [ ] **任务**:
  - [ ] 创建设置界面
  - [ ] 实现播放速度记忆
  - [ ] 添加自动播放设置
  - [ ] 实现字幕显示偏好
  - [ ] 添加主题切换（深色/浅色）
  - [ ] 实现字体大小调节

## 🔵 P3 - 低优先级（未来功能）

### 12. 数据同步功能
- [ ] **任务**:
  - [ ] 设计云同步API
  - [ ] 实现学习进度同步
  - [ ] 实现多设备同步
  - [ ] 添加数据备份和恢复

### 13. 离线资源管理
- [ ] **任务**:
  - [ ] 实现资源下载功能
  - [ ] 添加下载进度显示
  - [ ] 实现存储空间管理
  - [ ] 添加资源更新检查

### 14. 代码质量提升
- [ ] **任务**:
  - [ ] 添加单元测试（ViewModel、Repository）
  - [ ] 添加UI测试（关键流程）
  - [ ] 完善代码注释
  - [ ] 优化错误处理
  - [ ] 添加日志系统

### 15. 性能优化
- [ ] **任务**:
  - [ ] 实现音频预加载
  - [ ] 优化图片缓存
  - [ ] 优化数据库查询
  - [ ] 检查并修复内存泄漏
  - [ ] 性能分析工具集成

### 16. 可访问性支持
- [ ] **任务**:
  - [ ] 添加所有UI元素的内容描述
  - [ ] 支持TalkBack
  - [ ] 实现键盘导航
  - [ ] 添加高对比度模式

## 📋 功能细化说明

### LRC同步算法改进方案

当前算法问题：
```kotlin
// 当前实现：简单线性查找
for (i in segments.indices) {
    val segment = segments[i]
    if (segment.startTime <= positionSeconds) {
        bestMatchIndex = i
    } else {
        break
    }
}
```

改进方案：
```kotlin
// 改进：考虑时间窗口和容差
fun updatePosition(positionSeconds: Double) {
    val tolerance = 0.5 // 容差0.5秒
    var bestMatchIndex = -1
    var bestMatchScore = Double.MAX_VALUE
    
    for (i in segments.indices) {
        val segment = segments[i]
        val startTime = segment.startTime
        val endTime = segment.endTime.let { 
            if (it > 0) it else startTime + 5.0 // 默认5秒
        }
        
        // 计算匹配分数
        val score = when {
            positionSeconds >= startTime - tolerance && 
            positionSeconds <= endTime + tolerance -> {
                // 在时间窗口内，计算距离中心的距离
                val center = (startTime + endTime) / 2
                abs(positionSeconds - center)
            }
            else -> Double.MAX_VALUE
        }
        
        if (score < bestMatchScore) {
            bestMatchScore = score
            bestMatchIndex = i
        }
    }
    
    if (bestMatchIndex >= 0 && bestMatchIndex != _currentSegmentIndex.value) {
        _currentSegmentIndex.value = bestMatchIndex
    }
}
```

### 音频分段播放实现方案

```kotlin
// 在AudioManager中添加
fun playSegment(startTimeMs: Long, endTimeMs: Long, loop: Boolean = false) {
    seekTo(startTimeMs)
    play()
    
    // 监听播放位置，到达endTime时处理
    exoPlayer.addListener(object : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (exoPlayer.currentPosition >= endTimeMs) {
                if (loop) {
                    seekTo(startTimeMs)
                } else {
                    pause()
                }
            }
        }
    })
}
```

## 📝 注意事项

1. **时间同步问题**是最紧急的，直接影响用户体验
2. **后台播放**需要Android权限和前台服务配置
3. **MediaSession**需要Android 5.0+支持
4. 所有新功能都需要考虑向后兼容性
5. 建议先修复P0问题，再进行功能扩展
