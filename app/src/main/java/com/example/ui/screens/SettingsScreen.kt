package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currencies
import com.example.data.model.CurrencyInfo
import com.example.data.model.Languages
import com.example.data.model.ShopProfile
import com.example.ui.ShopViewModel
import com.example.ui.components.ShopTopBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var shopName by remember(profile) { mutableStateOf(profile?.shopName ?: "") }
    var shopNameArabic by remember(profile) { mutableStateOf(profile?.shopNameArabic ?: "") }
    var crNumber by remember(profile) { mutableStateOf(profile?.crNumber ?: "") }
    var vatin by remember(profile) { mutableStateOf(profile?.vatin ?: "") }
    var branch by remember(profile) { mutableStateOf(profile?.branch ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var whatsapp by remember(profile) { mutableStateOf(profile?.whatsapp ?: "") }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ShopTopBar(
                viewModel = viewModel,
                title = viewModel.tr("settings_title")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Signed In Account Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (profile?.loginType) {
                                    "WHATSAPP" -> Icons.Default.Chat
                                    "PHONE" -> Icons.Default.Phone
                                    else -> Icons.Default.AccountCircle
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.loginIdentifier ?: "rakib.comilla016@gmail.com",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Signed in via ${profile?.loginType ?: "GMAIL"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShopEmerald
                        )
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out", tint = ShopRose)
                    }
                }
            }

            // Shop Profile Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.tr("settings_shop_profile"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop / Company Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = shopNameArabic,
                        onValueChange = { shopNameArabic = it },
                        label = { Text("Arabic / Regional Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = crNumber,
                            onValueChange = { crNumber = it },
                            label = { Text("C.R. Number") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = vatin,
                            onValueChange = { vatin = it },
                            label = { Text("VATIN Tax No") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("Branch / Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cur = profile ?: return@Button
                            viewModel.loginOrSignUp(
                                type = cur.loginType,
                                identifier = cur.loginIdentifier,
                                shopName = shopName,
                                shopNameArabic = shopNameArabic,
                                crNumber = crNumber,
                                vatin = vatin,
                                branch = branch,
                                phone = phone,
                                currencyCode = currentCurrency.code,
                                languageCode = currentLang
                            )
                            Toast.makeText(context, "Shop details updated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(viewModel.tr("btn_save_changes"), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Currency & Language Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Global Currencies & Languages",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Currency Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Active Currency", fontWeight = FontWeight.SemiBold)
                            Text(
                                "${currentCurrency.flag} ${currentCurrency.name} (${currentCurrency.code}) - ${currentCurrency.decimals} decimals",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showCurrencyDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val lang = Languages.find(currentLang)
                        Column {
                            Text("Display Language", fontWeight = FontWeight.SemiBold)
                            Text(
                                "${lang.flag} ${lang.nativeName} (${lang.englishName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showLanguageDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change")
                        }
                    }
                }
            }

            // Switch User / Log Out Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ShopRoseContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Session Control", fontWeight = FontWeight.Bold, color = ShopRose)
                        Text("Sign in with another Gmail, Phone or WhatsApp", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopRose),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(viewModel.tr("btn_switch_account"), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency (All Supported)", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Currencies.ALL.forEach { curr ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentCurrency.code == curr.code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = {
                                viewModel.setCurrency(curr.code)
                                showCurrencyDialog = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${curr.flag} ${curr.name} (${curr.code})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${curr.decimals} decimals", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Application Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Languages.ALL.forEach { lang ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentLang == lang.code) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = {
                                viewModel.setLanguage(lang.code)
                                showLanguageDialog = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${lang.flag} ${lang.nativeName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(lang.englishName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
            }
        )
    }
}
