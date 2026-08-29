package com.example.moneytracker.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalContext
import com.example.moneytracker.model.Expense
import com.example.moneytracker.repository.ExpenseRepository
import com.example.moneytracker.util.MAX_AMOUNT
import com.example.moneytracker.util.ThousandsSeparatorVisualTransformation
import com.example.moneytracker.util.formatCurrencyDisplay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val expenseCategories = listOf("Food", "Shopping", "Transport", "Entertainment", "Bills", "Rent", "Others")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    var titleError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }
    var dateError by rememberSaveable { mutableStateOf<String?>(null) }

    var isSaving by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        titleError = if (title.isBlank()) "Title is required" else null
        val amountValue = amount.toDoubleOrNull()
        amountError = when {
            amount.isBlank() -> "Amount is required"
            amountValue == null -> "Amount must be a number"
            amountValue <= 0.0 -> "Amount must be greater than 0"
            amountValue > MAX_AMOUNT -> "Amount is too large (max RM ${formatCurrencyDisplay(MAX_AMOUNT)})"
            else -> null
        }
        categoryError = if (category.isBlank()) "Category is required" else null
        dateError = if (date.isBlank()) "Date is required" else null
        return titleError == null && amountError == null && categoryError == null && dateError == null
    }

    fun onSaveClick() {
        errorMessage = null
        if (!validate()) return
        val amountValue = amount.toDoubleOrNull() ?: return
        isSaving = true
        coroutineScope.launch {
            val newExpense = Expense(
                title = title.trim(),
                amount = amountValue,
                category = category,
                date = date,
                description = description.trim()
            )
            repository.addExpense(newExpense)
                .onSuccess {
                    isSaving = false
                    onNavigateBack()
                }
                .onFailure {
                    isSaving = false
                    errorMessage = it.message ?: "Failed to save expense"
                }
        }
    }

    val backgroundColor = Color(0xFFF6EFC9)
    val fieldBackgroundColor = Color.White
    val buttonColor = Color(0xFFE8D46A)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Expenses",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.size(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            isError = categoryError != null,
                            placeholder = { Text("eg. Food") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = fieldBackgroundColor,
                                unfocusedContainerColor = fieldBackgroundColor,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                errorBorderColor = Color.Red
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            expenseCategories.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    categoryError?.let { Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp)) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Remark Field (Title)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Remark", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        isError = titleError != null,
                        placeholder = { Text("eg. Chicken Rice") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = fieldBackgroundColor,
                            unfocusedContainerColor = fieldBackgroundColor,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            errorBorderColor = Color.Red
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    titleError?.let { Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp)) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Amount(RM)", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input ->
                            // Raw state stays comma-free digits + at most one dot.
                            // Commas are added purely for display via visualTransformation below.
                            val digitsOnly = input.filter { it.isDigit() || it == '.' }
                            val isValidShape = digitsOnly.isEmpty() || digitsOnly.count { it == '.' } <= 1
                            if (isValidShape) {
                                val candidate = digitsOnly.toDoubleOrNull()
                                if (candidate == null || candidate <= MAX_AMOUNT) {
                                    amount = digitsOnly
                                }
                            }
                        },
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        isError = amountError != null,
                        placeholder = { Text("eg. 3.95") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = fieldBackgroundColor,
                            unfocusedContainerColor = fieldBackgroundColor,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            errorBorderColor = Color.Red
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    amountError?.let { Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp)) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Date", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        readOnly = true,
                        isError = dateError != null,
                        placeholder = { Text("eg. 2026-07-17") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Pick Date")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = fieldBackgroundColor,
                            unfocusedContainerColor = fieldBackgroundColor,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            errorBorderColor = Color.Red
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(0.5f) // As per image, Date field is shorter
                    )
                    dateError?.let { Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp)) }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onSaveClick() },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .width(200.dp) // Adjusted to look like image
                        .height(48.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Text("Save", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it, color = Color.Red)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        date = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}