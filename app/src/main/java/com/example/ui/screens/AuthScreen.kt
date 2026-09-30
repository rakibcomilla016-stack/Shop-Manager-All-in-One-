package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currencies
import com.example.data.model.Languages
import com.example.ui.ShopViewModel
import com.example.ui.theme.ShopEmerald
import com.example.ui.theme.ShopNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: ShopViewModel) {
    val currentLang by viewModel.currentLang.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var selectedTab by remember { mutableStateOf("GMAIL") } // GMAIL, PHONE, WHATSAPP

    var gmailInput by remember { mutableStateOf("rakib.comilla016@gmail.com") }
    var phoneCountryCode by remember { mutableStateOf("+968") }
    var phoneInput by remember { mutableStateOf("92152565") }
    var whatsappCountryCode by remember { mutableStateOf("+968") }
    var whatsappInput by remember { mutableStateOf("92152565") }

    // Customizable Shop Profile Details (Single-Part configuration)
    var shopName by remember { mutableStateOf("Sholat Al Mujad Al Shamilah Trad.") }
    var shopNameArabic by remember { mutableStateOf("شركة شعلة المجد الشاملة للتجارة") }
    var crNumber by remember { mutableStateOf("1208612") }
    var vatinNumber by remember { mutableStateOf("OM1208612000") }
    var branchName by remember { mutableStateOf("Main Branch") }

    var selectedCurrencyCode by remember { mutableStateOf("OMR") }
    var selectedLanguageCode by remember { mutableStateOf(currentLang) }

    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("123456") }
    var showOtpSuccessMessage by remember { mutableStateOf(false) }

    var expandedCurrencyDropdown by remember { mutableStateOf(false) }
    var expandedLanguageDropdown by remember { mutableStateOf(false) }
    var expandedCountryCodeDropdown by remember { mutableStateOf(false) }

    val countryCodes = listOf(
        "+968" to "🇴🇲 Oman",
        "+880" to "🇧🇩 Bangladesh",
        "+966" to "🇸🇦 Saudi Arabia",
        "+971" to "🇦🇪 UAE",
        "+91" to "🇮🇳 India",
        "+92" to "🇵🇰 Pakistan",
        "+965" to "🇰🇼 Kuwait",
        "+973" to "🇧🇭 Bahrain",
        "+974" to "🇶🇦 Qatar",
        "+1" to "🇺🇸 USA/Canada",
        "+44" to "🇬🇧 UK"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App Emblem
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Shop Emblem",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = viewModel.tr("auth_title"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = viewModel.tr("auth_subtitle"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Single-Part Sign-Up Tabs (Gmail, Phone Number, WhatsApp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Choose Sign-Up / Login Method",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Gmail Tab
                    FilterChip(
                        selected = selectedTab == "GMAIL",
                        onClick = { selectedTab = "GMAIL" },
                        label = { Text(viewModel.tr("tab_gmail")) },
                        leadingIcon = {
                            Icon(Icons.Default.Mail, contentDescription = "Gmail", modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("tab_gmail")
                    )

                    // Phone Tab
                    FilterChip(
                        selected = selectedTab == "PHONE",
                        onClick = { selectedTab = "PHONE" },
                        label = { Text(viewModel.tr("tab_phone")) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("tab_phone")
                    )

                    // WhatsApp Tab
                    FilterChip(
                        selected = selectedTab == "WHATSAPP",
                        onClick = { selectedTab = "WHATSAPP" },
                        label = { Text(viewModel.tr("tab_whatsapp")) },
                        leadingIcon = {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("tab_whatsapp")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    "GMAIL" -> {
                        OutlinedTextField(
                            value = gmailInput,
                            onValueChange = { gmailInput = it },
                            label = { Text(viewModel.tr("enter_gmail")) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gmail_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ One-click sign-in with your business Google account",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShopEmerald
                        )
                    }

                    "PHONE" -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                OutlinedButton(
                                    onClick = { expandedCountryCodeDropdown = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text(phoneCountryCode, fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(
                                    expanded = expandedCountryCodeDropdown,
                                    onDismissRequest = { expandedCountryCodeDropdown = false }
                                ) {
                                    countryCodes.forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = { Text("$label ($code)") },
                                            onClick = {
                                                phoneCountryCode = code
                                                expandedCountryCodeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text(viewModel.tr("enter_phone")) },
                                leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = "Phone") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("phone_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }

                        if (isVerifyingOtp) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("SMS Verification Code (OTP)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    "WHATSAPP" -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                OutlinedButton(
                                    onClick = { expandedCountryCodeDropdown = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text(whatsappCountryCode, fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(
                                    expanded = expandedCountryCodeDropdown,
                                    onDismissRequest = { expandedCountryCodeDropdown = false }
                                ) {
                                    countryCodes.forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = { Text("$label ($code)") },
                                            onClick = {
                                                whatsappCountryCode = code
                                                expandedCountryCodeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = whatsappInput,
                                onValueChange = { whatsappInput = it },
                                label = { Text(viewModel.tr("enter_whatsapp")) },
                                leadingIcon = { Icon(Icons.Default.Chat, contentDescription = "WhatsApp") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("whatsapp_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Direct WhatsApp receipt delivery and customer alerts enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShopEmerald
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Customizable Shop Details (Integrated Single-Part Setup)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. " + viewModel.tr("shop_details_section"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text(viewModel.tr("shop_name")) },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Shop") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = shopNameArabic,
                    onValueChange = { shopNameArabic = it },
                    label = { Text(viewModel.tr("shop_name_arabic")) },
                    leadingIcon = { Icon(Icons.Default.Translate, contentDescription = "Arabic Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = crNumber,
                        onValueChange = { crNumber = it },
                        label = { Text(viewModel.tr("cr_number")) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cr_number_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = vatinNumber,
                        onValueChange = { vatinNumber = it },
                        label = { Text(viewModel.tr("vatin_number")) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vatin_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text(viewModel.tr("branch_name")) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Branch") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Currency & Language Selectors
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Currency Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        val activeCurr = Currencies.find(selectedCurrencyCode)
                        OutlinedButton(
                            onClick = { expandedCurrencyDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("currency_select_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${activeCurr.flag} ${activeCurr.code}", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedCurrencyDropdown,
                            onDismissRequest = { expandedCurrencyDropdown = false }
                        ) {
                            Currencies.ALL.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text("${curr.flag} ${curr.name} (${curr.code})") },
                                    onClick = {
                                        selectedCurrencyCode = curr.code
                                        expandedCurrencyDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Language Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        val activeLang = Languages.find(selectedLanguageCode)
                        OutlinedButton(
                            onClick = { expandedLanguageDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("language_select_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${activeLang.flag} ${activeLang.nativeName}", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedLanguageDropdown,
                            onDismissRequest = { expandedLanguageDropdown = false }
                        ) {
                            Languages.ALL.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.flag} ${lang.nativeName} (${lang.englishName})") },
                                    onClick = {
                                        selectedLanguageCode = lang.code
                                        viewModel.setLanguage(lang.code)
                                        expandedLanguageDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Action Button
        val identifier = when (selectedTab) {
            "GMAIL" -> gmailInput
            "PHONE" -> "$phoneCountryCode $phoneInput"
            else -> "$whatsappCountryCode $whatsappInput"
        }

        Button(
            onClick = {
                viewModel.loginOrSignUp(
                    type = selectedTab,
                    identifier = identifier,
                    shopName = shopName,
                    shopNameArabic = shopNameArabic,
                    crNumber = crNumber,
                    vatin = vatinNumber,
                    branch = branchName,
                    phone = "$phoneCountryCode $phoneInput",
                    currencyCode = selectedCurrencyCode,
                    languageCode = selectedLanguageCode
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_auth_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            val btnLabel = when (selectedTab) {
                "GMAIL" -> viewModel.tr("btn_continue_gmail")
                "PHONE" -> viewModel.tr("btn_continue_phone")
                else -> viewModel.tr("btn_continue_whatsapp")
            }
            Text(btnLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Demo login fallback
        TextButton(
            onClick = {
                viewModel.loginOrSignUp(
                    type = "GMAIL",
                    identifier = "rakib.comilla016@gmail.com",
                    shopName = "Sholat Al Mujad Al Shamilah Trad.",
                    shopNameArabic = "شركة شعلة المجد الشاملة للتجارة",
                    crNumber = "1208612",
                    vatin = "OM1208612000",
                    branch = "Main Branch",
                    phone = "+968 92152565",
                    currencyCode = "OMR",
                    languageCode = "en"
                )
            },
            modifier = Modifier.testTag("skip_demo_button")
        ) {
            Text(viewModel.tr("skip_for_now"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
