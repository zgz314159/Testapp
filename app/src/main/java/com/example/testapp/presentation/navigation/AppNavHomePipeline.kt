package com.example.testapp.presentation.navigation

import androidx.navigation.NavHostController

private const val HOME_ROUTE = "home"

fun NavHostController.navigateFromHome(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.popBackToHome() {
    popBackStack(HOME_ROUTE, inclusive = false)
}
