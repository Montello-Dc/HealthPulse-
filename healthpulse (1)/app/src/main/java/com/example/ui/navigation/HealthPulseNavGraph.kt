package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.viewmodel.HealthViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object PpgScan : Screen("ppg_scan", "Pulse Scan", Icons.Default.Favorite)
    object Lab : Screen("lab", "Lab & Entry", Icons.Default.Science)
    object Trends : Screen("trends", "Trends", Icons.Default.ShowChart)
    object Reminders : Screen("reminders", "Reminders", Icons.Default.Alarm)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    object Chat : Screen("chat", "Assistant", Icons.Default.ChatBubbleOutline)
}

@Composable
fun HealthPulseNavGraph(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.PpgScan,
        Screen.Lab,
        Screen.Trends,
        Screen.Reminders,
        Screen.Chat
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
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
                    viewModel = viewModel,
                    onNavigateToPpg = { navController.navigate(Screen.PpgScan.route) },
                    onNavigateToLab = { navController.navigate(Screen.Lab.route) },
                    onNavigateToHistory = { navController.navigate(Screen.Trends.route) },
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToChat = { navController.navigate(Screen.Chat.route) }
                )
            }

            composable(Screen.PpgScan.route) {
                PpgScanScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHistory = { navController.navigate(Screen.Trends.route) }
                )
            }

            composable(Screen.Lab.route) {
                VitalsEntryAndLabScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(Screen.Trends.route) }
                )
            }

            composable(Screen.Trends.route) {
                HistoryTrendsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Reminders.route) {
                RemindersScreen(viewModel = viewModel)
            }

            composable(Screen.Alerts.route) {
                AlertsCaregiversScreen(viewModel = viewModel)
            }

            composable(Screen.Chat.route) {
                ChatAssistantScreen(viewModel = viewModel)
            }
        }
    }
}
