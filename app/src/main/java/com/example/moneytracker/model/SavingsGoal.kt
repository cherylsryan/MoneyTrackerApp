package com.example.moneytracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavingsGoal(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val title: String,
    @SerialName("target_amount")
    val targetAmount: Double,
    @SerialName("current_amount")
    val currentAmount: Double = 0.0,
    @SerialName("icon_name")
    val iconName: String = "Savings"
)

@Serializable
data class NewSavingsGoal(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    @SerialName("target_amount")
    val targetAmount: Double,
    @SerialName("current_amount")
    val currentAmount: Double = 0.0,
    @SerialName("icon_name")
    val iconName: String = "Savings"
)
