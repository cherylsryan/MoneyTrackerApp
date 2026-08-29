package com.example.moneytracker.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Shopping" -> Icons.Default.ShoppingCart
        "Transport" -> Icons.Default.DirectionsCar
        "Entertainment" -> Icons.Default.Star
        "Bills" -> Icons.Default.Lightbulb
        "Rent" -> Icons.Default.Home
        else -> Icons.Default.ShoppingCart
    }
}
