package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.ShopViewModel
import com.example.ui.components.ExportShareDialog
import com.example.ui.components.ExportTarget
import com.example.ui.components.ShopTopBar
import com.example.ui.theme.*
import com.example.util.ExcelExportManager
import com.example.util.PdfPrintManager
import com.example.util.SlidesExportManager

@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val monthlyLedger by viewModel.monthlyLedger.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val returnItems by viewModel.returnItems.collectAsState()
    val stockItems by viewModel.stockItems.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var showExportHub by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalSales = monthlyLedger.sumOf { it.sale }
    val totalStorePurchase = monthlyLedger.sumOf { it.storePurchase }
    val totalLocalPurchase = monthlyLedger.sumOf { it.localPurchaseAndExpense }
    val totalReturns = monthlyLedger.sumOf { it.returns }
    val totalExpenses = totalStorePurchase + totalLocalPurchase + totalReturns
    val netMargin = totalSales - totalExpenses

    val todaySales = monthlyLedger.lastOrNull()?.sale ?: 0.0

    Scaffold(
        topBar = {
            ShopTopBar(
                viewModel = viewModel,
                title = profile?.shopName ?: viewModel.tr("app_title"),
                subtitle = profile?.shopNameArabic
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_banner_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopNavy)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile?.shopName ?: "Sholat Al Mujad Al Shamilah Trad.",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                if (!profile?.shopNameArabic.isNullOrBlank()) {
                                    Text(
                                        text = profile?.shopNameArabic ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ShopSlate300)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ShopNavyLight.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = profile?.branch ?: "Main Branch",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = ShopSlate700)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("C.R. No", fontSize = 10.sp, color = ShopSlate300)
                                Text(
                                    profile?.crNumber ?: "1208612",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text("VATIN / Tax No", fontSize = 10.sp, color = ShopSlate300)
                                Text(
                                    profile?.vatin ?: "OM1208612000",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text("Contact / Phone", fontSize = 10.sp, color = ShopSlate300)
                                Text(
                                    profile?.phone ?: "+968 92152565",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Key KPI Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Today's Sales
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = viewModel.tr("dash_today_sales"),
                        value = viewModel.formatMoney(todaySales),
                        icon = Icons.Default.Today,
                        color = ShopNavyLight,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                    // Monthly Sales
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = viewModel.tr("dash_month_sales"),
                        value = viewModel.formatMoney(totalSales),
                        icon = Icons.Default.TrendingUp,
                        color = ShopEmerald,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Net Profit / Margin
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = viewModel.tr("dash_net_margin"),
                        value = viewModel.formatMoney(netMargin),
                        icon = Icons.Default.AccountBalanceWallet,
                        color = if (netMargin >= 0) ShopEmerald else ShopRose,
                        containerColor = if (netMargin >= 0) ShopEmeraldContainer.copy(alpha = 0.3f) else ShopRoseContainer
                    )
                    // Total Returns
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = viewModel.tr("dash_total_returns"),
                        value = viewModel.formatMoney(totalReturns),
                        icon = Icons.Default.AssignmentReturn,
                        color = ShopAmber,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Quick Operations Section
            item {
                Text(
                    text = viewModel.tr("dash_quick_actions"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = viewModel.tr("nav_invoices"),
                            subtitle = "Create & Print Tax Invoices",
                            icon = Icons.Default.ReceiptLong,
                            tint = ShopNavy,
                            onClick = { onNavigate("invoices") }
                        )
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = viewModel.tr("nav_ledger"),
                            subtitle = "Monthly Sales & Purchases",
                            icon = Icons.Default.TableChart,
                            tint = ShopEmerald,
                            onClick = { onNavigate("ledger") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = viewModel.tr("nav_stock"),
                            subtitle = "Vegetables & Groceries",
                            icon = Icons.Default.Inventory2,
                            tint = ShopAmber,
                            onClick = { onNavigate("stock") }
                        )
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = viewModel.tr("nav_returns"),
                            subtitle = "Return Items Register",
                            icon = Icons.Default.AssignmentReturn,
                            tint = ShopRose,
                            onClick = { onNavigate("returns") }
                        )
                    }
                }
            }

            // Executive Reports & Export Hub (Google Slides, Excel, Google Sheets, PDF, Print, Share)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExportHub = true }
                        .testTag("export_hub_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF4B400).copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Slideshow, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Executive Reports & Presentation Hub",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Google Slides, Excel, Sheets, PDF, Print & Share",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ShopSlate300)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Format Action Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Slides
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val p = profile ?: com.example.data.model.ShopProfile()
                                        val file = SlidesExportManager.createPresentationSlidesPdf(context, p, selectedMonth, monthlyLedger, stockItems, returnItems, currentCurrency)
                                        SlidesExportManager.openInGoogleSlides(context, file)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("📽️ Slides", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                            }
                            // Sheets
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val p = profile ?: com.example.data.model.ShopProfile()
                                        val file = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        ExcelExportManager.openInGoogleSheets(context, file)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("📗 Sheets", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                            }
                            // Excel
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val p = profile ?: com.example.data.model.ShopProfile()
                                        val file = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        ExcelExportManager.openInExcel(context, file)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("📊 Excel", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1), textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                            }
                            // PDF
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFE4E6),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val p = profile ?: com.example.data.model.ShopProfile()
                                        val file = PdfPrintManager.createLedgerPdf(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        PdfPrintManager.openPdf(context, file)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("📄 PDF", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ShopRose, textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }
            }

            // Recent Invoices Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.tr("dash_recent_invoices"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { onNavigate("invoices") }) {
                        Text(viewModel.tr("dash_view_all"))
                    }
                }
            }

            // Invoices items
            if (invoices.isEmpty()) {
                item {
                    Text(
                        text = "No invoices generated yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(invoices.take(3)) { invoice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("invoices") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = invoice.customerName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${invoice.invoiceNo} • ${invoice.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = viewModel.formatMoney(invoice.totalAmount),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (invoice.status == "PAID") ShopEmeraldContainer else ShopAmberContainer
                                ) {
                                    Text(
                                        text = invoice.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (invoice.status == "PAID") ShopEmerald else ShopAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showExportHub) {
        ExportShareDialog(
            viewModel = viewModel,
            target = ExportTarget.ALL_IN_ONE,
            onDismiss = { showExportHub = false }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
