package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currencies
import com.example.data.model.Languages
import com.example.ui.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopTopBar(
    viewModel: ShopViewModel,
    title: String,
    subtitle: String? = null,
    showBranchBadge: Boolean = true
) {
    val profile by viewModel.profile.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var showLangMenu by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Shop Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val sub = subtitle ?: profile?.shopName ?: ""
                if (sub.isNotBlank()) {
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            // Currency Chip
            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .testTag("currency_chip")
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showCurrencyMenu = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentCurrency.flag} ${currentCurrency.code}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                DropdownMenu(
                    expanded = showCurrencyMenu,
                    onDismissRequest = { showCurrencyMenu = false }
                ) {
                    Currencies.ALL.take(8).forEach { curr ->
                        DropdownMenuItem(
                            text = {
                                Text("${curr.flag} ${curr.name} (${curr.code})")
                            },
                            onClick = {
                                viewModel.setCurrency(curr.code)
                                showCurrencyMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Language Chip
            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .testTag("language_chip")
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showLangMenu = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val activeLang = Languages.find(currentLang)
                        Text(
                            text = "${activeLang.flag} ${activeLang.nativeName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                DropdownMenu(
                    expanded = showLangMenu,
                    onDismissRequest = { showLangMenu = false }
                ) {
                    Languages.ALL.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text("${lang.flag} ${lang.nativeName} (${lang.englishName})")
                            },
                            onClick = {
                                viewModel.setLanguage(lang.code)
                                showLangMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
        }
    )
}
