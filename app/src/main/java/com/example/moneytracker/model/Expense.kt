package com.example.moneytracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String = ""
)

/**
 * Used only when inserting a new row, so we never send an "id" field
 * to Supabase (the database generates it automatically).
 */
@Serializable
data class NewExpense(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val description: String = ""
)