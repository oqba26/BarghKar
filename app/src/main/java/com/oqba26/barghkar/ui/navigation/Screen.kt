package com.oqba26.barghkar.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.oqba26.barghkar.R

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.home, Icons.Default.Home)
    data object Calculators : Screen("calculators", R.string.calculators, Icons.Default.Calculate)
    data object References : Screen("references", R.string.references, Icons.Default.Info)
    data object Customers : Screen("customers", R.string.customers, Icons.Default.People)
    data object Inventory : Screen("inventory", R.string.inventory, Icons.Default.Inventory)
    data object Projects : Screen("projects", R.string.projects, Icons.AutoMirrored.Filled.List)
    data object Settings : Screen("settings", R.string.settings, Icons.Default.Settings)

    // Sub-screens for Calculators
    data object OhmLaw : Screen("ohm_law", R.string.ohm_law, Icons.Default.Calculate)
    data object VoltageDrop : Screen("voltage_drop", R.string.voltage_drop, Icons.Default.Calculate)

    // Sub-screens for References
    data object ColorCodes : Screen("color_codes", R.string.wire_color_codes, Icons.Default.Info)
    data object Symbols : Screen("symbols", R.string.electrical_symbols, Icons.Default.Info)

    // Sub-screens for Projects
    data object ProjectDetails : Screen("project_details/{projectId}", R.string.project_details, Icons.AutoMirrored.Filled.List) {
        fun createRoute(projectId: Long) = "project_details/$projectId"
    }
}
