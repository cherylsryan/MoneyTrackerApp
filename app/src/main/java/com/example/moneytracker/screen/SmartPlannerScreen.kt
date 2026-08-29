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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch
import com.example.moneytracker.ai.RecommendationType
import com.example.moneytracker.ai.Recommendation
import com.example.moneytracker.util.formatCurrencyDisplay
import com.example.moneytracker.viewmodel.AiReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlannerScreen(navController: NavController, viewModel: AiReportViewModel = viewModel()) {
    val backgroundColor = Color(0xFFF6EFC9)
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Smart Planner & Recommendations", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF7E57C2))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Gemini is analyzing your finances...", color = Color.Gray)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLandscape) {
                    // Landscape Grid (A-B, C, D-E)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) { SavingGoalCard(uiState) }
                            Box(modifier = Modifier.weight(1f)) { RecommendationsCard(uiState, isLandscape) { route -> navigateToTab(route) } }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth()) { FinancialPlanCard(uiState) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) { AskAiCard(uiState) }
                            Box(modifier = Modifier.weight(1f)) { PersonalityCard(uiState) }
                        }
                    }
                } else {
                    SavingGoalCard(uiState)
                    Spacer(modifier = Modifier.height(16.dp))
                    RecommendationsCard(uiState, isLandscape) { route -> navigateToTab(route) }
                    Spacer(modifier = Modifier.height(16.dp))
                    FinancialPlanCard(uiState)
                    Spacer(modifier = Modifier.height(16.dp))
                    AskAiCard(uiState)
                    Spacer(modifier = Modifier.height(16.dp))
                    PersonalityCard(uiState)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SavingGoalCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    val goal = uiState.savingsGoals.firstOrNull()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    PlannerCard(title = "A. Saving Goal Prediction", modifier = modifier) {
        if (isLandscape) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(goal?.title ?: "Laptop Fund", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Target: RM ${formatCurrencyDisplay(goal?.targetAmount ?: 2000.0)}", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val progress = if ((goal?.targetAmount ?: 2000.0) > 0) (goal?.currentAmount ?: 700.0) / (goal?.targetAmount ?: 2000.0) else 0.0
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(Color(0xFFE0E0E0), 0f, 360f, false, style = Stroke(width = 10.dp.toPx()))
                        drawArc(Color(0xFF66BB6A), -90f, 360f * progress.toFloat(), false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text("${(progress * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(goal?.title ?: "Laptop Fund", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row {
                            Column {
                                Text("Target Amount", fontSize = 10.sp, color = Color.DarkGray)
                                Text("Saved Amount", fontSize = 10.sp, color = Color.DarkGray)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("RM ${formatCurrencyDisplay(goal?.targetAmount ?: 2000.0)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("RM ${formatCurrencyDisplay(goal?.currentAmount ?: 700.0)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(32.dp))
                val progress = if ((goal?.targetAmount ?: 2000.0) > 0) (goal?.currentAmount ?: 700.0) / (goal?.targetAmount ?: 2000.0) else 0.0
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(Color(0xFFE0E0E0), 0f, 360f, false, style = Stroke(width = 8.dp.toPx()))
                        drawArc(Color(0xFF66BB6A), -90f, 360f * progress.toFloat(), false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text("${(progress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E5F5), RoundedCornerShape(12.dp)).padding(12.dp), 
            horizontalArrangement = Arrangement.SpaceAround, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("Month Left", fontSize = 11.sp)
                Text("5 months", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Target Date", fontSize = 11.sp)
                Text("31 Dec 2026", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                val left = (goal?.targetAmount ?: 2000.0) - (goal?.currentAmount ?: 700.0)
                val monthly = left / 5
                Text("Save to reach goal:", fontSize = 11.sp)
                Text("RM ${formatCurrencyDisplay(monthly)} / mo", fontWeight = FontWeight.Bold, color = Color(0xFF7E57C2), fontSize = 14.sp)
                Text("RM ${formatCurrencyDisplay(monthly / 4)} / wk", fontWeight = FontWeight.Bold, color = Color(0xFF7E57C2), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RecommendationsCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, isLandscape: Boolean, modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    PlannerCard(title = "B. AI Recommendations", modifier = modifier) {
        if (uiState.recommendations.isEmpty()) {
            Text("No recommendations at this time.", fontSize = 12.sp, color = Color.DarkGray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recommendations.take(if (isLandscape) 3 else 5).forEach { rec ->
                    var showDetails by rememberSaveable { mutableStateOf(false) }
                    
                    RecommendationItem(
                        icon = when (rec.type) { RecommendationType.WARNING -> Icons.Default.Warning; RecommendationType.INFO -> Icons.Default.Info; RecommendationType.SUCCESS -> Icons.Default.CheckCircle; else -> Icons.Default.Info },
                        title = rec.title, 
                        description = rec.description,
                        showDescription = showDetails,
                        backgroundColor = when (rec.type) { RecommendationType.WARNING -> Color(0xFFFFEBEE); RecommendationType.INFO -> Color(0xFFE3F2FD); RecommendationType.SUCCESS -> Color(0xFFE8F5E9); else -> Color.White },
                        iconColor = when (rec.type) { RecommendationType.WARNING -> Color.Black; RecommendationType.INFO -> Color(0xFF1976D2); RecommendationType.SUCCESS -> Color(0xFF2E7D32); else -> Color.Gray },
                        onToggleDetails = { showDetails = !showDetails },
                        onClick = rec.targetRoute?.let { route -> { onNavigate(route) } }
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialPlanCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    val displayMonth = if (uiState.selectedReport != null) uiState.selectedReport!!.month else uiState.currentAnchorMonth
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    PlannerCard(
        title = "C. ${displayMonth.substring(5).let { 
            when(it) { "01" -> "January"; "02" -> "February"; "03" -> "March"; "04" -> "April"; "05" -> "May"; "06" -> "June"; "07" -> "July"; "08" -> "August"; "09" -> "September"; "10" -> "October"; "11" -> "November"; "12" -> "December"; else -> "This Month" } 
        }}'s Financial Plan",
        modifier = modifier,
        trailingContent = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(if (isLandscape) 28.dp else 20.dp), tint = Color(0xFF1A237E)) }
    ) {
        val categoriesList = listOf("Food", "Shopping", "Transport", "Entertainment", "Bills", "Others")
        val targetMonth = if (uiState.selectedReport != null) uiState.selectedReport!!.month else uiState.currentAnchorMonth
        val activeBudgets = uiState.budgets.filter { it.month == targetMonth }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categoriesList.forEach { categoryName ->
                val budget = activeBudgets.find { it.category == categoryName }
                val weeklyLimit = (budget?.amount ?: 0.0) / 4
                PlanItem(
                    label = "$categoryName Limit", 
                    amount = if (budget != null) "RM ${formatCurrencyDisplay(weeklyLimit)}/week" else "Not Set",
                    isLandscape = isLandscape
                )
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = if (isLandscape) 16.dp else 12.dp), color = Color(0xFFF5F5F5))
        
        Text("Monthly Saving Targets", fontWeight = FontWeight.ExtraBold, fontSize = if (isLandscape) 16.sp else 12.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
        if (uiState.savingsGoals.isEmpty()) {
            Text("No saving goals set.", fontSize = if (isLandscape) 14.sp else 10.sp, color = Color.DarkGray)
        } else {
            uiState.savingsGoals.forEach { goal ->
                val left = goal.targetAmount - goal.currentAmount
                val monthlyNeeded = if (left > 0) left / 5 else 0.0
                PlanItem(
                    label = goal.title, 
                    amount = if (left > 0) "RM ${formatCurrencyDisplay(monthlyNeeded)}/month" else "Done! ✅",
                    isLandscape = isLandscape
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Avoid non-essential stuff, focus on needs only!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 14.sp else 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun AskAiCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, viewModel: AiReportViewModel = viewModel(), modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()
    
    PlannerCard(title = "D. Ask AI Assistant", modifier = modifier) {
        val quickActions = listOf(
            "How to save RM 200?",
            "Analyze my Food habit",
            "Monthly forecast",
            "Emergency fund tips"
        )
        var userQuestion by rememberSaveable { mutableStateOf("") }
        var aiResponse by rememberSaveable { mutableStateOf<String?>(null) }
        var isThinking by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = userQuestion,
            onValueChange = { userQuestion = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ask about your finances...", fontSize = 14.sp) },
            trailingIcon = {
                if (userQuestion.isNotBlank()) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isThinking = true
                                aiResponse = viewModel.askAi(userQuestion)
                                isThinking = false
                                userQuestion = ""
                            }
                        },
                        enabled = !isThinking
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF7E57C2))
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7E57C2),
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true,
            enabled = !isThinking
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickActions.size) { index ->
                val q = quickActions[index]
                SuggestionChip(
                    onClick = {
                        scope.launch {
                            isThinking = true
                            aiResponse = viewModel.askAi(q)
                            isThinking = false
                        }
                    },
                    label = { Text(q, fontSize = if (isLandscape) 13.sp else 11.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White, labelColor = Color.Black),
                    border = BorderStroke(1.dp, Color.Black),
                    enabled = !isThinking
                )
            }
        }
        
        if (isThinking || aiResponse != null) {
            Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 12.dp))
            var isExpanded by rememberSaveable { mutableStateOf(false) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                border = BorderStroke(0.5.dp, Color(0xFF7E57C2))
            ) {
                Row(modifier = Modifier.padding(if (isLandscape) 20.dp else 16.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF7E57C2),
                        modifier = Modifier.size(if (isLandscape) 28.dp else 24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "AI Advice",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLandscape) 16.sp else 13.sp,
                            color = Color(0xFF7E57C2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val text = if (isThinking) "Thinking..." else aiResponse ?: ""
                        Column {
                            Text(
                                text = text,
                                fontSize = if (isLandscape) 16.sp else 15.sp,
                                color = Color.Black,
                                lineHeight = 20.sp,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (!isThinking && aiResponse != null && aiResponse!!.length > 100) {
                                Text(
                                    text = if (isExpanded) "Show less" else "See more...",
                                    color = Color(0xFF7E57C2),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp).clickable { isExpanded = !isExpanded }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalityCard(uiState: com.example.moneytracker.viewmodel.AiReportUiState, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    PlannerCard(title = "E. AI Financial Personality", modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)).padding(if (isLandscape) 20.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(uiState.financialPersonality, fontWeight = FontWeight.ExtraBold, fontSize = if (isLandscape) 20.sp else 16.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(uiState.personalityDescription, fontSize = if (isLandscape) 16.sp else 12.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun PlannerCard(modifier: Modifier = Modifier, title: String, trailingContent: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Card(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
        Column(modifier = Modifier.padding(if (isLandscape) 28.dp else 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = if (isLandscape) 22.sp else 18.sp, color = Color(0xFF1A237E), modifier = Modifier.weight(1f))
                if (trailingContent != null) trailingContent()
            }
            Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 16.dp))
            content()
        }
    }
}

@Composable
fun RecommendationItem(
    icon: ImageVector, 
    title: String, 
    description: String, 
    showDescription: Boolean,
    backgroundColor: Color, 
    iconColor: Color, 
    onToggleDetails: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) { 
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp)) 
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black)
                Text(
                    text = if (showDescription) "Hide details" else "View details", 
                    fontSize = 12.sp, 
                    color = Color.Blue, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onToggleDetails() }
                )
            }
            if (onClick != null) {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.DarkGray)
                }
            }
        }
        if (showDescription) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

@Composable
fun PlanItem(label: String, amount: String, isLandscape: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (isLandscape) 4.dp else 2.dp), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = if (isLandscape) 16.sp else 14.sp)
        Text(text = amount, fontSize = if (isLandscape) 16.sp else 14.sp, fontWeight = FontWeight.Bold)
    }
}
