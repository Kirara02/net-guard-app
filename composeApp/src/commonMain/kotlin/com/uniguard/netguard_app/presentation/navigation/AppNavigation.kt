package com.uniguard.netguard_app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.presentation.ui.screens.AboutScreen
import com.uniguard.netguard_app.presentation.ui.screens.ChangePasswordScreen
import com.uniguard.netguard_app.presentation.ui.screens.DashboardScreen
import com.uniguard.netguard_app.presentation.ui.screens.HistoryScreen
import com.uniguard.netguard_app.presentation.ui.screens.LoginScreen
import com.uniguard.netguard_app.presentation.ui.screens.ReportScreen
import com.uniguard.netguard_app.presentation.ui.screens.SettingsScreen
import com.uniguard.netguard_app.presentation.ui.screens.RegisterScreen
import com.uniguard.netguard_app.presentation.ui.screens.ServerManagementScreen
import com.uniguard.netguard_app.presentation.ui.screens.SplashScreen
import com.uniguard.netguard_app.presentation.viewmodel.AuthViewModel
import com.uniguard.netguard_app.data.remote.api.AuthInterceptor
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
) {
    val authViewModel = rememberKoinViewModel<AuthViewModel>()
    val authInterceptor = koinInject<AuthInterceptor>()

    // Listen for unauthorized events and navigate to login
    LaunchedEffect(Unit) {
        authInterceptor.unauthorizedEvent.collect {
            // Clear current user state
            authViewModel.logout()
            // Navigate to login screen
            navController.navigate(Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        // Splash Screen
        composable<Splash> {
            SplashScreen(navController = navController)
        }

        // Authentication Routes
        composable<Login> {
            LoginScreen(
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
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onNavigateToReport = {
                    navController.navigate(Report)
                },
                onLogout = {
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

        composable<Settings> {
            SettingsScreen(
                onNavigateToAbout = { navController.navigate(About) },
                onNavigateToChangePassword = { navController.navigate(ChangePassword) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Report> {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<About> {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ChangePassword> {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}