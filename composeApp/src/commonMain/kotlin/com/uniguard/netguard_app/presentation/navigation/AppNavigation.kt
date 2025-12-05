package com.uniguard.netguard_app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.presentation.ui.screens.shared.about.AboutScreen
import com.uniguard.netguard_app.presentation.ui.screens.user.change_password.ChangePasswordScreen
import com.uniguard.netguard_app.presentation.ui.screens.user.dashboard.DashboardScreen
import com.uniguard.netguard_app.presentation.ui.screens.user.history.HistoryScreen
import com.uniguard.netguard_app.presentation.ui.screens.user.login.LoginScreen
import com.uniguard.netguard_app.presentation.ui.screens.user.report.ReportScreen
import com.uniguard.netguard_app.presentation.ui.screens.shared.settings.SettingsScreen
import com.uniguard.netguard_app.presentation.ui.screens.user.servers.ServersScreen
import com.uniguard.netguard_app.presentation.ui.screens.shared.splash.SplashScreen
import com.uniguard.netguard_app.presentation.viewmodel.user.AuthViewModel
import com.uniguard.netguard_app.data.remote.api.AuthInterceptor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.UserRole
import com.uniguard.netguard_app.presentation.ui.components.showErrorToast
import com.uniguard.netguard_app.presentation.ui.components.showToast
import com.uniguard.netguard_app.presentation.ui.screens.shared.permissions.PermissionsScreen
import com.uniguard.netguard_app.presentation.ui.screens.admin.users.UsersScreen
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.groups.GroupsScreen
import com.uniguard.netguard_app.presentation.ui.screens.super_admin.dashboard.SADashboardScreen
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = rememberKoinViewModel<AuthViewModel>()
) {
    val authInterceptor: AuthInterceptor = koinInject()

    val logoutState by authViewModel.logoutState.collectAsState()
    val hasHandledUnauthorized = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authInterceptor.unauthorizedEvent.collect {
            if (hasHandledUnauthorized.value) return@collect
            hasHandledUnauthorized.value = true

            authViewModel.forceLocalLogout()

            showErrorToast("Session expired. Please login again.")

            delay(300)

            navController.navigate(Login) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }


    LaunchedEffect(logoutState) {
        when (val state = logoutState) {
            is ApiResult.Success -> {
                showToast(state.data)
                delay(300)
                authViewModel.resetLogoutState()
            }
            is ApiResult.Error -> {
                showErrorToast(state.message)
                authViewModel.resetLogoutState()
            }
            else -> {}
        }
    }


    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        // Splash Screen
        composable<Splash> {
            SplashScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        // Authentication Routes
        composable<Login> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Dashboard) {
                        popUpTo<Login> { inclusive = true }
                    }
                },
            )
        }

        // Main App Routes
        composable<Dashboard> {
            val user by authViewModel.currentUser.collectAsState()
            val isUserChecked by authViewModel.isUserChecked.collectAsState()

            when {
                !isUserChecked -> {
                    LoadingOverlay()
                    return@composable
                }

                user == null -> {
                    LaunchedEffect("redirectLogout") {
                        navController.navigate(Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    return@composable
                }

                else -> {
                    if (user!!.userRole == UserRole.SUPER_ADMIN) {
                        SADashboardScreen(
                            authViewModel = authViewModel,
                            onNavigateToGroupList = { navController.navigate(Groups) },
                            onNavigateToUserList = { navController.navigate(Users) },
                            onNavigateToServerList = { navController.navigate(ServerList) },
                            onNavigateToSettings = { navController.navigate(Settings) },
                        )
                    } else {
                        DashboardScreen(
                            authViewModel = authViewModel,
                            onNavigateToServerList = { navController.navigate(ServerList) },
                            onNavigateToHistory = { navController.navigate(History) },
                            onNavigateToSettings = { navController.navigate(Settings) },
                            onNavigateToReport = { navController.navigate(Report) },
                            onNavigateToUsers = { navController.navigate(Users) },
                        )
                    }
                }
            }

        }

        composable<Groups> {
            GroupsScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Users> {
            UsersScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ServerList> {
            ServersScreen(
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
                viewModel = authViewModel,
                onNavigateToAbout = { navController.navigate(About) },
                onNavigateToChangePassword = { navController.navigate(ChangePassword) },
                onNavigateToPermissions = { navController.navigate(Permissions) },
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

        composable<Permissions> {
            PermissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ChangePassword> {
            ChangePasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}