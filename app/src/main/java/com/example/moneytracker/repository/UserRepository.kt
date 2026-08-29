package com.example.moneytracker.repository

import android.content.Context
import com.example.moneytracker.data.SupabaseClient
import com.example.moneytracker.model.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UserRepository(context: Context) {
    private val postgrest = SupabaseClient.client.postgrest
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private val _isLoggedIn = MutableStateFlow(false)
        val isLoggedIn = _isLoggedIn.asStateFlow()
    }


    init {
        _isLoggedIn.value = isUserLoggedIn()
    }

    suspend fun signUp(email: String, password: String, name: String = ""): Result<Unit> {
        return try {
            val existingUsers = postgrest.from("user")
                .select {
                    filter {
                        eq("email", email)
                    }
                }.decodeList<User>()

            if (existingUsers.isNotEmpty()) {
                return Result.failure(Exception("User already exists"))
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { email.substringBefore("@") },
                email = email,
                password = password
            )

            postgrest.from("user").insert(newUser)
            saveUserSession(newUser)
            _isLoggedIn.value = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            val users = postgrest.from("user")
                .select {
                    filter {
                        eq("email", email)
                        eq("password", password)
                    }
                }.decodeList<User>()

            if (users.isEmpty()) {
                return Result.failure(Exception("Invalid email or password"))
            }

            saveUserSession(users[0])
            _isLoggedIn.value = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUserSession(user: User) {
        sharedPreferences.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_name", user.name)
            apply()
        }
    }

    fun signOut(): Result<Unit> {
        sharedPreferences.edit().clear().apply()
        _isLoggedIn.value = false
        return Result.success(Unit)
    }

    fun getCurrentUserId(): String? = sharedPreferences.getString("user_id", null)
    fun getCurrentUserEmail(): String? = sharedPreferences.getString("user_email", null)
    fun getCurrentUserName(): String? = sharedPreferences.getString("user_name", null)

    suspend fun updateProfile(name: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
        return try {
            postgrest.from("user").update({
                set("name", name)
            }) {
                filter {
                    eq("id", userId)
                }
            }
            sharedPreferences.edit().putString("user_name", name).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean = getCurrentUserId() != null
}
