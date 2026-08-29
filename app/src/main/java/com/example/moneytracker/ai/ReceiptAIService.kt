package com.example.moneytracker.ai

import android.util.Base64
import com.example.moneytracker.model.Receipt
import com.example.moneytracker.model.ReceiptItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Sends a receipt photo to Google Gemini and converts the response into a Receipt.
 */
class ReceiptAIService(private val apiKey: String) {

    private val client = OkHttpClient()

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=$apiKey"

    private val instructions = """
        Read this receipt photo and reply with ONLY this JSON, nothing else, no markdown:
        {
          "store": "store name",
          "date": "yyyy-MM-dd",
          "total": 0.00,
          "items": [
            {
              "qty": 1,
              "name": "item name",
              "description": "options if any",
              "amount": 0.00
            }
          ]
        }
    """.trimIndent()

    fun scanReceipt(imageBytes: ByteArray): Receipt {
        val base64Image =
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val textPart = JSONObject()
            .put("text", instructions)

        val imagePart = JSONObject()
            .put(
                "inline_data",
                JSONObject()
                    .put("mime_type", "image/jpeg")
                    .put("data", base64Image)
            )

        val parts = JSONArray()
            .put(textPart)
            .put(imagePart)

        val contentItem = JSONObject()
            .put("parts", parts)

        val contents = JSONArray()
            .put(contentItem)

        val requestJson = JSONObject()
            .put("contents", contents)

        val body = requestJson
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        client.newCall(request).execute().use { response: Response ->

            if (!response.isSuccessful) {
                throw IOException(
                    "AI request failed: ${response.code} — ${response.body?.string()}"
                )
            }

            val responseText =
                response.body?.string().orEmpty()

            val aiReply = JSONObject(responseText)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            return parseReceiptJson(aiReply)
        }
    }

    private fun parseReceiptJson(text: String): Receipt {

        val json = JSONObject(text)

        val itemsArray =
            json.getJSONArray("items")

        val items = mutableListOf<ReceiptItem>()

        for (i in 0 until itemsArray.length()) {

            val itemJson =
                itemsArray.getJSONObject(i)

            items.add(
                ReceiptItem(
                    name = itemJson.getString("name"),
                    description = itemJson.optString(
                        "description",
                        ""
                    ),
                    qty = itemJson.optInt(
                        "qty",
                        1
                    ),
                    amount = itemJson.getDouble(
                        "amount"
                    )
                )
            )
        }

        return Receipt(
            storeName = json.getString("store"),
            date = json.getString("date"),
            totalAmount = json.getDouble("total"),
            items = items
        )
    }
}