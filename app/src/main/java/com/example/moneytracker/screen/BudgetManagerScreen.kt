package com.example.moneytracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.Expense
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.repository.UserRepository
import com.example.moneytracker.repository.BudgetRepository
import com.example.moneytracker.repository.ExpenseRepository
import com.example.moneytracker.repository.SavingsGoalRepository
import com.example.moneytracker.util.MAX_AMOUNT
import com.example.moneytracker.util.ThousandsSeparatorVisualTransformation
import com.example.moneytracker.util.formatCurrencyDisplay
import kotlinx.coroutines.launch
import java.util.Locale

private val expenseCategories = listOf("Food", "Shopping", "Transport", "Entertainment", "Bills", "Rent", "Others")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagerScreen() {
    val context = LocalContext.current
    val budgetRepository = remember { BudgetRepository(context) }
    val expenseRepository = remember { ExpenseRepository(context) }
    val savingsGoalRepository = remember { SavingsGoalRepository(context) }
    val userRepository = remember { UserRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val sessionStatus by UserRepository.isLoggedIn.collectAsState()
    
    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
    var savingsGoals by remember { mutableStateOf<List<SavingsGoal>>(emptyList()) }
    var allExpenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    
    var showAddBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showAddGoalDialog by rememberSaveable { mutableStateOf(false) }
    var budgetToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var goalToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    
    var budgetToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var goalToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }

    val budgetToEdit = budgets.find { it.id == budgetToEditId }
    val goalToEdit = savingsGoals.find { it.id == goalToEditId }
    val budgetToDelete = budgets.find { it.id == budgetToDeleteId }
    val goalToDelete = savingsGoals.find { it.id == goalToDeleteId }

    fun loadData() {
        if (!userRepository.isUserLoggedIn()) return
        
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            
            val budgetResult = budgetRepository.getBudgets()
            val expenseResult = expenseRepository.getExpenses()
            val goalResult = savingsGoalRepository.getSavingsGoals()
            
            if (budgetResult.isSuccess && expenseResult.isSuccess && goalResult.isSuccess) {
                budgets = budgetResult.getOrThrow()
                allExpenses = expenseResult.getOrThrow()
                savingsGoals = goalResult.getOrThrow()
            } else {
                errorMessage = "Failed to load data"
            }
            isLoading = false
        }
    }

    LaunchedEffect(sessionStatus) {
        loadData()
    }

    val backgroundColor = Color(0xFFF6EFC9)

    Scaffold(
        containerColor = backgroundColor,
        topBar = { 
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Budget & Savings Hub",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A237E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
            ) 
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Your Budgets",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (isLoading && budgets.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF1A237E)) } }
            } else {
                items(budgets) { budget ->
                    val monthlyExpenses = allExpenses.filter { it.date.startsWith(budget.month) && (it.category == budget.category || budget.category == "Others") }
                    val totalSpent = monthlyExpenses.sumOf { it.amount }
                    BudgetHubCard(
                        budget = budget,
                        totalSpent = totalSpent,
                        onEdit = { budgetToEditId = budget.id },
                        onDelete = { budgetToDeleteId = budget.id }
                    )
                }

                item {
                    HubActionButton(
                        text = "Create New Budget",
                        onClick = { showAddBudgetDialog = true }
                    )
                }
            }

            item {
                Text(
                    "Savings Goals",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            if (isLoading && savingsGoals.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF1A237E)) } }
            } else {
                val chunkedGoals = savingsGoals.chunked(2)
                chunkedGoals.forEach { rowGoals ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowGoals.forEach { goal ->
                                SavingsGoalCard(
                                    goal = goal,
                                    modifier = Modifier.weight(1f),
                                    onEdit = { goalToEditId = goal.id },
                                    onDelete = { goalToDeleteId = goal.id }
                                )
                            }
                            if (rowGoals.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    HubActionButton(
                        text = "Add Savings Goal",
                        onClick = { showAddGoalDialog = true }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Dialogs
    if (showAddBudgetDialog) {
        BudgetHubDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { name, category, amount, month, alert ->
                coroutineScope.launch {
                    budgetRepository.addBudget(Budget(name = name, category = category, amount = amount, month = month, overspendAlert = alert))
                        .onSuccess { loadData() }
                    showAddBudgetDialog = false
                }
            }
        )
    }

    budgetToEdit?.let { budget ->
        BudgetHubDialog(
            initialName = budget.name,
            initialCategory = budget.category,
            initialAmount = com.example.moneytracker.util.formatAmountForInput(budget.amount),
            initialMonth = budget.month,
            initialAlert = budget.overspendAlert,
            isEdit = true,
            onDismiss = { budgetToEditId = null },
            onConfirm = { name, category, amount, month, alert ->
                coroutineScope.launch {
                    budgetRepository.updateBudget(budget.copy(name = name, category = category, amount = amount, month = month, overspendAlert = alert))
                        .onSuccess { loadData() }
                    budgetToEditId = null
                }
            }
        )
    }

    if (showAddGoalDialog) {
        SavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, target, current ->
                coroutineScope.launch {
                    savingsGoalRepository.addSavingsGoal(SavingsGoal(title = title, targetAmount = target, currentAmount = current))
                        .onSuccess { loadData() }
                    showAddGoalDialog = false
                }
            }
        )
    }

    goalToEdit?.let { goal ->
        SavingsGoalDialog(
            initialTitle = goal.title,
            initialTarget = com.example.moneytracker.util.formatAmountForInput(goal.targetAmount),
            initialCurrent = com.example.moneytracker.util.formatAmountForInput(goal.currentAmount),
            isEdit = true,
            onDismiss = { goalToEditId = null },
            onConfirm = { title, target, current ->
                coroutineScope.launch {
                    savingsGoalRepository.updateSavingsGoal(goal.copy(title = title, targetAmount = target, currentAmount = current))
                        .onSuccess { loadData() }
                    goalToEditId = null
                }
            }
        )
    }

    // Deletion Alerts
    budgetToDelete?.let { budget ->
        val cleanedName = budget.name.replace("May ", "", ignoreCase = true)
            .replace("electricity fees", "Bills", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { budgetToDeleteId = null },
            title = { Text("Delete Budget") },
            text = { Text("Delete budget for $cleanedName?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        budget.id?.let { budgetRepository.deleteBudget(it) }?.onSuccess { loadData() }
                        budgetToDeleteId = null
                    }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { budgetToDeleteId = null }) { Text("Cancel") } }
        )
    }
    
    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDeleteId = null },
            title = { Text("Delete Goal") },
            text = { Text("Delete savings goal \"${goal.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        goal.id?.let { savingsGoalRepository.deleteSavingsGoal(it) }?.onSuccess { loadData() }
                        goalToDeleteId = null
                    }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { goalToDeleteId = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun BudgetHubCard(
    budget: Budget,
    totalSpent: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (budget.amount > 0) (totalSpent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val goldColor = Color(0xFFE8D46A)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(budget.category),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val cleanedName = budget.name.replace("May ", "", ignoreCase = true)
                    .replace("electricity fees", "Bills", ignoreCase = true)
                Text(cleanedName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(
                    text = "RM${formatCurrencyDisplay(totalSpent)} / RM${formatCurrencyDisplay(budget.amount)}, ${(progress * 100).toInt()}% used",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = if (progress > 0.9f) Color.Red else goldColor,
                    trackColor = Color(0xFFF5F5F5),
                    strokeCap = StrokeCap.Round
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoal,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val goldColor = Color(0xFFE8D46A)

    Card(
        modifier = modifier.clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.LightGray)
                }
            }
            
            Icon(
                imageVector = if (goal.title.contains("Laptop", true)) Icons.Default.Laptop else Icons.Default.Savings,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                goal.title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 15.sp, 
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "RM${formatCurrencyDisplay(goal.currentAmount)} / RM${formatCurrencyDisplay(goal.targetAmount)}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF66BB6A), // Use green for savings
                trackColor = Color(0xFFF5F5F5),
                strokeCap = StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text("${(progress * 100).toInt()}% saved", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun HubActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetHubDialog(
    initialName: String = "",
    initialCategory: String = "Shopping",
    initialAmount: String = "",
    initialMonth: String = "",
    initialAlert: Boolean = true,
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Boolean) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var amount by rememberSaveable { mutableStateOf(initialAmount) }
    var month by rememberSaveable { mutableStateOf(if (initialMonth.isEmpty()) {
        val now = java.util.Calendar.getInstance()
        "%d-%02d".format(now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH) + 1)
    } else initialMonth) }
    var alert by rememberSaveable { mutableStateOf(initialAlert) }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val goldColor = Color(0xFFE8D46A)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF6EFC9), // Matches design
        title = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isEdit) "Edit Budget" else "Create New Budget", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogField("Budget Name", name, { name = it }, Icons.Default.ShoppingCart, "e.g., Entertainment")
                DialogField(
                    label = "Budget Amount (RM)",
                    value = amount,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() || it == '.' }
                        val isValidShape = digitsOnly.isEmpty() || digitsOnly.count { it == '.' } <= 1
                        if (isValidShape) {
                            val candidate = digitsOnly.toDoubleOrNull()
                            if (candidate == null || candidate <= MAX_AMOUNT) {
                                amount = digitsOnly
                            }
                        }
                    },
                    icon = Icons.Default.Savings,
                    placeholder = "e.g., 1,000.00",
                    keyboardType = KeyboardType.Decimal,
                    visualTransformation = ThousandsSeparatorVisualTransformation()
                )
                
                Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(getCategoryIcon(category), null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(type = androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedContainerColor = Color(0xFFEBEBEB), unfocusedContainerColor = Color(0xFFEBEBEB))
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Row { Icon(getCategoryIcon(cat), null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(cat) } },
                                onClick = { category = cat; categoryExpanded = false }
                            )
                        }
                    }
                }

                DialogField("Month (YYYY-MM)", month, { month = it }, Icons.Default.Home, "2026-08")

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Set Overspend Alert", fontWeight = FontWeight.Medium)
                    }
                    Switch(checked = alert, onCheckedChange = { alert = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = goldColor))
                }
                
                error?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal != null && amountVal > 0 && amountVal <= MAX_AMOUNT && name.isNotBlank() && month.matches(Regex("\\d{4}-\\d{2}"))) {
                        onConfirm(name, category, amountVal, month, alert)
                    } else if (amountVal != null && amountVal > MAX_AMOUNT) {
                        error = "Amount is too large (max RM ${formatCurrencyDisplay(MAX_AMOUNT)})"
                    } else {
                        error = "Please fill all fields correctly."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black)
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Red)
            }
        }
    )
}

