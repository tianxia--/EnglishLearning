package com.englishlearning.utils

import java.util.Locale
import kotlin.math.max
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringUtils @Inject constructor() {

    /**
     * Calculate similarity score (0-100) between two strings
     */
    fun calculateSimilarity(target: String, spoken: String): Float {
        val normalizedTarget = normalize(target)
        val normalizedSpoken = normalize(spoken)

        if (normalizedTarget.isEmpty()) return 0f
        if (normalizedSpoken.isEmpty()) return 0f

        val distance = levenshteinDistance(normalizedTarget, normalizedSpoken)
        val maxLength = max(normalizedTarget.length, normalizedSpoken.length)

        val similarity = 1.0f - (distance.toFloat() / maxLength.toFloat())
        return (similarity * 100).coerceIn(0f, 100f)
    }

    private fun normalize(text: String): String {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9 ]"), "") // Remove punctuation
            .replace(Regex("\\s+"), " ") // Collpase whitespace
            .trim()
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                if (s1[i - 1] == s2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(
                        dp[i - 1][j],    // Deletion
                        dp[i][j - 1],    // Insertion
                        dp[i - 1][j - 1] // Substitution
                    )
                }
            }
        }
        return dp[m][n]
    }
}
