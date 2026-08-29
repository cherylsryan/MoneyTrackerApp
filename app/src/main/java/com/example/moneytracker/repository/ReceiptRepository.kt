package com.example.moneytracker.repository

import android.content.Context
import android.util.Log
import com.example.moneytracker.data.SupabaseClient
import com.example.moneytracker.model.NewExpense
import com.example.moneytracker.model.Receipt
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReceiptRepository(context: Context) {

    private val supabase = SupabaseClient.client
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    suspend fun uploadImage(imageBytes: ByteArray): String {
        try {
            val userId = getCurrentUserId()
                ?: throw IllegalStateException("No user is logged in")

            val fileName = "${userId}/${UUID.randomUUID()}.jpg"

            // Attempt to upload to "receipts" bucket. 
            // Note: If this fails, make sure you created a PUBLIC bucket named "receipts" in Supabase Storage.
            supabase.storage
                .from("receipts")
                .upload(fileName, imageBytes)

            val url = supabase.storage
                .from("receipts")
                .publicUrl(fileName)
            
            Log.d("ReceiptRepository", "Image uploaded successfully: $url")
            return url
        } catch (e: Exception) {
            Log.e("ReceiptRepository", "Error uploading image", e)
            throw Exception("Storage error: ${e.message}. Please ensure a public 'receipts' bucket exists.")
        }
    }

    suspend fun saveReceipt(
        receipt: Receipt,
        imageUrl: String
    ) {
        try {
            val userId = getCurrentUserId()
                ?: throw IllegalStateException("No user is logged in")

            val itemsSummary = receipt.items.joinToString("\n") { "• ${it.qty}x ${it.name} (RM ${"%.2f".format(it.amount)})" }
            
            val formattedDescription = buildString {
                append("🧾 Items:\n")
                append(itemsSummary)
                append("\n\n🖼️ View Receipt:\n")
                append(imageUrl)
            }

            // Fallback for date if AI failed to detect it or returned empty
            val finalDate = if (receipt.date.length >= 10 && receipt.date.contains("-")) {
                receipt.date 
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            // Map Receipt to Expense table
            val newExpense = NewExpense(
                userId = userId,
                title = receipt.storeName.ifBlank { "Scanned Receipt" },
                amount = receipt.totalAmount,
                category = "Others", 
                date = finalDate,
                description = formattedDescription
            )

            Log.d("ReceiptRepository", "Attempting to insert expense: $newExpense")

            // Insert into the 'expenses' table
            supabase.from("expenses").insert(newExpense)
            
            Log.d("ReceiptRepository", "Expense saved successfully")
        } catch (e: Exception) {
            Log.e("ReceiptRepository", "Error saving receipt to database", e)
            throw Exception("Database error: ${e.message}")
        }
    }
}