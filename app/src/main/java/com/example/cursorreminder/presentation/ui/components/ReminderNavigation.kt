package com.example.cursorreminder.presentation.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cursorreminder.presentation.ui.screens.AllRemindersScreen
import com.example.cursorreminder.presentation.ui.screens.HomeScreen
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel

@Composable
fun ReminderNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ReminderViewModel
) {
    Scaffold(
        bottomBar = { BottomNav(navController = navController) },
        contentWindowInsets = WindowInsets(top = 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onRequestPermission = { /* handle permission */ }
                )
            }
            composable(BottomNavItem.AllReminders.route) {
                AllRemindersScreen(
                    viewModel = viewModel,
                    onRequestPermission = { /* handle permission */ }
                )
            }
        }
    }
}