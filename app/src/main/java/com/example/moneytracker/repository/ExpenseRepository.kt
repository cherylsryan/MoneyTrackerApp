package com.example.moneytracker.repository

import android.content.Context
import android.util.Log
import com.example.moneytracker.data.SupabaseClient
import com.example.moneytracker.model.Expense
import com.example.moneytracker.model.NewExpense
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ExpenseRepository(context: Context) {

    private val table = SupabaseClient.client.from("expenses")
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    suspend fun getExpenses(): Result<List<Expense>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val expenses = table
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("date", Order.DESCENDING)
                }
                .decodeList<Expense>()
            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenseById(expenseId: String): Result<Expense> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val expense = table
                .select {
                    filter {
                        eq("id", expenseId)
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<Expense>()
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpense(expense: Expense): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val newExpense = NewExpense(
                userId = userId,
                title = expense.title,
                amount = expense.amount,
                category = expense.category,
                date = expense.date,
                description = expense.description
            )
            table.insert(newExpense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> {
        val id = expense.id ?: return Result.failure(IllegalArgumentException("Expense id is required"))
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            // Use set() to specify only the fields to update, avoiding sending 'id' in the body
            table.update({
                set("title", expense.title)
                set("amount", expense.amount)
                set("category", expense.category)
                set("date", expense.date)
                set("description", expense.description)
            }) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Update failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        Log.d("ExpenseRepository", "Attempting to delete expense: $expenseId for user: $userId")
        return try {
            table.delete {
                filter {
                    eq("id", expenseId)
                    eq("user_id", userId)
                }
            }
            Log.d("ExpenseRepository", "Delete success")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Delete failed", e)
            Result.failure(e)
        }
    }
}