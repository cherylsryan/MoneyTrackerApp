package com.example.moneytracker.repository

import android.content.Context
import android.util.Log
import com.example.moneytracker.data.SupabaseClient
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.NewSavingsGoal
import io.github.jan.supabase.postgrest.from

class SavingsGoalRepository(context: Context) {

    private val table = SupabaseClient.client.from("savings_goals")
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    suspend fun getSavingsGoals(): Result<List<SavingsGoal>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val goals = table
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SavingsGoal>()
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val newGoal = NewSavingsGoal(
                userId = userId,
                title = goal.title,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                iconName = goal.iconName
            )
            table.insert(newGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal): Result<Unit> {
        val id = goal.id ?: return Result.failure(IllegalArgumentException("Goal id is required"))
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        
        return try {
            table.update({
                set("title", goal.title)
                set("target_amount", goal.targetAmount)
                set("current_amount", goal.currentAmount)
                set("icon_name", goal.iconName)
            }) {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SavingsGoalRepository", "Update failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return try {
            table.delete {
                filter {
                    eq("id", goalId)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SavingsGoalRepository", "Delete failed", e)
            Result.failure(e)
        }
    }
}
