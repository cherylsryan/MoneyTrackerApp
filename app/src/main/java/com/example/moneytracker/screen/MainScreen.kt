package com.example.moneytracker.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneytracker.repository.UserRepository
import com.example.moneytracker.viewmodel.AiReportViewModel
import com.example.moneytracker.viewmodel.ReceiptScannerViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val requiresAuth: Boolean = false) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.AutoMirrored.Filled.List, requiresAuth = true)
    object Budget : Screen("budget", "Budget", Icons.Default.Wallet, requiresAuth = true)
    object AiReport : Screen("ai_report", "AI", Icons.Default.Assessment, requiresAuth = true)
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    // Initialize repository to trigger the login status check immediately
    val userRepository = remember { UserRepository(context) }
    val isLoggedIn by UserRepository.isLoggedIn.collectAsState()

    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Expenses,
        Screen.Budget,
        Screen.AiReport
    )
    val receiptViewModel: ReceiptScannerViewModel = viewModel()
    val aiReportViewModel: AiReportViewModel = viewModel()
    val backgroundColor = Color(0xFFF6EFC9)
    val navRailBackgroundColor = Color.White

    // Detect if we should use Side Rail (Landscape) or Bottom Bar (Portrait)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(modifier = Modifier.fillMaxSize().background(navRailBackgroundColor)) {
        if (isLandscape) {
            NavigationRail(
                containerColor = navRailBackgroundColor,
                modifier = Modifier.fillMaxHeight().width(90.dp),
                header = {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            ) {
                Spacer(modifier = Modifier.weight(1f))
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationRailItem(
                        icon = { 
                            Icon(
                                imageVector = screen.icon, 
                                contentDescription = null,
                                tint = if (screen.requiresAuth && !isLoggedIn) Color.Gray else if (selected) Color(0xFF1A237E) else Color.Gray
                            ) 
                        },
                        label = { 
                            Text(
                                text = screen.label,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (screen.requiresAuth && !isLoggedIn) Color.Gray else if (selected) Color(0xFF1A237E) else Color.Gray
                            ) 
                        },
                        selected = selected,
                        colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                            indicatorColor = Color(0xFFF3E5F5)
                        ),
                        onClick = {
                            if (screen.requiresAuth && !isLoggedIn) {
                                navController.navigate("profile")
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { 
                    if (isLandscape) it.clip(RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp))
                    else it
                }
                .background(backgroundColor)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (!isLandscape) {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            items.forEach { screen ->
                                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                NavigationBarItem(
                                    icon = { 
                                        Icon(
                                            imageVector = screen.icon, 
                                            contentDescription = null,
                                            tint = if (screen.requiresAuth && !isLoggedIn) Color.LightGray else if (selected) Color(0xFF1A237E) else Color.Gray
                                        ) 
                                    },
                                    label = { 
                                        Text(
                                            text = screen.label,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (screen.requiresAuth && !isLoggedIn) Color.LightGray else if (selected) Color(0xFF1A237E) else Color.Gray
                                        ) 
                                    },
                                    selected = selected,
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0xFFF3E5F5) // Soft Lavender highlight
                                    ),
                                    onClick = {
                                        if (screen.requiresAuth && !isLoggedIn) {
                                            navController.navigate("profile")
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onProfileClick = { navController.navigate("profile") },
                            onAIScannerClick = { navController.navigate("ai_scanner") }
                        )
                    }
                    composable(Screen.Expenses.route) {
                        ExpenseListScreen(
                            onAddExpenseClick = { navController.navigate("add") },
                            onEditExpenseClick = { expenseId -> navController.navigate("edit/$expenseId") }
                        )
                    }
                    composable(Screen.Budget.route) { BudgetManagerScreen() }
                    composable(Screen.AiReport.route) { AiReportScreen(navController, aiReportViewModel) }
                    composable("smart_planner") { SmartPlannerScreen(navController, aiReportViewModel) }

                    // Detail screens
                    composable("profile") {
                        ProfileScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("add") {
                        AddExpenseScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable(
                        route = "edit/{expenseId}",
                        arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
                        EditExpenseScreen(
                            expenseId = expenseId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("ai_scanner") {
                        AIScannerScreen(
                            viewModel = receiptViewModel,
                            onBack = { navController.popBackStack() },
                            onScanned = { navController.navigate("receipt_detail") }
                        )
                    }
                    composable("receipt_detail") {
                        ReceiptDetailScreen(
                            viewModel = receiptViewModel,
                            onBack = {
                                receiptViewModel.reset()
                                navController.popBackStack()
                            },
                            onSaved = {
                                receiptViewModel.reset()
                                navController.popBackStack("home", inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}
