package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.DailyLedgerEntry
import com.example.data.model.ShopProfile
import com.example.ui.ShopViewModel
import com.example.ui.components.ExportShareDialog
import com.example.ui.components.ExportTarget
import com.example.ui.components.ShopTopBar
import com.example.ui.theme.*
import com.example.util.ExcelExportManager
import com.example.util.PdfPrintManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesLedgerScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val monthlyLedger by viewModel.monthlyLedger.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<DailyLedgerEntry?>(null) }

    val totalSales = monthlyLedger.sumOf { it.sale }
    val totalStorePurchase = monthlyLedger.sumOf { it.storePurchase }
    val totalLocalPurchase = monthlyLedger.sumOf { it.localPurchaseAndExpense }
    val totalReturns = monthlyLedger.sumOf { it.returns }
    val totalExpenses = totalStorePurchase + totalLocalPurchase + totalReturns
    val totalNetMargin = totalSales - totalExpenses

    Scaffold(
        topBar = {
            ShopTopBar(
                viewModel = viewModel,
                title = viewModel.tr("ledger_title")
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingEntry = null
                    showAddDialog = true
                },
                containerColor = ShopEmerald,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(viewModel.tr("ledger_add_entry")) },
                modifier = Modifier.testTag("add_ledger_entry_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Month Selector Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Month: $selectedMonth",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Row {
                        listOf("2026-08", "2026-09", "2026-10").forEach { month ->
                            FilterChip(
                                selected = selectedMonth == month,
                                onClick = { viewModel.setSelectedMonth(month) },
                                label = { Text(month.substring(5)) },
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                // PDF, Excel, Print, Share Action Buttons for Ledger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Excel Export Button
                    Button(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val excelFile = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                            ExcelExportManager.openExcelFile(context, excelFile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)), // Official Excel Green
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val pdf = PdfPrintManager.createLedgerPdf(context, p, selectedMonth, monthlyLedger, currentCurrency)
                            PdfPrintManager.openPdf(context, pdf)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ShopRose, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("PDF", fontSize = 11.sp, color = ShopRose, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val pdf = PdfPrintManager.createLedgerPdf(context, p, selectedMonth, monthlyLedger, currentCurrency)
                            PdfPrintManager.printPdf(context, pdf, "Ledger_$selectedMonth")
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Print", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val excelFile = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                            ExcelExportManager.shareExcelFile(context, excelFile, "Monthly Sales & Expense Ledger $selectedMonth (Excel)")
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = ShopEmerald, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Share", fontSize = 11.sp, color = ShopEmerald, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "More Export Options (Google Slides, Sheets, WhatsApp)",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Top 5 Summary Cards Grid (Matching Screenshot 3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Monthly Sales and Expense Ledger - $selectedMonth",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryPill(
                            title = "Total Sales",
                            value = viewModel.formatMoney(totalSales),
                            modifier = Modifier.weight(1f),
                            bg = ShopSlate100,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                        SummaryPill(
                            title = "Store Purchase",
                            value = viewModel.formatMoney(totalStorePurchase),
                            modifier = Modifier.weight(1f),
                            bg = ShopSlate100,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryPill(
                            title = "Local Purchase",
                            value = viewModel.formatMoney(totalLocalPurchase),
                            modifier = Modifier.weight(1f),
                            bg = ShopSlate100,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                        SummaryPill(
                            title = "Total Returns",
                            value = viewModel.formatMoney(totalReturns),
                            modifier = Modifier.weight(1f),
                            bg = ShopSlate100,
                            textColor = ShopRose
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Big Net Profit Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (totalNetMargin >= 0) ShopEmeraldContainer else ShopRoseContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Net Profit (Cash Basis):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (totalNetMargin >= 0) ShopEmerald else ShopRose
                            )
                            Text(
                                viewModel.formatMoney(totalNetMargin),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (totalNetMargin >= 0) ShopEmerald else ShopRose
                            )
                        }
                    }
                }
            }

            // Ledger Table Container with horizontal scrolling for complete tabular view
            val hScroll = rememberScrollState()

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(hScroll)
                            .background(Color(0xFF134E4A)) // Deep Teal matching sheet
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text("Date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                        Text("Sale", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(105.dp), textAlign = TextAlign.End)
                        Text("Store Purchase", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(115.dp), textAlign = TextAlign.End)
                        Text("Local Purchase & Exp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(135.dp), textAlign = TextAlign.End)
                        Text("Returns", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                        Text("Total Daily Exp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(110.dp), textAlign = TextAlign.End)
                        Text("Daily Net Margin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                        Text("Actions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                    }

                    // Table rows
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(hScroll)
                    ) {
                        items(monthlyLedger) { entry ->
                            val isNegative = entry.dailyNetMargin < 0
                            val rowBg = if (monthlyLedger.indexOf(entry) % 2 == 0) Color.Transparent else ShopSlate50

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.date, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
                                Text(viewModel.formatMoney(entry.sale), fontSize = 12.sp, modifier = Modifier.width(105.dp), textAlign = TextAlign.End)
                                Text(
                                    if (entry.storePurchase > 0) viewModel.formatMoney(entry.storePurchase) else "-",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(115.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    if (entry.localPurchaseAndExpense > 0) viewModel.formatMoney(entry.localPurchaseAndExpense) else "-",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(135.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    if (entry.returns > 0) viewModel.formatMoney(entry.returns) else "-",
                                    fontSize = 12.sp,
                                    color = if (entry.returns > 0) ShopRose else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(90.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    viewModel.formatMoney(entry.totalDailyExpenses),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(110.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    viewModel.formatMoney(entry.dailyNetMargin),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNegative) ShopRose else ShopEmerald,
                                    modifier = Modifier.width(120.dp),
                                    textAlign = TextAlign.End
                                )
                                Row(
                                    modifier = Modifier.width(70.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingEntry = entry
                                            showAddDialog = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = ShopNavyLight)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteDailyLedger(entry.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = ShopRose)
                                    }
                                }
                            }
                            Divider(color = ShopSlate200)
                        }

                        // Bottom Total Row (Matching Screenshot 3 TOTAL row)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFCCFBF1)) // Light cyan/teal
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp), color = Color(0xFF0F766E))
                                Text(viewModel.formatMoney(totalSales), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(105.dp), textAlign = TextAlign.End, color = Color(0xFF0F766E))
                                Text(viewModel.formatMoney(totalStorePurchase), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(115.dp), textAlign = TextAlign.End, color = Color(0xFF0F766E))
                                Text(viewModel.formatMoney(totalLocalPurchase), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(135.dp), textAlign = TextAlign.End, color = Color(0xFF0F766E))
                                Text(viewModel.formatMoney(totalReturns), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End, color = ShopRose)
                                Text(viewModel.formatMoney(totalExpenses), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(110.dp), textAlign = TextAlign.End, color = Color(0xFF0F766E))
                                Text(viewModel.formatMoney(totalNetMargin), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(120.dp), textAlign = TextAlign.End, color = if (totalNetMargin >= 0) ShopEmerald else ShopRose)
                                Spacer(modifier = Modifier.width(70.dp))
                            }
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        DailyLedgerDialog(
            entry = editingEntry,
            selectedMonth = selectedMonth,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (showExportDialog) {
        ExportShareDialog(
            viewModel = viewModel,
            target = ExportTarget.LEDGER,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun SummaryPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    bg: Color,
    textColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
        }
    }
}

@Composable
fun DailyLedgerDialog(
    entry: DailyLedgerEntry?,
    selectedMonth: String,
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    var date by remember { mutableStateOf(entry?.date ?: "$selectedMonth-29") }
    var saleText by remember { mutableStateOf(entry?.sale?.toString() ?: "450.000") }
    var storePurchaseText by remember { mutableStateOf(entry?.storePurchase?.toString() ?: "0.000") }
    var localExpText by remember { mutableStateOf(entry?.localPurchaseAndExpense?.toString() ?: "110.000") }
    var returnsText by remember { mutableStateOf(entry?.returns?.toString() ?: "0.000") }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }

    val sale = saleText.toDoubleOrNull() ?: 0.0
    val store = storePurchaseText.toDoubleOrNull() ?: 0.0
    val local = localExpText.toDoubleOrNull() ?: 0.0
    val ret = returnsText.toDoubleOrNull() ?: 0.0
    val totalExp = store + local + ret
    val netMargin = sale - totalExp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (entry == null) "Record Daily Ledger" else "Edit Ledger Entry", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = saleText,
                    onValueChange = { saleText = it },
                    label = { Text("Daily Sales") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storePurchaseText,
                    onValueChange = { storePurchaseText = it },
                    label = { Text("Store Purchase") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = localExpText,
                    onValueChange = { localExpText = it },
                    label = { Text("Local Purchase & Expenses") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = returnsText,
                    onValueChange = { returnsText = it },
                    label = { Text("Returns") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Calculated live summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (netMargin >= 0) ShopEmeraldContainer else ShopRoseContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Daily Expenses: ${viewModel.formatMoney(totalExp)}", fontSize = 12.sp)
                        Text(
                            "Daily Net Margin: ${viewModel.formatMoney(netMargin)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (netMargin >= 0) ShopEmerald else ShopRose
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mYear = if (date.length >= 7) date.substring(0, 7) else selectedMonth
                    val newEntry = DailyLedgerEntry(
                        id = entry?.id ?: 0L,
                        date = date,
                        monthYear = mYear,
                        sale = sale,
                        storePurchase = store,
                        localPurchaseAndExpense = local,
                        returns = ret,
                        notes = notes
                    )
                    viewModel.saveDailyLedger(newEntry) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopEmerald)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
