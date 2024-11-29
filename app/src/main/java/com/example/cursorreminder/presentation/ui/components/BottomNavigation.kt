package com.example.cursorreminder.presentation.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cursorreminder.R

@Composable
fun BottomNav(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        listOf(BottomNavItem.Home, BottomNavItem.AllReminders).forEach { item ->
            NavigationBarItem(
                icon = { Icon(ImageVector.vectorResource(item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: Int,
) {
    data object Home : BottomNavItem(
        route = "home",
        title = "Today",
        icon = R.drawable.icon_today
    )

    data object AllReminders : BottomNavItem(
        route = "all_reminders",
        title = "All",
        icon = R.drawable.icon_list
    )
}