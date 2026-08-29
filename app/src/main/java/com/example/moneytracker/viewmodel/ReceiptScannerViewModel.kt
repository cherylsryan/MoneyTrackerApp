package com.example.moneytracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.ai.ReceiptAIService
import com.example.moneytracker.model.Receipt
import com.example.moneytracker.model.ScanState
import com.example.moneytracker.repository.ReceiptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiptScannerViewModel(application: Application) : AndroidViewModel(application) {

    // TODO: Move this API key to local.properties before submitting.
    private val aiService = ReceiptAIService(
        apiKey = "AQ.Ab8RN6IjADtLc1Y1ZMxAyfXeI_pbtJQVrPHtoIZ9V7uK5_LO9g"
    )

    private val repository = ReceiptRepository(application)

    private val _state = mutableStateOf<ScanState>(ScanState.Idle)
    val state: State<ScanState> = _state

    private val _selectedImageUri = mutableStateOf<Uri?>(null)
    val selectedImageUri: State<Uri?> = _selectedImageUri

    private var imageBytes: ByteArray? = null

    fun setImage(uri: Uri, bytes: ByteArray) {
        _selectedImageUri.value = uri
        imageBytes = bytes
    }

    fun scanReceipt() {
        val bytes = imageBytes

        if (bytes == null) {
            _state.value = ScanState.Error(
                "Please upload a receipt photo first"
            )
            return
        }

        _state.value = ScanState.Processing

        viewModelScope.launch {
            try {
                val receipt = withContext(Dispatchers.IO) {
                    aiService.scanReceipt(bytes)
                }

                if (!isReceiptValid(receipt)) {
                    _state.value = ScanState.Error(
                        "Couldn't read a valid total from this receipt. Please try a clearer photo."
                    )
                    return@launch
                }

                _state.value = ScanState.Success(receipt)

            } catch (e: Exception) {
                _state.value = ScanState.Error(
                    e.message ?: "Could not read this receipt"
                )
            }
        }
    }

    /**
     * The AI can return valid JSON with placeholder values like
     * store/date = "null" (as a literal string) and total = 0.00
     * when it can't actually read the receipt. Catch that here.
     */
    private fun isReceiptValid(receipt: Receipt): Boolean {
        val hasValidTotal = receipt.totalAmount > 0.0
        val hasValidStore = receipt.storeName.isNotBlank() &&
                !receipt.storeName.equals("null", ignoreCase = true)
        val hasValidDate = receipt.date.isNotBlank() &&
                !receipt.date.equals("null", ignoreCase = true)

        return hasValidTotal && hasValidStore && hasValidDate
    }

    fun saveReceipt() {
        val currentState = _state.value

        if (currentState !is ScanState.Success) {
            return
        }

        // Defensive guard: never allow saving an invalid receipt,
        // even if something else set Success state directly.
        if (!isReceiptValid(currentState.receipt)) {
            _state.value = ScanState.Error(
                "This receipt has no valid total. Nothing to save."
            )
            return
        }

        val bytes = imageBytes ?: return

        _state.value = ScanState.Saving

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val imageUrl = repository.uploadImage(bytes)

                    repository.saveReceipt(
                        currentState.receipt,
                        imageUrl
                    )
                }

                _state.value = ScanState.Saved

            } catch (e: Exception) {
                _state.value = ScanState.Error(
                    e.message ?: "Could not save this receipt"
                )
            }
        }
    }

    fun reset() {
        imageBytes = null
        _selectedImageUri.value = null
        _state.value = ScanState.Idle
    }
}