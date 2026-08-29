package com.example.moneytracker.screen

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moneytracker.util.formatCurrencyDisplay
import com.example.moneytracker.viewmodel.AiReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiReportScreen(navController: NavController, viewModel: AiReportViewModel = viewModel()) {
    val backgroundColor = Color(0xFFF6EFC9)
    var selectedTab by remember { mutableStateOf("This Month") }
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Refresh data every time we navigate to this screen
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Financial Analysis",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = backgroundColor),
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tabs - Added more space for landscape
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(if (isLandscape) 32.dp else 12.dp))
                        .background(Color(0xFFD9D9D9))
                        .padding(if (isLandscape) 8.dp else 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 16.dp else 0.dp)
                ) {
                    TabItem(
                        "This Month", 
                        selectedTab == "This Month", 
                        fontSize = if (isLandscape) 18.sp else 14.sp,
                        width = if (isLandscape) 200.dp else 140.dp
                    ) { 
                        selectedTab = "This Month"
                        viewModel.selectTab("This Month")
                    }
                    TabItem(
                        "Last Month", 
                        selectedTab == "Last Month",
                        fontSize = if (isLandscape) 18.sp else 14.sp,
                        width = if (isLandscape) 200.dp else 140.dp
                    ) { 
                        selectedTab = "Last Month"
                        viewModel.selectTab("Last Month")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLandscape) {
                    // Landscape Grid with Equal Heights
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { SpendingSummaryCard(selectedTab, uiState, Modifier.fillMaxHeight()) }
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { MonthlyComparisonCard(selectedTab, uiState, Modifier.fillMaxHeight()) }
                        }
                        
                        if (selectedTab == "This Month" && uiState.totalExpensesForMonth > 0) {
                            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { PredictionCard(uiState, Modifier.fillMaxHeight()) }
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { HabitsCard(uiState, Modifier.fillMaxHeight()) }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { AiInsightCard(selectedTab, uiState, Modifier.fillMaxHeight()) }
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { HealthScoreCard(selectedTab, uiState, viewModel, Modifier.fillMaxHeight()) }
                        }
                    }
                } else {
                    // Portrait
                    SpendingSummaryCard(selectedTab, uiState)
                    Spacer(modifier = Modifier.height(16.dp))
                    MonthlyComparisonCard(selectedTab, uiState)
                    
                    if (selectedTab == "This Month" && uiState.totalExpensesForMonth > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        PredictionCard(uiState)
                        Spacer(modifier = Modifier.height(16.dp))
                        HabitsCard(uiState)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    AiInsightCard(selectedTab, uiState)
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthScoreCard(selectedTab, uiState, viewModel)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedTab == "This Month") {
                    Button(
                        onClick = { navController.navigate("smart_planner") },
                        modifier = Modifier
                            .fillMaxWidth(if (isLandscape) 0.4f else 0.8f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "View Recommendations", color = Color.Black, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SpendingSummaryCard(selectedTab: String, uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    AnalysisCard(title = "1. Current Spending Summary", modifier = modifier) {
        val categoryTotals = if (selectedTab == "Last Month") {
            if (uiState.lastMonthCategoryTotals.isNotEmpty()) uiState.lastMonthCategoryTotals 
            else mapOf("Food" to 300.0, "Groceries" to 200.0, "Transport" to 150.0, "Bills" to 120.0, "Entertainment" to 50.0)
        } else {
            uiState.categoryTotals
        }
        
        val totals = categoryTotals.values.toList()
        val totalSum = totals.sum()
        val colors = listOf(Color(0xFFFFA726), Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFF7E57C2), Color(0xFFFFEE58), Color(0xFFEC407A))

        if (isLandscape) {
            // Landscape: Chart Above, Details Below
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    if (totalSum > 0) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            totals.forEachIndexed { index, amount ->
                                val sweepAngle = (amount.toFloat() / totalSum.toFloat()) * 360f
                                drawArc(colors[index % colors.size], startAngle, sweepAngle, true)
                                startAngle += sweepAngle
                            }
                        }
                    } else {
                        Text("No data", fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    categoryTotals.entries.take(5).forEachIndexed { index, entry ->
                        val percent = if (totalSum > 0) (entry.value / totalSum * 100).toInt() else 0
                        LegendItem(colors[index % colors.size], entry.key, "RM ${formatCurrencyDisplay(entry.value)} ($percent%)", fontSize = 14.sp)
                    }
                }
            }
        } else {
            // Portrait: Side by side
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    if (totalSum > 0) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            totals.forEachIndexed { index, amount ->
                                val sweepAngle = (amount.toFloat() / totalSum.toFloat()) * 360f
                                drawArc(colors[index % colors.size], startAngle, sweepAngle, true)
                                startAngle += sweepAngle
                            }
                        }
                    } else {
                        Text("No data", fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    categoryTotals.entries.take(5).forEachIndexed { index, entry ->
                        val percent = if (totalSum > 0) (entry.value / totalSum * 100).toInt() else 0
                        LegendItem(colors[index % colors.size], entry.key, "RM ${formatCurrencyDisplay(entry.value)} ($percent%)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyComparisonCard(selectedTab: String, uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    AnalysisCard(title = "2. Monthly Comparison", subtitle = if (isLandscape) null else "(Total Expenses)", modifier = modifier) {
        if (isLandscape) {
            Text(text = "(Total Expenses)", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(if (isLandscape) 220.dp else 150.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxTotal = uiState.monthlyTotals.values.maxOrNull() ?: 1.0
            val sortedKeys = uiState.monthlyTotals.keys.toList()
            uiState.monthlyTotals.forEach { (month, total) ->
                val isHighlighted = if (selectedTab == "Last Month") month == sortedKeys.getOrNull(1) else month == sortedKeys.lastOrNull()
                BarChartItem(month, total.toFloat(), maxTotal.toFloat(), if (isHighlighted) Color(0xFFFFB300) else Color(0xFFE0E0E0), isLandscape = isLandscape)
            }
        }
    }
}

@Composable
fun AiInsightCard(selectedTab: String, uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    AnalysisCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(if (isLandscape) 32.dp else 24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "AI Insight", fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 22.sp else 18.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (selectedTab == "Last Month") uiState.lastMonthInsight else uiState.aiInsight,
            fontSize = if (isLandscape) 17.sp else 14.sp,
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HealthScoreCard(selectedTab: String, uiState: com.example.moneytracker.viewmodel.AiReportUiState, viewModel: AiReportViewModel, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val score = if (selectedTab == "Last Month") uiState.lastMonthHealthScore else uiState.healthScore
    val comment = if (selectedTab == "Last Month") uiState.lastMonthHealthComment else uiState.healthComment
    
    AnalysisCard(
        title = "Financial Health Score",
        modifier = modifier,
        trailingContent = {
            if (selectedTab == "This Month") {
                IconButton(onClick = { viewModel.saveCurrentReport() }) {
                    if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Icon(imageVector = if (uiState.saveSuccess) Icons.Default.Check else Icons.Default.Save, contentDescription = "Save", tint = if (uiState.saveSuccess) Color(0xFF2E7D32) else Color.Gray)
                }
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(if (isLandscape) 100.dp else 80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(Color(0xFFE0E0E0), 135f, 270f, false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(if (score > 60) Color(0xFF66BB6A) else Color(0xFFEF5350), 135f, 270f * (score / 100f), false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                }
                Text(text = score.toString(), fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 28.sp else 24.sp)
            }
            Spacer(modifier = Modifier.width(if (isLandscape) 20.dp else 16.dp))
            Column {
                Text(text = if (score >= 70) "Healthy" else "Caution", fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 18.sp else 16.sp)
                Text(text = comment, fontSize = if (isLandscape) 14.sp else 12.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun PredictionCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    AnalysisCard(title = "3. AI Spending Prediction", modifier = modifier) {
        val now = java.time.LocalDate.now()
        val dailyAvg = uiState.totalExpensesForMonth / now.dayOfMonth
        val predictedTotal = if (uiState.predictedSpending > 0) uiState.predictedSpending else (dailyAvg * now.lengthOfMonth())
        val totalBudget = uiState.budgets.sumOf { it.amount }.let { if (it == 0.0) 1000.0 else it }
        Column {
            Text(text = "Estimated end-of-month spending:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "RM ${formatCurrencyDisplay(predictedTotal)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if (predictedTotal > totalBudget) Color(0xFFD32F2F) else Color(0xFF2E7D32))
            Text(text = if (predictedTotal > totalBudget) "⚠️ Projected to exceed budget!" else "✅ On track to stay under budget.", color = if (predictedTotal > totalBudget) Color(0xFFD32F2F) else Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HabitsCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    AnalysisCard(title = "4. Spending Habits", modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Weekend vs Weekday", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Weekend: ${uiState.weekendSpendingPercent}%", fontSize = 14.sp)
            }
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { uiState.weekendSpendingPercent / 100f }, modifier = Modifier.fillMaxSize(), color = if (uiState.weekendSpendingPercent > 50) Color(0xFFFFB300) else Color(0xFF66BB6A), trackColor = Color(0xFFEEEEEE), strokeCap = StrokeCap.Round, strokeWidth = 10.dp)
                Text("${uiState.weekendSpendingPercent}%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, fontSize: androidx.compose.ui.unit.TextUnit = 14.sp, width: androidx.compose.ui.unit.Dp = 140.dp, onClick: () -> Unit) {
    Box(modifier = Modifier.width(width).clip(RoundedCornerShape(width / 2)).background(if (isSelected) Color.White else Color.Transparent).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(text = text, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium, fontSize = fontSize, color = if (isSelected) Color.Black else Color(0xFF444444))
    }
}

@Composable
fun AnalysisCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .padding(if (isLandscape) 20.dp else 20.dp)
                .then(if (modifier == Modifier.fillMaxHeight()) Modifier.fillMaxHeight() else Modifier)
        ) {
            if (title != null) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = if (isLandscape) 20.sp else 18.sp, color = Color(0xFF1A237E))
                        if (subtitle != null) Text(text = subtitle, fontSize = 13.sp, color = Color.DarkGray)
                    }
                    if (trailingContent != null) trailingContent()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            content()
        }
    }
}

@Composable
fun LegendItem(color: Color, category: String, amount: String, fontSize: androidx.compose.ui.unit.TextUnit = 12.sp) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = category, fontSize = fontSize, modifier = Modifier.width(110.dp))
        Text(text = amount, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BarChartItem(label: String, value: Float, maxValue: Float, color: Color, isLandscape: Boolean = false) {
    val heightFactor = value / maxValue
    val chartHeight = if (isLandscape) 150.dp else 100.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "RM ${formatCurrencyDisplay(value.toDouble())}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(40.dp).height(chartHeight * heightFactor).background(color))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp)
    }
}
