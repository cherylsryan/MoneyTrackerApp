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

data class HomeUiState(
    val isLoading: Boolean = false,
    val todaySpending: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val currentSavings: Double = 0.0,
    val targetSavings: Double = 0.0,
    val recentExpenses: List<Expense> = emptyList(),
    val dailyTip: String = "",
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository = ExpenseRepository(application)
    private val budgetRepository = BudgetRepository(application)
    private val savingsGoalRepository = SavingsGoalRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
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
                
                processHomeData(expenses, budgets, goals)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data"
                )
            }
        }
    }

    private fun processHomeData(expenses: List<Expense>, budgets: List<Budget>, goals: List<SavingsGoal>) {
        val now = LocalDate.now()
        
        // Find latest data anchor (consistent with AI screen)
        val latestDate = expenses.maxByOrNull { it.date }?.date?.let { 
            try { LocalDate.parse(it) } catch(e: Exception) { null }
        }
        val anchorDate = if (expenses.any { it.date.startsWith(now.format(DateTimeFormatter.ofPattern("yyyy-MM"))) }) {
            now
        } else {
            latestDate ?: now
        }

        val currentMonthStr = anchorDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val todayStr = LocalDate.now().toString() // Use actual today's date

        // 1. Today's Spending
        val todaySpending = expenses.filter { it.date == todayStr }.sumOf { it.amount }

        // 2. Monthly Budget Progress
        val monthlySpent = expenses.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.amount }
        val monthlyBudget = budgets.filter { it.month == currentMonthStr }.sumOf { it.amount }.let { 
            if (it == 0.0) 1000.0 else it
        }

        // 3. Monthly Saving Progress (Using the first goal as primary)
        val goal = goals.firstOrNull()
        val currentSavings = goal?.currentAmount ?: 0.0
        val targetSavings = goal?.targetAmount ?: 1.0 // Avoid div by zero

        // 4. Recent Expenses (Top 3)
        val recentExpenses = expenses.take(3)

        // 5. Daily AI Financial Tip
        val tips = listOf(
            "Track every RM! Small expenses like coffee add up to big savings over time.",
            "Try the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings.",
            "Before buying something non-essential, wait 24 hours. The impulse often fades!",
            "Review your subscriptions. Are you paying for apps or services you don't use?",
            "Meal prepping on Sundays can save you RM 150+ every month on lunch.",
            "Aim to save at least 10% of your income before you start spending.",
            "Emergency funds should ideally cover 3-6 months of your basic living costs.",
            "Check for discounts and cashbacks before you shop to maximize your budget."
        )
        val dailyTip = tips[now.dayOfYear % tips.size]

        _uiState.value = HomeUiState(
            isLoading = false,
            todaySpending = todaySpending,
            monthlySpent = monthlySpent,
            monthlyBudget = monthlyBudget,
            currentSavings = currentSavings,
            targetSavings = targetSavings,
            recentExpenses = recentExpenses,
            dailyTip = dailyTip
        )
    }
}
