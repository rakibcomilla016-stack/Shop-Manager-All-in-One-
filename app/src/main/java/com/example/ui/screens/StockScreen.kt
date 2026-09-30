package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockItem
import com.example.ui.ShopViewModel
import com.example.ui.components.ExportShareDialog
import com.example.ui.components.ExportTarget
import com.example.ui.components.ShopTopBar
import com.example.ui.theme.*
import com.example.util.ExcelExportManager
import com.example.util.PdfPrintManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val stockItems by viewModel.stockItems.collectAsState()
    val categoryFilter by viewModel.stockCategoryFilter.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<StockItem?>(null) }

    val totalAmount = stockItems.sumOf { it.amount }

    Scaffold(
        topBar = {
            ShopTopBar(
                viewModel = viewModel,
                title = viewModel.tr("stock_title"),
                subtitle = "Stock List Of ${profile?.branch ?: "Bu-Hassan Shop"}"
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingItem = null
                    showAddDialog = true
                },
                containerColor = ShopNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(viewModel.tr("stock_add_item")) },
                modifier = Modifier.testTag("add_stock_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Header Info Card (Matching Screenshot 4)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LOCATION: Stock List Of ${profile?.branch ?: "Bu-Hassan Shop"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "REF NO: 08489  •  DATE: 31/08/2026",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL REQUISITION", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = viewModel.formatMoney(totalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ShopEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Excel, PDF, Print, Share Action Buttons (Matching Screenshot 4 Stock Sheet)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val p = profile ?: com.example.data.model.ShopProfile()
                                val excelFile = ExcelExportManager.exportStockToExcel(context, p, stockItems, currentCurrency)
                                ExcelExportManager.openExcelFile(context, excelFile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)), // Excel Green
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
                                val p = profile ?: com.example.data.model.ShopProfile()
                                val pdf = PdfPrintManager.createStockPdf(context, p, stockItems, currentCurrency)
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
                                val p = profile ?: com.example.data.model.ShopProfile()
                                val pdf = PdfPrintManager.createStockPdf(context, p, stockItems, currentCurrency)
                                PdfPrintManager.printPdf(context, pdf, "Stock_Requisition")
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
                                val p = profile ?: com.example.data.model.ShopProfile()
                                val excelFile = ExcelExportManager.exportStockToExcel(context, p, stockItems, currentCurrency)
                                ExcelExportManager.shareExcelFile(context, excelFile, "Stock Requisition Sheet (Excel)")
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
            }

            // Category Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = when (categoryFilter) {
                    "Vegetable Requisition" -> 1
                    "Dates, Water & Groceries" -> 2
                    else -> 0
                },
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = categoryFilter == "All",
                    onClick = { viewModel.setStockCategoryFilter("All") },
                    text = { Text("All Items (${stockItems.size})") }
                )
                Tab(
                    selected = categoryFilter == "Vegetable Requisition",
                    onClick = { viewModel.setStockCategoryFilter("Vegetable Requisition") },
                    text = { Text("🥦 Vegetables & Produce") }
                )
                Tab(
                    selected = categoryFilter == "Dates, Water & Groceries",
                    onClick = { viewModel.setStockCategoryFilter("Dates, Water & Groceries") },
                    text = { Text("🥫 Dates, Water & Groceries") }
                )
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E3A8A)) // Deep blue matching header
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(30.dp))
                Text("Item Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("Quantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                Text("Rate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                Text("Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(28.dp))
            }

            // Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(stockItems) { item ->
                    val isEven = stockItems.indexOf(item) % 2 == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isEven) Color.Transparent else ShopSlate50)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${item.itemNumber}", fontSize = 11.sp, modifier = Modifier.width(30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.itemName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(item.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${item.quantity} ${item.unit}", fontSize = 11.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                        Text(viewModel.formatMoney(item.rate), fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                        Text(
                            viewModel.formatMoney(item.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.width(75.dp),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { viewModel.deleteStockItem(item.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ShopRose, modifier = Modifier.size(16.dp))
                        }
                    }
                    Divider(color = ShopSlate200)
                }

                // Footer signatures area (matching Screenshot 4 bottom)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Requisition Authorization", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Prepared / Counted By:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Staff Signature & Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Received / Checked By:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Supervisor Signature", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(70.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddStockItemDialog(
            nextNumber = stockItems.size + 1,
            defaultCategory = if (categoryFilter != "All") categoryFilter else "Vegetable Requisition",
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (showExportDialog) {
        ExportShareDialog(
            viewModel = viewModel,
            target = ExportTarget.STOCK,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun AddStockItemDialog(
    nextNumber: Int,
    defaultCategory: String,
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("Kg") }
    var rateText by remember { mutableStateOf("1.200") }
    var category by remember { mutableStateOf(defaultCategory) }

    val qty = quantityText.toDoubleOrNull() ?: 1.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val amount = qty * rate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Requisition Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name (e.g. POTATO 25 KG)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (Kg/Ctn/Bag)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Rate / Price per Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = category == "Vegetable Requisition",
                        onClick = { category = "Vegetable Requisition" },
                        label = { Text("Produce") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = category == "Dates, Water & Groceries",
                        onClick = { category = "Dates, Water & Groceries" },
                        label = { Text("Groceries") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Item Amount: ${viewModel.formatMoney(amount)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank()) {
                        val newItem = StockItem(
                            refNo = "08489",
                            date = "2026-08-31",
                            location = profile?.branch ?: "Bu-Hassan Shop",
                            category = category,
                            itemNumber = nextNumber,
                            itemName = itemName,
                            quantity = qty,
                            unit = unit,
                            rate = rate,
                            amount = amount
                        )
                        viewModel.saveStockItem(newItem) {
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
