package com.example.moneytracker.ai

import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.Expense
import com.example.moneytracker.model.SavingsGoal
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service to generate AI insights and recommendations based on financial data using Google Gemini.
 */
class FinancialAIService(private val apiKey: String) {

    private val client = OkHttpClient()
    
    // Using the same model name as in ReceiptAIService
    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=$apiKey"

    suspend fun generateFinancialAnalysis(
        currentMonthTotal: Double,
        lastMonthTotal: Double,
        categoryTotals: Map<String, Double>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>
    ): FinancialAnalysisResponse = withContext(Dispatchers.IO) {
        val prompt = """
            You are a professional financial advisor. Analyze the following financial data and provide insights, recommendations, and a financial personality assessment.
            
            Current Month Total Spending: RM $currentMonthTotal
            Last Month Total Spending: RM $lastMonthTotal
            Spending by Category: ${categoryTotals.map { "${it.key}: RM ${it.value}" }.joinToString(", ")}
            Budgets: ${budgets.map { "${it.category}: RM ${it.amount}" }.joinToString(", ")}
            Savings Goals: ${goals.map { "${it.title}: Target RM ${it.targetAmount}, Current RM ${it.currentAmount}" }.joinToString(", ")}
            
            Return ONLY a JSON object with the following structure, no markdown:
            {
              "insight": "A brief summary of spending compared to last month.",
              "healthScore": 0 to 100 based on budget adherence and goals,
              "healthComment": "One concise sentence comment about the health score.",
              "predictedSpending": predicted total for current month as Double,
              "recommendations": [
                {
                  "title": "Short title",
                  "description": "Actionable advice",
                  "type": "WARNING" | "INFO" | "SUCCESS",
                  "targetRoute": "expenses" | "budget" | null
                }
              ],
              "financialPersonality": "A creative title like 'The Master Saver'",
              "personalityDescription": "A brief explanation of their spending habits."
            }
        """.trimIndent()

        val textPart = JSONObject().put("text", prompt)
        val contents = JSONArray().put(JSONObject().put("parts", JSONArray().put(textPart)))
        val requestJson = JSONObject().put("contents", contents)

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(endpoint).post(body).build()

        client.newCall(request).execute().use { response: Response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("AI request failed: ${response.code} - $responseBody")
            }

            val aiReply = JSONObject(responseBody)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(aiReply)
            val recommendationsArray = json.getJSONArray("recommendations")
            val recommendations = mutableListOf<Recommendation>()
            for (i in 0 until recommendationsArray.length()) {
                val recJson = recommendationsArray.getJSONObject(i)
                recommendations.add(
                    Recommendation(
                        title = recJson.getString("title"),
                        description = recJson.getString("description"),
                        type = RecommendationType.valueOf(recJson.getString("type")),
                        targetRoute = if (recJson.isNull("targetRoute")) null else recJson.optString("targetRoute", null)
                    )
                )
            }

            FinancialAnalysisResponse(
                insight = json.getString("insight"),
                healthScore = json.getInt("healthScore"),
                healthComment = json.optString("healthComment", "Analyze your spending to get tips."),
                predictedSpending = json.getDouble("predictedSpending"),
                recommendations = recommendations,
                financialPersonality = json.getString("financialPersonality"),
                personalityDescription = json.getString("personalityDescription")
            )
        }
    }

    suspend fun askAi(question: String, contextData: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            Context: $contextData
            Question: $question
            
            Instructions:
            1. You are a professional financial assistant. 
            2. Only answer questions related to personal finance, budgeting, spending habits, or the provided financial context.
            3. If the question is NOT related to money or finance, politely reply that you can only assist with financial matters and provide a tip on how to save money instead.
            4. Provide a concise and helpful answer.
        """.trimIndent()
        
        val textPart = JSONObject().put("text", prompt)
        val contents = JSONArray().put(JSONObject().put("parts", JSONArray().put(textPart)))
        val requestJson = JSONObject().put("contents", contents)

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(endpoint).post(body).build()

        try {
            client.newCall(request).execute().use { response: Response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext "AI unavailable (${response.code})"
                }

                JSONObject(responseBody)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            }
        } catch (e: Exception) {
            "Connection Error: ${e.message}"
        }
    }

    // Fallback methods
    fun generateInsight(
        currentMonthTotal: Double,
        lastMonthTotal: Double,
        currentMonth: String,
        lastMonth: String
    ): String {
        return "Spending: RM $currentMonthTotal."
    }

    fun getRecommendations(
        categoryTotals: Map<String, Double>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        healthScore: Int
    ): List<Recommendation> {
        return emptyList()
    }
}

data class FinancialAnalysisResponse(
    val insight: String,
    val healthScore: Int,
    val healthComment: String,
    val predictedSpending: Double,
    val recommendations: List<Recommendation>,
    val financialPersonality: String,
    val personalityDescription: String
)

data class Recommendation(
    val title: String,
    val description: String,
    val type: RecommendationType,
    val targetRoute: String? = null
)

enum class RecommendationType {
    WARNING, INFO, SUCCESS
}
