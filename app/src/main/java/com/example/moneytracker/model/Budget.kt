package com.example.moneytracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val name: String,
    val category: String,
    val amount: Double,
    val month: String, // format: YYYY-MM
    @SerialName("overspend_alert")
    val overspendAlert: Boolean = true
)

@Serializable
data class NewBudget(
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val category: String,
    val amount: Double,
    val month: String,
    @SerialName("overspend_alert")
    val overspendAlert: Boolean = true
)
