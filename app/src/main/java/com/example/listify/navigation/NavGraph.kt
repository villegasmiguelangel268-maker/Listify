package com.example.listify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listify.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home" // set "splash" if you have a splash screen
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("add") {
            val existingItem =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<GroceryItem>("editItem")

            AddEditItemScreen(navController, existingItem)
        }
    }
}
