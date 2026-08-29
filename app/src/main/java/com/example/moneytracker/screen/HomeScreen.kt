package com.example.moneytracker.screen

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytracker.model.Expense
import com.example.moneytracker.repository.UserRepository
import com.example.moneytracker.util.formatCurrencyDisplay
import com.example.moneytracker.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onAIScannerClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val isLoggedIn by UserRepository.isLoggedIn.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val backgroundColor = Color(0xFFF6EFC9)

    // Refresh data every time we navigate to this screen
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Always visible in both orientations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Money Tracker",
                        fontSize = if (isLandscape) 28.sp else 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Track smarter. Save better.",
                        fontSize = 16.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Black
                    )
                }
            }

            // Daily AI Tip: Always visible
            if (uiState.dailyTip.isNotEmpty()) {
                DailyTipCard(uiState.dailyTip)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dashboard Cards
            if (isLandscape) {
                // 2x2 Grid for Landscape
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TodaySpendingCard(uiState.todaySpending)
                        RecentExpensesCard(uiState.recentExpenses)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        BudgetProgressCard(uiState.monthlySpent, uiState.monthlyBudget)
                        SavingProgressCard(uiState.currentSavings, uiState.targetSavings)
                    }
                }
            } else {
                // Single Column for Portrait
                TodaySpendingCard(uiState.todaySpending)
                BudgetProgressCard(uiState.monthlySpent, uiState.monthlyBudget)
                SavingProgressCard(uiState.currentSavings, uiState.targetSavings)
                RecentExpensesCard(uiState.recentExpenses)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scan Receipt Button
            ScanReceiptButton(isLoggedIn, onAIScannerClick, onProfileClick)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DailyTipCard(tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD).copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, Color(0xFF1A237E).copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF1A237E),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Daily AI Tip",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1A237E)
                )
                Text(
                    tip,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun TodaySpendingCard(amount: Double) {
    HomeCard {
        Column {
            Text("Today's Spending", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                "RM ${formatCurrencyDisplay(amount)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (amount > 300) Color(0xFFD32F2F) else Color(0xFF1A237E)
            )
        }
    }
}

@Composable
fun BudgetProgressCard(spent: Double, budget: Double) {
    val progress = if (budget > 0) spent / budget else 0.0
    HomeCard {
        Column {
            Text("Monthly Budget Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("RM${formatCurrencyDisplay(spent)} / RM${formatCurrencyDisplay(budget)} (${(progress * 100).toInt()}%)", fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)),
                color = Color(0xFFFFB300),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SavingProgressCard(current: Double, target: Double) {
    val progress = if (target > 0) current / target else 0.0
    HomeCard {
        Column {
            Text("Monthly Saving Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("RM${formatCurrencyDisplay(current)} / RM${formatCurrencyDisplay(target)} (${(progress * 100).toInt()}%)", fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)),
                color = Color(0xFFFFB300),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun RecentExpensesCard(expenses: List<Expense>) {
    HomeCard {
        Column {
            Text("Recent expenses", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            if (expenses.isEmpty()) {
                Text("No recent expenses", color = Color.Gray, fontSize = 14.sp)
            } else {
                expenses.take(3).forEach { expense ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${expense.title}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("RM${formatCurrencyDisplay(expense.amount)}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ScanReceiptButton(isLoggedIn: Boolean, onAIScannerClick: () -> Unit, onProfileClick: () -> Unit) {
    Button(
        onClick = { if (isLoggedIn) onAIScannerClick() else onProfileClick() },
        modifier = Modifier.wrapContentSize().height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8D46A),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(26.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Scan Receipt", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HomeCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
