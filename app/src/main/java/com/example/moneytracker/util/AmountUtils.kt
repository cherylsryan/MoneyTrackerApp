package com.example.moneytracker.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Typical Supabase 'numeric(10,2)' supports up to 8 digits before decimal.
const val MAX_AMOUNT = 99_999_999.99

/**
 * Converts a Double amount into a plain, comma-free numeric string suitable
 * as the raw value behind an amount TextField. Avoids Kotlin's default
 * scientific notation (e.g. "8.9999999E7") for large values.
 */
fun formatAmountForInput(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        BigDecimal(amount)
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    }
}

/**
 * For read-only display text (not editable fields), e.g. "RM 8,000.00".
 */
fun formatCurrencyDisplay(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = DecimalFormat("#,##0.00", symbols)
    return formatter.format(amount)
}

/**
 * Inserts thousands separators into the integer part of a raw digit string,
 * for DISPLAY only. The underlying TextField value stays comma-free, so
 * amount.toDoubleOrNull() keeps working normally on the raw state.
 */
class ThousandsSeparatorVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val dotIndex = raw.indexOf('.')
        val integerPart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
        val decimalPart = if (dotIndex >= 0) raw.substring(dotIndex) else "" // includes the dot

        val len = integerPart.length
        val formattedBuilder = StringBuilder()
        val rawToFormattedLength = IntArray(len + 1)

        for (i in integerPart.indices) {
            rawToFormattedLength[i] = formattedBuilder.length
            formattedBuilder.append(integerPart[i])
            val posFromEnd = len - i
            val remaining = posFromEnd - 1
            if (remaining > 0 && remaining % 3 == 0) {
                formattedBuilder.append(',')
            }
        }
        rawToFormattedLength[len] = formattedBuilder.length

        val formatted = formattedBuilder.toString() + decimalPart
        val formattedIntegerLength = rawToFormattedLength[len]

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return if (clamped <= len) {
                    rawToFormattedLength[clamped]
                } else {
                    formattedIntegerLength + (clamped - len)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return if (clamped <= formattedIntegerLength) {
                    var rawCount = 0
                    for (i in 0..len) {
                        if (rawToFormattedLength[i] <= clamped) rawCount = i else break
                    }
                    rawCount
                } else {
                    len + (clamped - formattedIntegerLength)
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}