@Composable
fun SavingsGoalDialog(
    initialTitle: String = "",
    initialTarget: String = "",
    initialCurrent: String = "0",
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var target by rememberSaveable { mutableStateOf(initialTarget) }
    var current by rememberSaveable { mutableStateOf(initialCurrent) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val goldColor = Color(0xFFE8D46A)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF6EFC9),
        title = { Text(if (isEdit) "Edit Goal" else "Add Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogField("Goal Title", title, { title = it }, Icons.Default.Savings, "e.g., New Laptop")
                DialogField(
                    label = "Target Amount (RM)",
                    value = target,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() || it == '.' }
                        val isValidShape = digitsOnly.isEmpty() || digitsOnly.count { it == '.' } <= 1
                        if (isValidShape) {
                            val candidate = digitsOnly.toDoubleOrNull()
                            if (candidate == null || candidate <= MAX_AMOUNT) {
                                target = digitsOnly
                            }
                        }
                    },
                    icon = Icons.Default.Savings,
                    placeholder = "5,000.00",
                    keyboardType = KeyboardType.Decimal,
                    visualTransformation = ThousandsSeparatorVisualTransformation()
                )
                DialogField(
                    label = "Current Savings (RM)",
                    value = current,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() || it == '.' }
                        val isValidShape = digitsOnly.isEmpty() || digitsOnly.count { it == '.' } <= 1
                        if (isValidShape) {
                            val candidate = digitsOnly.toDoubleOrNull()
                            if (candidate == null || candidate <= MAX_AMOUNT) {
                                current = digitsOnly
                            }
                        }
                    },
                    icon = Icons.Default.Savings,
                    placeholder = "1,500.00",
                    keyboardType = KeyboardType.Decimal,
                    visualTransformation = ThousandsSeparatorVisualTransformation()
                )
                error?.let { Text(it, color = Color.Red) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetVal = target.toDoubleOrNull()
                    val currentVal = current.toDoubleOrNull() ?: 0.0
                    if (targetVal != null && targetVal > 0 && targetVal <= MAX_AMOUNT && currentVal <= MAX_AMOUNT && title.isNotBlank()) {
                        onConfirm(title, targetVal, currentVal)
                    } else if ((targetVal != null && targetVal > MAX_AMOUNT) || currentVal > MAX_AMOUNT) {
                        error = "Amount is too large (max RM ${formatCurrencyDisplay(MAX_AMOUNT)})"
                    } else {
                        error = "Please enter valid title and amount."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black)
            ) { Text("Save Goal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = { 
                Box(Modifier.size(40.dp).padding(4.dp).background(Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(24.dp))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedContainerColor = Color(0xFFEBEBEB), unfocusedContainerColor = Color(0xFFEBEBEB), focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
        )
    }
}
