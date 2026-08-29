package com.example.moneytracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiReport(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val month: String,
    @SerialName("health_score")
    val healthScore: Int,
    val insight: String,
    @SerialName("total_expenses")
    val totalExpenses: Double,
    @SerialName("generated_at")
    val generatedAt: String
)

@Serializable
data class NewAiReport(
    @SerialName("user_id")
    val userId: String,
    val month: String,
    @SerialName("health_score")
    val healthScore: Int,
    val insight: String,
    @SerialName("total_expenses")
    val totalExpenses: Double,
    @SerialName("generated_at")
    val generatedAt: String
)
