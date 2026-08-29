package com.example.moneytracker.repository

import android.content.Context
import com.example.moneytracker.data.SupabaseClient
import com.example.moneytracker.model.AiReport
import com.example.moneytracker.model.NewAiReport
import io.github.jan.supabase.postgrest.from
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AiReportRepository(context: Context) {

    private val table = SupabaseClient.client.from("ai_reports")
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    suspend fun saveReport(month: String, score: Int, insight: String, total: Double): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val report = NewAiReport(
                userId = userId,
                month = month,
                healthScore = score,
                insight = insight,
                totalExpenses = total,
                generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            table.insert(report)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSavedReports(): Result<List<AiReport>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val reports = table.select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<AiReport>()
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
