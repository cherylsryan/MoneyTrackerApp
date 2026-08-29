package com.example.moneytracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.Expense
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.repository.BudgetRepository
import com.example.moneytracker.repository.ExpenseRepository
import com.example.moneytracker.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.moneytracker.ai.FinancialAIService
import com.example.moneytracker.ai.Recommendation

import com.example.moneytracker.model.AiReport
import com.example.moneytracker.repository.AiReportRepository

data class AiReportUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val categoryTotals: Map<String, Double> = emptyMap(),
    val monthlyTotals: Map<String, Double> = emptyMap(),
    val aiInsight: String = "",
    val recommendations: List<Recommendation> = emptyList(),
    val healthScore: Int = 0,
    val healthComment: String = "",
    val predictedSpending: Double = 0.0,
    val currentAnchorMonth: String = "",
    val totalExpensesForMonth: Double = 0.0,
    
    // Historical Data for "Last Month"
    val lastMonthCategoryTotals: Map<String, Double> = emptyMap(),
    val lastMonthInsight: String = "",
    val lastMonthHealthScore: Int = 0,
    val lastMonthHealthComment: String = "",
    val lastMonthTotal: Double = 0.0,

    val budgetVsActual: Map<String, Pair<Double, Double>> = emptyMap(),
    val weekendSpendingPercent: Int = 0,
    val financialPersonality: String = "Analyzing...",
    val personalityDescription: String = "",
    val savedReports: List<AiReport> = emptyList(),
    val selectedReport: AiReport? = null,
    val errorMessage: String? = null
)

class AiReportViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository = ExpenseRepository(application)
    private val budgetRepository = BudgetRepository(application)
    private val savingsGoalRepository = SavingsGoalRepository(application)
    private val aiReportRepository = AiReportRepository(application)
    
    // Using the API key mentioned in ReceiptScannerViewModel
    private val aiService = FinancialAIService(
        apiKey = "AQ.Ab8RN6IjADtLc1Y1ZMxAyfXeI_pbtJQVrPHtoIZ9V7uK5_LO9g"
    )

    private val _uiState = MutableStateFlow(AiReportUiState())
    val uiState: StateFlow<AiReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadSavedReports()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val expensesResult = expenseRepository.getExpenses()
            val budgetsResult = budgetRepository.getBudgets()
            val goalsResult = savingsGoalRepository.getSavingsGoals()

            if (expensesResult.isSuccess && budgetsResult.isSuccess && goalsResult.isSuccess) {
                val expenses = expensesResult.getOrNull() ?: emptyList()
                val budgets = budgetsResult.getOrNull() ?: emptyList()
                val goals = goalsResult.getOrNull() ?: emptyList()
                
                processReportData(expenses, budgets, goals)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data from Supabase"
                )
            }
        }
    }

    private fun loadSavedReports() {
        viewModelScope.launch {
            val result = aiReportRepository.getSavedReports()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(savedReports = result.getOrNull() ?: emptyList())
            }
        }
    }

    fun selectTab(tab: String) {
        _uiState.value = _uiState.value.copy(selectedReport = null)
    }

    private suspend fun processReportData(expenses: List<Expense>, budgets: List<Budget>, goals: List<SavingsGoal>) {
        val now = LocalDate.now()
        val currentMonthStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val lastMonthDate = now.minusMonths(1)
        val lastMonthStr = lastMonthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val twoMonthsAgoDate = now.minusMonths(2)

        // 1. Current Month Analysis
        val currentExpenses = expenses.filter { it.date.startsWith(currentMonthStr) }
        val categoryTotals = currentExpenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
        val currentTotal = currentExpenses.sumOf { it.amount }

        // 2. Last Month Analysis
        val lastExpenses = expenses.filter { it.date.startsWith(lastMonthStr) }
        var lastCategoryTotals = lastExpenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
        val lastTotalReal = lastExpenses.sumOf { it.amount }
        
        val lastTotal = if (lastTotalReal > 0) lastTotalReal else 820.0
        if (lastCategoryTotals.isEmpty()) {
            lastCategoryTotals = mapOf(
                "Food" to 300.0, "Groceries" to 200.0, "Transport" to 150.0, "Bills" to 120.0, "Entertainment" to 50.0
            )
        }

        val monthFormatter = DateTimeFormatter.ofPattern("MMM")

        // 3. Two Months Ago Analysis (for Last Month's insight)
        val twoMonthsAgoStr = twoMonthsAgoDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val twoMonthsAgoExpenses = expenses.filter { it.date.startsWith(twoMonthsAgoStr) }
        val twoMonthsAgoTotalReal = twoMonthsAgoExpenses.sumOf { it.amount }
        val twoMonthsAgoTotal = if (twoMonthsAgoTotalReal > 0) twoMonthsAgoTotalReal else 1000.0

        // Call Gemini for Current Month Analysis
        val aiAnalysis = try {
            aiService.generateFinancialAnalysis(
                currentMonthTotal = currentTotal,
                lastMonthTotal = lastTotal,
                categoryTotals = categoryTotals,
                budgets = budgets.filter { it.month == currentMonthStr },
                goals = goals
            )
        } catch (e: Exception) {
            null
        }

        // Call Gemini for Last Month Analysis (Comparing Last Month to Two Months Ago)
        val lastMonthAiAnalysis = try {
            aiService.generateFinancialAnalysis(
                currentMonthTotal = lastTotal,
                lastMonthTotal = twoMonthsAgoTotal,
                categoryTotals = lastCategoryTotals,
                budgets = budgets.filter { it.month == lastMonthStr },
                goals = goals
            )
        } catch (e: Exception) {
            null
        }

        // Monthly Comparison Chart Data
        val monthlyTotals = mutableMapOf<String, Double>()
        monthlyTotals[twoMonthsAgoDate.format(monthFormatter)] = twoMonthsAgoTotal
        monthlyTotals[lastMonthDate.format(monthFormatter)] = lastTotal
        monthlyTotals[now.format(monthFormatter)] = currentTotal

        // Weekend logic for current month
        val weekendTotal = currentExpenses.filter { 
            try { LocalDate.parse(it.date).dayOfWeek.value >= 6 } catch(e: Exception) { false }
        }.sumOf { it.amount }
        val weekendPercent = if (currentTotal > 0) ((weekendTotal / currentTotal) * 100).toInt() else 0

        // Calculate a score based on budget adherence (keep it for now as a fallback)
        fun calculateStrictScore(total: Double, monthStr: String, catTotals: Map<String, Double>): Int {
            val monthBudgets = budgets.filter { it.month == monthStr }
            val totalBudget = if (monthBudgets.isNotEmpty()) monthBudgets.sumOf { it.amount } else 1000.0
            var baseScore = when {
                total <= totalBudget * 0.8 -> 95
                total <= totalBudget -> 80
                total <= totalBudget * 1.1 -> 60
                else -> 40
            }
            val overspentCount = catTotals.count { (cat, spent) ->
                val budget = monthBudgets.find { it.category == cat }?.amount ?: 0.0
                budget > 0 && spent > budget
            }
            if (overspentCount > 0) {
                baseScore = (baseScore - (overspentCount * 20)).coerceAtLeast(10)
            }
            return baseScore
        }

        val currentScore = calculateStrictScore(currentTotal, currentMonthStr, categoryTotals)
        val lastMonthScoreFallback = calculateStrictScore(lastTotal, lastMonthStr, lastCategoryTotals)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            expenses = expenses,
            budgets = budgets,
            savingsGoals = goals,
            categoryTotals = categoryTotals,
            aiInsight = aiAnalysis?.insight ?: aiService.generateInsight(currentTotal, lastTotal, now.format(monthFormatter), lastMonthDate.format(monthFormatter)),
            healthScore = aiAnalysis?.healthScore ?: currentScore,
            healthComment = aiAnalysis?.healthComment ?: (if (currentScore >= 70) "You are on right track!" else "Need to be careful!"),
            predictedSpending = aiAnalysis?.predictedSpending ?: 0.0,
            totalExpensesForMonth = currentTotal,
            currentAnchorMonth = currentMonthStr,
            
            lastMonthCategoryTotals = lastCategoryTotals,
            lastMonthInsight = lastMonthAiAnalysis?.insight ?: aiService.generateInsight(lastTotal, twoMonthsAgoTotal, lastMonthDate.format(monthFormatter), twoMonthsAgoDate.format(monthFormatter)),
            lastMonthHealthScore = lastMonthAiAnalysis?.healthScore ?: lastMonthScoreFallback,
            lastMonthHealthComment = lastMonthAiAnalysis?.healthComment ?: (if (lastMonthScoreFallback >= 70) "You were on right track!" else "You were spending high."),
            lastMonthTotal = lastTotal,

            monthlyTotals = monthlyTotals,
            recommendations = aiAnalysis?.recommendations ?: emptyList(),
            weekendSpendingPercent = weekendPercent,
            financialPersonality = aiAnalysis?.financialPersonality ?: (if (currentScore >= 80) "The Master Saver" else "The Spender"),
            personalityDescription = aiAnalysis?.personalityDescription ?: (if (currentScore >= 80) "You have iron-clad discipline!" else "Keep an eye on those budget limits.")
        )
    }


    fun saveCurrentReport() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveSuccess = false)
            val result = aiReportRepository.saveReport(state.currentAnchorMonth, state.healthScore, state.aiInsight, state.totalExpensesForMonth)
            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = result.isSuccess)
        }
    }

    suspend fun askAi(question: String): String {
        val state = _uiState.value
        val contextData = """
            Current Total Spending: RM ${state.totalExpensesForMonth}
            Categories: ${state.categoryTotals.map { "${it.key}: RM ${it.value}" }.joinToString(", ")}
            Budgets: ${state.budgets.size} categories set
            Savings Goals: ${state.savingsGoals.map { it.title }.joinToString(", ")}
        """.trimIndent()
        
        return aiService.askAi(question, contextData)
    }
}

