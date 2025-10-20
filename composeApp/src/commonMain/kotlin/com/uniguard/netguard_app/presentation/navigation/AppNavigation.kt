package com.uniguard.netguard_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uniguard.netguard_app.di.AppModule
import com.uniguard.netguard_app.presentation.ui.screens.DashboardScreen
import com.uniguard.netguard_app.presentation.ui.screens.HistoryScreen
import com.uniguard.netguard_app.presentation.ui.screens.LoginScreen
import com.uniguard.netguard_app.presentation.ui.screens.ProfileScreen
import com.uniguard.netguard_app.presentation.ui.screens.RegisterScreen
import com.uniguard.netguard_app.presentation.ui.screens.ServerManagementScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    isLoggedIn: Boolean = AppModule.authViewModel.isLoggedIn.collectAsState().value
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Dashboard else Login
    ) {
        // Authentication Routes
        composable<Login> {
            LoginScreen(
                viewModel = AppModule.authViewModel,
                onLoginSuccess = {
                    navController.navigate(Dashboard) {
                        popUpTo<Login> { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Register)
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                viewModel = AppModule.authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Dashboard) {
                        popUpTo<Register> { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Routes
        composable<Dashboard> {
            DashboardScreen(
                onNavigateToServerList = {
                    navController.navigate(ServerList)
                },
                onNavigateToHistory = {
                    navController.navigate(History)
                },
                onNavigateToProfile = {
                    navController.navigate(Profile)
                },
                onLogout = {
                    AppModule.authViewModel.logout()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<ServerList> {
            ServerManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<History> {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}