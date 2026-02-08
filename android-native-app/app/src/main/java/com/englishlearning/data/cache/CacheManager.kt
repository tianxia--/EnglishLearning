package com.englishlearning.data.cache

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 缓存管理器 - 管理应用缓存
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CacheManager"
    }

    /**
     * 获取缓存大小
     */
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        var size = 0L

        // 计算应用缓存目录大小
        context.cacheDir?.let {
            size += getDirSize(it)
        }

        // 计算外部缓存目录大小
        context.externalCacheDir?.let {
            size += getDirSize(it)
        }

        size
    }

    /**
     * 清除所有缓存
     */
    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            var success = true

            // 清除内部缓存
            context.cacheDir?.let { cacheDir ->
                val deleted = deleteDir(cacheDir)
                if (!deleted) {
                    Log.w(TAG, "Failed to delete internal cache")
                    success = false
                } else {
                    Log.d(TAG, "Internal cache cleared")
                }
            }

            // 清除外部缓存
            context.externalCacheDir?.let { externalCacheDir ->
                val deleted = deleteDir(externalCacheDir)
                if (!deleted) {
                    Log.w(TAG, "Failed to delete external cache")
                    success = false
                } else {
                    Log.d(TAG, "External cache cleared")
                }
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
            false
        }
    }

    /**
     * 递归计算目录大小
     */
    private fun getDirSize(dir: File): Long {
        var size = 0L
        try {
            val files = dir.listFiles()
            files?.forEach { file ->
                size += if (file.isFile) {
                    file.length()
                } else {
                    getDirSize(file)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating directory size", e)
        }
        return size
    }

    /**
     * 递归删除目录
     */
    private fun deleteDir(dir: File): Boolean {
        try {
            val files = dir.listFiles()
            files?.forEach { file ->
                if (file.isDirectory) {
                    deleteDir(file)
                }
                file.delete()
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting directory", e)
            return false
        }
    }

    /**
     * 格式化文件大小
     */
    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()

        return String.format(
            "%.1f %s",
            size / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}
