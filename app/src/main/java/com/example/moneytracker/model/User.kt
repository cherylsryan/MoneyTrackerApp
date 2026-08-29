package com.example.moneytracker.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val name: String,
    val email: String,
    val password: String? = null
)
