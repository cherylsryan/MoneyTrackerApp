package com.example.moneytracker.repository

import android.content.Context
import android.util.Log
import com.example.moneytracker.data.SupabaseClient
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.NewBudget
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class BudgetRepository(context: Context) {

    private val table = SupabaseClient.client.from("budgets")
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    suspend fun getBudgets(): Result<List<Budget>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val budgets = table
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("month", Order.DESCENDING)
                }
                .decodeList<Budget>()
            Result.success(budgets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBudget(budget: Budget): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val newBudget = NewBudget(
                userId = userId,
                name = budget.name,
                category = budget.category,
                amount = budget.amount,
                month = budget.month,
                overspendAlert = budget.overspendAlert
            )
            table.insert(newBudget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBudget(budget: Budget): Result<Unit> {
        val id = budget.id ?: return Result.failure(IllegalArgumentException("Budget id is required"))
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            table.update({
                set("name", budget.name)
                set("category", budget.category)
                set("amount", budget.amount)
                set("month", budget.month)
                set("overspend_alert", budget.overspendAlert)
            }) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Update failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBudget(budgetId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            table.delete {
                filter {
                    eq("id", budgetId)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Delete failed", e)
            Result.failure(e)
        }
    }
}
