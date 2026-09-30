package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Strings
import com.example.ui.ShopViewModel

@Composable
fun MainAppScreen(viewModel: ShopViewModel) {
    val profile by viewModel.profile.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()

    var currentScreen by remember { mutableStateOf("dashboard") }

    // Support dynamic RTL based on selected language (Arabic / Urdu)
    val layoutDirection = Strings.getLayoutDirection(currentLang)

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (profile?.isLoggedIn != true) {
            AuthScreen(viewModel = viewModel)
        } else {
            // Handle device back button
            BackHandler(enabled = currentScreen != "dashboard") {
                currentScreen = "dashboard"
            }

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == "dashboard",
                            onClick = { currentScreen = "dashboard" },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text(viewModel.tr("nav_dashboard"), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_item_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "invoices",
                            onClick = { currentScreen = "invoices" },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Invoices") },
                            label = { Text(viewModel.tr("nav_invoices"), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_item_invoices")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "ledger",
                            onClick = { currentScreen = "ledger" },
                            icon = { Icon(Icons.Default.TableChart, contentDescription = "Ledger") },
                            label = { Text(viewModel.tr("nav_ledger"), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_item_ledger")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "stock",
                            onClick = { currentScreen = "stock" },
                            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Stock") },
                            label = { Text(viewModel.tr("nav_stock"), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_item_stock")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "returns",
                            onClick = { currentScreen = "returns" },
                            icon = { Icon(Icons.Default.AssignmentReturn, contentDescription = "Returns") },
                            label = { Text(viewModel.tr("nav_returns"), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_item_returns")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "settings",
                            onClick = { currentScreen = "settings" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text(viewModel.tr("nav_settings"), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_item_settings")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen(viewModel = viewModel, onNavigate = { currentScreen = it })
                        "invoices" -> InvoiceScreen(viewModel = viewModel)
                        "ledger" -> SalesLedgerScreen(viewModel = viewModel)
                        "stock" -> StockScreen(viewModel = viewModel)
                        "returns" -> ReturnRegisterScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                        else -> DashboardScreen(viewModel = viewModel, onNavigate = { currentScreen = it })
                    }
                }
            }
        }
    }
}
