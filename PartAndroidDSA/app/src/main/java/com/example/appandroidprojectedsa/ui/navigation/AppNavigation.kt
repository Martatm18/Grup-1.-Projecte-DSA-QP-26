package com.example.appandroidprojectedsa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appandroidprojectedsa.ui.screens.LoginScreen
import com.example.appandroidprojectedsa.ui.screens.ShopScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("shop") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("shop") {
            ShopScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("shop") { inclusive = true }
                    }
                }
            )
        }
    }
}
