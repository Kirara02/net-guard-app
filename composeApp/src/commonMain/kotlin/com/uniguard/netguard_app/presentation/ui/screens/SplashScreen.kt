package com.uniguard.netguard_app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uniguard.netguard_app.core.rememberKoinViewModel
import com.uniguard.netguard_app.presentation.navigation.Dashboard
import com.uniguard.netguard_app.presentation.navigation.Login
import com.uniguard.netguard_app.presentation.viewmodel.SplashState
import com.uniguard.netguard_app.presentation.viewmodel.SplashViewModel
import netguardapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashScreen(
    navController: NavController,
    onNavigateToLogin: () -> Unit = {
        navController.navigate(Login) {
            popUpTo(0) { inclusive = true }
        }
    },
    onNavigateToDashboard: () -> Unit = {
        navController.navigate(Dashboard) {
            popUpTo(0) { inclusive = true }
        }
    }
) {
    val splashViewModel = rememberKoinViewModel<SplashViewModel>()
    val splashState by splashViewModel.splashState.collectAsState()

    // Handle navigation based on authentication state
    LaunchedEffect(splashState) {
        when (splashState) {
            is SplashState.NavigateToLogin -> {
                onNavigateToLogin()
            }
            is SplashState.NavigateToDashboard -> {
                onNavigateToDashboard()
            }
            SplashState.Loading -> {
                // Stay on splash screen
            }
        }
    }

    // Splash screen UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Icon placeholder
        Text(
            text = "🛡️",
            fontSize = 80.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App Name
        Text(
            text = stringResource(Res.string.splash_app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tagline
        Text(
            text = stringResource(Res.string.splash_tagline),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Loading indicator
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color.White,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Loading text
        Text(
            text = stringResource(Res.string.splash_loading),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}