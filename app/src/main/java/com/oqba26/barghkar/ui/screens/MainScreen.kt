package com.oqba26.barghkar.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oqba26.barghkar.ui.navigation.Screen
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel
import com.oqba26.barghkar.data.sync.RealtimeSyncManager
import com.oqba26.barghkar.data.model.UserRole
import com.oqba26.barghkar.data.model.ApprenticePermission
import io.github.jan.supabase.auth.status.SessionStatus
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import android.app.Activity
import com.oqba26.barghkar.ui.components.CustomDialog

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    
    var showExitDialog by remember { mutableStateOf(false) }

    val realtimeManager = remember { RealtimeSyncManager(context, authViewModel) }

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            realtimeManager.startListening()
        }
    }

    val items = remember(userProfile, userProfile?.permissions) {
        android.util.Log.d("MainScreen", "Refreshing tabs. Role: ${userProfile?.role}, Permissions: ${userProfile?.permissions}")
        val allItems = listOf(
            Screen.Home,
            Screen.Calculators,
            Screen.Customers,
            Screen.Inventory,
            Screen.Projects,
            Screen.Settings,
        )

        when (userProfile?.role) {
            UserRole.MASTER -> allItems
            UserRole.APPRENTICE -> allItems.filter { screen ->
                when (screen) {
                    Screen.Inventory -> userProfile?.permissions?.contains(ApprenticePermission.MANAGE_INVENTORY) == true
                    Screen.Projects -> userProfile?.permissions?.contains(ApprenticePermission.EDIT_PROJECTS) == true
                    else -> true
                }
            }
            else -> listOf(Screen.Home, Screen.Calculators, Screen.Settings)
        }
    }

    // Using RTL for Persian language support
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentRoute = currentDestination?.route
        val showBottomBar = currentRoute != Screen.Login.route && sessionStatus is SessionStatus.Authenticated

        // مدیریت دکمه بازگشت (Back)
        if (showBottomBar) {
            BackHandler {
                if (currentRoute == Screen.Home.route) {
                    showExitDialog = true
                } else {
                    // یک‌راست به صفحه خانه برمی‌گردد
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(stringResource(screen.titleRes)) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        // جلوگیری از پشته شدن صفحات تکراری
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
        
        // Auto-navigate to Home if authenticated and on Login screen
        LaunchedEffect(sessionStatus) {
            if (sessionStatus is SessionStatus.Authenticated && navController.currentDestination?.route == Screen.Login.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else if (sessionStatus is SessionStatus.NotAuthenticated && navController.currentDestination?.route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        if (showExitDialog) {
            CustomDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("خروج از برنامه") },
                text = { Text("آیا مطمئن هستید که می‌خواهید از برنامه خارج شوید؟") },
                confirmButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("انصراف")
                    }
                },
                dismissButton = {
                    Button(onClick = { (context as? Activity)?.finish() }) {
                        Text("تایید")
                    }
                }
            )
        }
    }
}

@Composable
fun NavGraph(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        com.oqba26.barghkar.ui.navigation.NavGraph(
            navController = navController,
            authViewModel = authViewModel
        )
    }
}
