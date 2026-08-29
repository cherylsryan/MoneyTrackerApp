package com.example.moneytracker.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.model.Expense
import com.example.moneytracker.repository.ExpenseRepository
import com.example.moneytracker.repository.UserRepository
import com.example.moneytracker.util.MAX_AMOUNT
import com.example.moneytracker.util.ThousandsSeparatorVisualTransformation
import com.example.moneytracker.util.formatAmountForInput
import com.example.moneytracker.util.formatCurrencyDisplay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val expenseCategories = listOf("Food", "Shopping", "Transport", "Entertainment", "Bills", "Rent", "Others")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListScreen(
    onAddExpenseClick: () -> Unit, // This nav callback is now unused for adding
    onEditExpenseClick: (String) -> Unit, // This nav callback is now unused for editing
) {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(context) }
    val userRepository = remember { UserRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val sessionStatus by UserRepository.isLoggedIn.collectAsState()

    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    // Dialog state
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var expenseToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    val expenseToEdit = expenses.find { it.id == expenseToEditId }

    fun loadExpenses() {
        if (!userRepository.isUserLoggedIn()) {
            expenses = emptyList()
            errorMessage = "Please login to see your expenses."
            return
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            repository.getExpenses()
                .onSuccess {
                    expenses = it
                    isLoading = false
                }
                .onFailure {
                    errorMessage = it.message ?: "Failed to load expenses"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(sessionStatus) {
        loadExpenses()
    }

    val filteredExpenses = expenses.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val groupedExpenses = filteredExpenses.groupBy { it.date }
    val backgroundColor = Color(0xFFF6EFC9)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Expenses",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A237E)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1A237E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
                        }
                    }
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
                    .padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    placeholder = { Text("Search your spending...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFEEEEEE),
                        unfocusedBorderColor = Color(0xFFEEEEEE)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF1A237E))
                        }
                    }
                    !userRepository.isUserLoggedIn() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Please login to see your expenses.", color = Color.Gray)
                        }
                    }
                    errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = errorMessage ?: "Something went wrong", color = Color.Red)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { loadExpenses() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    filteredExpenses.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                val emptyIcon = when {
                                    searchQuery.isNotEmpty() -> Icons.Default.SearchOff
                                    else -> Icons.AutoMirrored.Filled.ReceiptLong
                                }
                                val emptyText = when {
                                    searchQuery.isNotEmpty() -> "No matches found for \"$searchQuery\""
                                    else -> "Your expense list is empty.\nTap the + button to get started!"
                                }

                                Icon(
                                    imageVector = emptyIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.Gray.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = emptyText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            groupedExpenses.forEach { (date, dailyExpenses) ->
                                val dayTotal = dailyExpenses.sumOf { it.amount }
                                stickyHeader {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(backgroundColor)
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = date,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.DarkGray
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFE8D46A).copy(alpha = 0.2f))
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "RM ${formatCurrencyDisplay(dayTotal)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                                items(dailyExpenses, key = { it.id ?: it.hashCode().toString() }) { expense ->
                                    ExpenseRow(
                                        expense = expense,
                                        onEditClick = { expenseToEditId = expense.id },
                                    ) {
                                        expenseToDelete = expense
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        ExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, category, date, description ->
                coroutineScope.launch {
                    val newExpense = Expense(title = title, amount = amount, category = category, date = date, description = description)
                    repository.addExpense(newExpense).onSuccess { loadExpenses() }
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Expense Dialog
    expenseToEdit?.let { expense ->
        ExpenseDialog(
            initialTitle = expense.title,
            initialAmount = formatAmountForInput(expense.amount),
            initialCategory = expense.category,
            initialDate = expense.date,
            initialDescription = expense.description,
            isEdit = true,
            onDismiss = { expenseToEditId = null },
            onConfirm = { title, amount, category, date, description ->
                coroutineScope.launch {
                    val updated = expense.copy(title = title, amount = amount, category = category, date = date, description = description)
                    repository.updateExpense(updated).onSuccess { loadExpenses() }
                    expenseToEditId = null
                }
            }
        )
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete \"${expense.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = expense.id
                        expenseToDelete = null
                        if (id != null) {
                            coroutineScope.launch {
                                repository.deleteExpense(id).onSuccess { loadExpenses() }
                            }
                        }
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDialog(
    initialTitle: String = "",
    initialAmount: String = "",
    initialCategory: String = "Food",
    initialDate: String = "",
    initialDescription: String = "",
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    // "amount" always stays comma-free raw digits — commas are added only
    // visually via ThousandsSeparatorVisualTransformation below.
    var amount by rememberSaveable { mutableStateOf(initialAmount) }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf(if (initialDate.isEmpty()) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    } else initialDate) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val goldColor = Color(0xFFE8D46A)
    val fieldBg = Color.White

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF6EFC9),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEdit) "Edit Expense" else "Add Expense", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category
                Column {
                    Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("eg. Food") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg)
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            expenseCategories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryExpanded = false })
                            }
                        }
                    }
                }

                // Remark (Title)
                Column {
                    Text("Remark", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("eg. Chicken Rice") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg)
                    )
                }

                // Amount
                Column {
                    Text("Amount(RM)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input ->
                            // Only digits and a single dot are ever stored in raw state.
                            // Commas the user might type are simply dropped here.
                            val digitsOnly = input.filter { it.isDigit() || it == '.' }
                            val isValidShape = digitsOnly.isEmpty() ||
                                    (digitsOnly.count { it == '.' } <= 1)
                            if (isValidShape) {
                                val candidate = digitsOnly.toDoubleOrNull()
                                if (candidate == null || candidate <= MAX_AMOUNT) {
                                    amount = digitsOnly
                                }
                            }
                        },
                        visualTransformation = ThousandsSeparatorVisualTransformation(),
                        placeholder = { Text("eg. 3.95") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = error != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg)
                    )
                }

                // Date
                Column {
                    Text("Date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("eg. 2026-07-17") },
                        trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.Add, null) } },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg)
                    )
                }

                error?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    when {
                        title.isBlank() || date.isEmpty() -> {
                            error = "Please fill all fields correctly."
                        }
                        amountVal == null || amountVal <= 0.0 -> {
                            error = "Please enter a valid amount greater than 0."
                        }
                        amountVal > MAX_AMOUNT -> {
                            error = "Amount is too large (max RM ${formatCurrencyDisplay(MAX_AMOUNT)})."
                        }
                        else -> {
                            error = null
                            onConfirm(title, amountVal, category, date, description)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                        date = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}