package com.oqba26.barghkar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.oqba26.barghkar.ui.screens.*
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                }
            )
        }
        composable(Screen.Calculators.route) {
            CalculatorsScreen(
                onNavigateToOhmLaw = {
                    navController.navigate(Screen.OhmLaw.route)
                },
                onNavigateToVoltageDrop = {
                    navController.navigate(Screen.VoltageDrop.route)
                }
            )
        }
        composable(Screen.References.route) {
            ReferencesScreen(
                onNavigateToColorCodes = {
                    navController.navigate(Screen.ColorCodes.route)
                }
            ) {
                navController.navigate(Screen.Symbols.route)
            }
        }
        composable(Screen.Customers.route) {
            CustomersScreen()
        }
        composable(Screen.Inventory.route) {
            InventoryScreen()
        }
        composable(Screen.Projects.route) {
            ProjectsScreen(
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                }
            )
        }
        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType },
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            MaterialListScreen(projectId = projectId)
        }
        composable(Screen.OhmLaw.route) {
            OhmLawScreen()
        }
        composable(Screen.VoltageDrop.route) {
            VoltageDropScreen()
        }
        composable(Screen.ColorCodes.route) {
            ColorCodeScreen()
        }
        composable(Screen.Symbols.route) {
            SymbolsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
