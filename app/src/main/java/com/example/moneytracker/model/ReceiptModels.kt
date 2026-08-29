package com.example.moneytracker.model

data class ReceiptItem(
    val name: String,
    val description: String = "",
    val qty: Int = 1,
    val amount: Double = 0.0
)

data class Receipt(
    val id: String = "",
    val storeName: String = "",
    val date: String = "",
    val totalAmount: Double = 0.0,
    val imageUrl: String = "",
    val items: List<ReceiptItem> = emptyList()
)

sealed class ScanState {
    object Idle : ScanState()
    object Processing : ScanState()
    object Saving : ScanState()
    data class Success(val receipt: Receipt) : ScanState()
    object Saved : ScanState()
    data class Error(val message: String) : ScanState()
}