package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.model.ReturnItem
import com.example.ui.ShopViewModel
import com.example.ui.components.ExportShareDialog
import com.example.ui.components.ExportTarget
import com.example.ui.components.ShopTopBar
import com.example.ui.theme.*
import com.example.util.ExcelExportManager
import com.example.util.PdfPrintManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnRegisterScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val returnItems by viewModel.returnItems.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val totalReturns = returnItems.sumOf { it.amount }

    Scaffold(
        topBar = {
            ShopTopBar(
                viewModel = viewModel,
                title = viewModel.tr("returns_title"),
                subtitle = "Register No: REG-400 • Checked By: Supervisor"
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ShopRose,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(viewModel.tr("returns_add_item")) },
                modifier = Modifier.testTag("add_return_item_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Register Metadata Card (Matching Screenshot 1 Excel header)
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
                                "RETURN ITEMS REGISTER (400 ITEMS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ShopRose
                            )
                            Text(
                                "Date: 2026-09-30 • Checked By: Supervisor",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Returns", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                viewModel.formatMoney(totalReturns),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ShopRose
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sync to Daily Sales Ledger button
                    Button(
                        onClick = {
                            val sample = returnItems.firstOrNull() ?: ReturnItem(
                                registerNo = "REG-400",
                                date = "2026-09-30",
                                checkedBy = "Supervisor",
                                itemName = "Damaged Produce",
                                quantity = 1.0,
                                unit = "Kg",
                                rate = totalReturns,
                                amount = totalReturns
                            )
                            viewModel.syncReturnToLedger(sample) { msg ->
                                Toast.makeText(context, "Synced returns to Sales & Expense Ledger!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_to_ledger_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(viewModel.tr("returns_sync_ledger"), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Excel, PDF, Print, Share buttons (Matching Screenshot 1 Excel register)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val p = profile ?: com.example.data.model.ShopProfile()
                                val excelFile = ExcelExportManager.exportReturnItemsToExcel(context, p, returnItems, currentCurrency)
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
                                val pdf = PdfPrintManager.createReturnPdf(context, p, returnItems, currentCurrency)
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
                                val pdf = PdfPrintManager.createReturnPdf(context, p, returnItems, currentCurrency)
                                PdfPrintManager.printPdf(context, pdf, "Return_Items_Register")
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
                                val excelFile = ExcelExportManager.exportReturnItemsToExcel(context, p, returnItems, currentCurrency)
                                ExcelExportManager.shareExcelFile(context, excelFile, "Return Items Register (Excel)")
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

            // Table Header matching Screenshot 1 (Sl. No., Return Item's Name, Quantity (Kg / Pkt), Rate, Amount)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A)) // Dark navy
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sl. No.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(42.dp))
                Text("Return Item's Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("Quantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                Text("Rate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                Text("Amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(28.dp))
            }

            // Return Items List
            if (returnItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No return items logged. Tap '+ Record Return Item'.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(returnItems) { item ->
                        val isEven = returnItems.indexOf(item) % 2 == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isEven) Color.Transparent else ShopSlate50)
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${item.itemNumber}", fontSize = 11.sp, modifier = Modifier.width(42.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(item.reason, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${item.quantity} ${item.unit}", fontSize = 11.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                            Text(viewModel.formatMoney(item.rate), fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                            Text(
                                viewModel.formatMoney(item.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.width(75.dp),
                                textAlign = TextAlign.End,
                                color = ShopRose
                            )
                            IconButton(
                                onClick = { viewModel.deleteReturnItem(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ShopRose, modifier = Modifier.size(16.dp))
                            }
                        }
                        Divider(color = ShopSlate200)
                    }

                    item { Spacer(modifier = Modifier.height(70.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReturnDialog(
            nextNumber = returnItems.size + 1,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (showExportDialog) {
        ExportShareDialog(
            viewModel = viewModel,
            target = ExportTarget.RETURNS,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun AddReturnDialog(
    nextNumber: Int,
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("Kg") }
    var rateText by remember { mutableStateOf("1.500") }
    var reason by remember { mutableStateOf("Damaged Produce") }

    val qty = qtyText.toDoubleOrNull() ?: 1.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val amount = qty * rate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Return Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Return Item's Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
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
                        label = { Text("Unit (Kg/Pkt)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Rate") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Return Reason / Condition") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ShopRoseContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Total Return Amount: ${viewModel.formatMoney(amount)}",
                        fontWeight = FontWeight.Bold,
                        color = ShopRose,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank()) {
                        val newItem = ReturnItem(
                            registerNo = "REG-400",
                            date = "2026-09-30",
                            checkedBy = "Supervisor",
                            itemNumber = nextNumber,
                            itemName = itemName,
                            quantity = qty,
                            unit = unit,
                            rate = rate,
                            amount = amount,
                            reason = reason
                        )
                        viewModel.saveReturnItem(newItem) {
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopRose)
            ) {
                Text("Save Return Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
