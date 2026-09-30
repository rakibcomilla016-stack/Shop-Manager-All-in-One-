package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.ShopProfile
import com.example.ui.ShopViewModel
import com.example.ui.components.ExportShareDialog
import com.example.ui.components.ExportTarget
import com.example.ui.components.ShopTopBar
import com.example.ui.theme.*
import com.example.util.ExcelExportManager
import com.example.util.PdfPrintManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val invoices by viewModel.invoices.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    var showCreateDialog by remember { mutableStateOf(false) }
    var viewingInvoice by remember { mutableStateOf<Invoice?>(null) }
    var viewingInvoiceItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedInvoiceForExport by remember { mutableStateOf<Invoice?>(null) }
    var selectedInvoiceItemsForExport by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }

    val filteredInvoices = invoices.filter {
        val matchesQuery = it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.invoiceNo.contains(searchQuery, ignoreCase = true)
        val matchesStatus = if (selectedStatusFilter == "ALL") true else it.status == selectedStatusFilter
        matchesQuery && matchesStatus
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                viewModel = viewModel,
                title = viewModel.tr("inv_title")
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(viewModel.tr("inv_create_new")) },
                modifier = Modifier.testTag("create_invoice_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Search and Status Filters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by customer or invoice number...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_invoice_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ALL", "PAID", "DUE", "DRAFT").forEach { status ->
                            FilterChip(
                                selected = selectedStatusFilter == status,
                                onClick = { selectedStatusFilter = status },
                                label = { Text(status) }
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                val p = profile ?: ShopProfile()
                                val file = ExcelExportManager.exportInvoicesToExcel(context, p, invoices, currentCurrency)
                                ExcelExportManager.openExcelFile(context, file)
                            }
                        ) {
                            Icon(
                                Icons.Default.TableChart,
                                contentDescription = "Export Invoices to Excel",
                                tint = Color(0xFF107C41)
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedInvoiceForExport = null
                                selectedInvoiceItemsForExport = emptyList()
                                showExportDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "More Export Options (Google Sheets, Slides, WhatsApp)",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Invoices List
            if (filteredInvoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = ShopSlate300,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No invoices found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Create New Invoice' to generate a tax invoice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredInvoices) { inv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val items = viewModel.getInvoiceItems(inv.id)
                                        viewingInvoice = inv
                                        viewingInvoiceItems = items
                                    }
                                }
                                .testTag("invoice_card_${inv.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = inv.customerName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${inv.invoiceNo} • ${inv.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (inv.customerPhone.isNotBlank()) {
                                            Text(
                                                text = "📞 ${inv.customerPhone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ShopNavyLight
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = viewModel.formatMoney(inv.totalAmount),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (inv.status) {
                                                "PAID" -> ShopEmeraldContainer
                                                "DUE" -> ShopAmberContainer
                                                else -> ShopSlate200
                                            }
                                        ) {
                                            Text(
                                                text = inv.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (inv.status) {
                                                    "PAID" -> ShopEmerald
                                                    "DUE" -> ShopAmber
                                                    else -> ShopSlate700
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = ShopSlate100)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Action Buttons (PDF, Print, Share, Delete)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Branch: ${inv.branch}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // PDF Button
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val items = viewModel.getInvoiceItems(inv.id)
                                                    val p = profile ?: ShopProfile()
                                                    val pdf = PdfPrintManager.createInvoicePdf(context, p, inv, items, currentCurrency)
                                                    PdfPrintManager.openPdf(context, pdf)
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = ShopRose, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShopRose)
                                        }

                                        // Print Button
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val items = viewModel.getInvoiceItems(inv.id)
                                                    val p = profile ?: ShopProfile()
                                                    val pdf = PdfPrintManager.createInvoicePdf(context, p, inv, items, currentCurrency)
                                                    PdfPrintManager.printPdf(context, pdf, "Invoice_${inv.invoiceNo}")
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = "Print", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Print", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Share Button
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val items = viewModel.getInvoiceItems(inv.id)
                                                    val p = profile ?: ShopProfile()
                                                    val pdf = PdfPrintManager.createInvoicePdf(context, p, inv, items, currentCurrency)
                                                    PdfPrintManager.sharePdf(context, pdf, "Tax Invoice ${inv.invoiceNo}")
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = ShopEmerald, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShopEmerald)
                                        }

                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val items = viewModel.getInvoiceItems(inv.id)
                                                    selectedInvoiceForExport = inv
                                                    selectedInvoiceItemsForExport = items
                                                    showExportDialog = true
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Export & Share Options",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = { viewModel.deleteInvoice(inv.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = ShopRose,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Invoice Detail & Print/PDF Preview Dialog
    viewingInvoice?.let { inv ->
        InvoiceDetailDialog(
            invoice = inv,
            items = viewingInvoiceItems,
            viewModel = viewModel,
            onDismiss = { viewingInvoice = null }
        )
    }

    // Create New Invoice Dialog
    if (showCreateDialog) {
        CreateInvoiceDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showExportDialog) {
        ExportShareDialog(
            viewModel = viewModel,
            target = ExportTarget.INVOICE,
            selectedInvoice = selectedInvoiceForExport,
            invoiceItems = selectedInvoiceItemsForExport,
            onDismiss = {
                showExportDialog = false
                selectedInvoiceForExport = null
                selectedInvoiceItemsForExport = emptyList()
            }
        )
    }
}

@Composable
fun CreateInvoiceDialog(
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf(profile?.branch ?: "Main Branch") }
    var status by remember { mutableStateOf("PAID") }
    var discountText by remember { mutableStateOf("0.0") }

    // Line items list
    data class TempItem(
        val name: String,
        val qty: Double,
        val unit: String,
        val price: Double,
        val vatRate: Double = 5.0
    )

    var itemsList by remember {
        mutableStateOf(
            listOf(
                TempItem("Royal Gala Apple", 5.0, "Ctn", 7.000, 5.0),
                TempItem("Fresh Potato 25kg", 4.0, "Bag", 4.200, 5.0)
            )
        )
    }

    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemUnit by remember { mutableStateOf("Kg") }
    var newItemPrice by remember { mutableStateOf("1.000") }

    val subtotal = itemsList.sumOf { it.qty * it.price }
    val vatAmount = itemsList.sumOf { (it.qty * it.price) * (it.vatRate / 100.0) }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal + vatAmount - discount).coerceAtLeast(0.0)

    val dateToday = "2026-09-30"
    val nextInvNo = "INV-${System.currentTimeMillis() % 10000}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header (Matching Screenshot 2 action bar: Draft, New, Save, PDF, Share, Print)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Tax Invoice",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Bar matching Screenshot 2: [Save] [PDF] [Share] [Print]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ShopSlate100),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val inv = Invoice(
                                    invoiceNo = nextInvNo,
                                    date = dateToday,
                                    branch = branch,
                                    customerName = customerName.ifBlank { "Walk-in Customer" },
                                    customerPhone = customerPhone,
                                    status = status,
                                    subtotal = subtotal,
                                    vatRate = 5.0,
                                    vatAmount = vatAmount,
                                    discount = discount,
                                    totalAmount = grandTotal,
                                    notes = ""
                                )
                                val invItems = itemsList.mapIndexed { idx, it ->
                                    InvoiceItem(
                                        invoiceId = 0L,
                                        itemNumber = idx + 1,
                                        itemName = it.name,
                                        quantity = it.qty,
                                        unit = it.unit,
                                        unitPrice = it.price,
                                        vatRate = it.vatRate,
                                        totalValue = it.qty * it.price
                                    )
                                }
                                viewModel.saveInvoice(inv, invItems) {
                                    Toast.makeText(context, "Invoice Saved Successfully!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp)
                        }

                        // PDF direct
                        OutlinedButton(
                            onClick = {
                                val inv = Invoice(
                                    invoiceNo = nextInvNo,
                                    date = dateToday,
                                    branch = branch,
                                    customerName = customerName.ifBlank { "Customer" },
                                    customerPhone = customerPhone,
                                    status = status,
                                    subtotal = subtotal,
                                    vatRate = 5.0,
                                    vatAmount = vatAmount,
                                    discount = discount,
                                    totalAmount = grandTotal
                                )
                                val invItems = itemsList.mapIndexed { idx, it ->
                                    InvoiceItem(invoiceId = 0L, itemNumber = idx + 1, itemName = it.name, quantity = it.qty, unit = it.unit, unitPrice = it.price, totalValue = it.qty * it.price)
                                }
                                val p = profile ?: ShopProfile()
                                val pdf = PdfPrintManager.createInvoicePdf(context, p, inv, invItems, currentCurrency)
                                PdfPrintManager.openPdf(context, pdf)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ShopRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 12.sp, color = ShopRose)
                        }

                        // Share direct
                        OutlinedButton(
                            onClick = {
                                val inv = Invoice(
                                    invoiceNo = nextInvNo,
                                    date = dateToday,
                                    branch = branch,
                                    customerName = customerName.ifBlank { "Customer" },
                                    customerPhone = customerPhone,
                                    status = status,
                                    subtotal = subtotal,
                                    vatRate = 5.0,
                                    vatAmount = vatAmount,
                                    discount = discount,
                                    totalAmount = grandTotal
                                )
                                val invItems = itemsList.mapIndexed { idx, it ->
                                    InvoiceItem(invoiceId = 0L, itemNumber = idx + 1, itemName = it.name, quantity = it.qty, unit = it.unit, unitPrice = it.price, totalValue = it.qty * it.price)
                                }
                                val p = profile ?: ShopProfile()
                                val pdf = PdfPrintManager.createInvoicePdf(context, p, inv, invItems, currentCurrency)
                                PdfPrintManager.sharePdf(context, pdf, "Tax Invoice $nextInvNo")
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = ShopEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp, color = ShopEmerald)
                        }

                        // Print direct
                        OutlinedButton(
                            onClick = {
                                val inv = Invoice(
                                    invoiceNo = nextInvNo,
                                    date = dateToday,
                                    branch = branch,
                                    customerName = customerName.ifBlank { "Customer" },
                                    customerPhone = customerPhone,
                                    status = status,
                                    subtotal = subtotal,
                                    vatRate = 5.0,
                                    vatAmount = vatAmount,
                                    discount = discount,
                                    totalAmount = grandTotal
                                )
                                val invItems = itemsList.mapIndexed { idx, it ->
                                    InvoiceItem(invoiceId = 0L, itemNumber = idx + 1, itemName = it.name, quantity = it.qty, unit = it.unit, unitPrice = it.price, totalValue = it.qty * it.price)
                                }
                                val p = profile ?: ShopProfile()
                                val pdf = PdfPrintManager.createInvoicePdf(context, p, inv, invItems, currentCurrency)
                                PdfPrintManager.printPdf(context, pdf, "Invoice_$nextInvNo")
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Invoice metadata
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(viewModel.tr("inv_customer")) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_customer_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text(viewModel.tr("inv_phone")) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text(viewModel.tr("inv_branch")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Payment Status: ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf("PAID", "DUE", "DRAFT").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Items list header
                Text(
                    text = "Items & Produce List",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Added items table
                itemsList.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "${item.qty} ${item.unit} @ ${viewModel.formatMoney(item.price)} + VAT ${item.vatRate}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                viewModel.formatMoney(item.qty * item.price),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    itemsList = itemsList.filterIndexed { i, _ -> i != index }
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = ShopRose)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add item inputs row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSlate50)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("+ Add Line Item", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            placeholder = { Text("Item Name (e.g. Tomato Box)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newItemQty,
                                onValueChange = { newItemQty = it },
                                placeholder = { Text("Qty") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = newItemUnit,
                                onValueChange = { newItemUnit = it },
                                placeholder = { Text("Unit") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedTextField(
                                value = newItemPrice,
                                onValueChange = { newItemPrice = it },
                                placeholder = { Text("Price") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newItemName.isNotBlank()) {
                                    val q = newItemQty.toDoubleOrNull() ?: 1.0
                                    val p = newItemPrice.toDoubleOrNull() ?: 1.0
                                    itemsList = itemsList + TempItem(newItemName, q, newItemUnit, p, 5.0)
                                    newItemName = ""
                                    newItemQty = "1"
                                    newItemPrice = "1.000"
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add Item to Invoice")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                // Summary Table
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.tr("inv_subtotal"), fontSize = 13.sp)
                        Text(viewModel.formatMoney(subtotal), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(viewModel.tr("inv_vat_total") + " (5%)", fontSize = 13.sp)
                        Text(viewModel.formatMoney(vatAmount), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(viewModel.tr("inv_discount"), fontSize = 13.sp)
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            modifier = Modifier.width(120.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            viewModel.tr("inv_grand_total"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            viewModel.formatMoney(grandTotal),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Action
                Button(
                    onClick = {
                        val inv = Invoice(
                            invoiceNo = nextInvNo,
                            date = dateToday,
                            branch = branch,
                            customerName = customerName.ifBlank { "Walk-in Customer" },
                            customerPhone = customerPhone,
                            status = status,
                            subtotal = subtotal,
                            vatRate = 5.0,
                            vatAmount = vatAmount,
                            discount = discount,
                            totalAmount = grandTotal,
                            notes = ""
                        )
                        val invItems = itemsList.mapIndexed { idx, it ->
                            InvoiceItem(
                                invoiceId = 0L,
                                itemNumber = idx + 1,
                                itemName = it.name,
                                quantity = it.qty,
                                unit = it.unit,
                                unitPrice = it.price,
                                vatRate = it.vatRate,
                                totalValue = it.qty * it.price
                            )
                        }
                        viewModel.saveInvoice(inv, invItems) {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_invoice_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewModel.tr("btn_save_invoice"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailDialog(
    invoice: Invoice,
    items: List<InvoiceItem>,
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    var showDetailExportDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Top controls & action buttons (PDF, Print, Share, Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tax Invoice Receipt", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Prominent PDF, Print, Share buttons matching Screenshot 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PDF Button
                    Button(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val pdf = PdfPrintManager.createInvoicePdf(context, p, invoice, items, currentCurrency)
                            PdfPrintManager.openPdf(context, pdf)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopRose),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontWeight = FontWeight.Bold)
                    }

                    // Print Button
                    Button(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val pdf = PdfPrintManager.createInvoicePdf(context, p, invoice, items, currentCurrency)
                            PdfPrintManager.printPdf(context, pdf, "Invoice_${invoice.invoiceNo}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print", fontWeight = FontWeight.Bold)
                    }

                    // Share Button
                    Button(
                        onClick = {
                            val p = profile ?: ShopProfile()
                            val pdf = PdfPrintManager.createInvoicePdf(context, p, invoice, items, currentCurrency)
                            PdfPrintManager.sharePdf(context, pdf, "Tax Invoice ${invoice.invoiceNo}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { showDetailExportDialog = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "More Export Options (Excel, Google Sheets, WhatsApp)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Paper styled tax invoice layout (Matching Screenshot 2)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile?.shopName ?: "SHOLAT AL MUJAD AL SHAMILAH TRAD. - REC.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        if (!profile?.shopNameArabic.isNullOrBlank()) {
                            Text(
                                text = profile?.shopNameArabic ?: "",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                        }
                        Text(
                            text = "C.R. ${profile?.crNumber ?: "1208612"}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${profile?.country ?: "Sultanate of Oman"}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "PH: ${profile?.phone ?: "92152565"}   VATIN No: ${profile?.vatin ?: "OM1208612000"}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TAX INVOICE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Divider(modifier = Modifier.width(140.dp), thickness = 1.5.dp, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Branch, Customer, Date
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Branch: ${invoice.branch}", fontSize = 12.sp, color = Color.Black)
                                Text("Invoice #: ${invoice.invoiceNo}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Customer: ${invoice.customerName}", fontSize = 12.sp, color = Color.Black)
                                Text("Date: ${invoice.date}", fontSize = 12.sp, color = Color.Black)
                            }
                            if (invoice.customerPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Phone: ${invoice.customerPhone}", fontSize = 12.sp, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Items Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ShopSlate100)
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), color = Color.Black)
                            Text("Item", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = Color.Black)
                            Text("Qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), color = Color.Black)
                            Text("Price", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.End, color = Color.Black)
                            Text("Value", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.End, color = Color.Black)
                        }
                        Divider(color = Color.Black)

                        // Items rows
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.itemNumber}", fontSize = 11.sp, modifier = Modifier.width(24.dp), color = Color.Black)
                                Text(item.itemName, fontSize = 11.sp, modifier = Modifier.weight(1f), color = Color.Black)
                                Text("${item.quantity} ${item.unit}", fontSize = 11.sp, modifier = Modifier.width(36.dp), color = Color.Black)
                                Text(viewModel.formatMoney(item.unitPrice), fontSize = 11.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.End, color = Color.Black)
                                Text(viewModel.formatMoney(item.totalValue), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(60.dp), textAlign = TextAlign.End, color = Color.Black)
                            }
                            Divider(color = ShopSlate200)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 12.sp, color = Color.Black)
                                Text(viewModel.formatMoney(invoice.subtotal), fontSize = 12.sp, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("VAT (5%):", fontSize = 12.sp, color = Color.Black)
                                Text(viewModel.formatMoney(invoice.vatAmount), fontSize = 12.sp, color = Color.Black)
                            }
                            if (invoice.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", fontSize = 12.sp, color = Color.Black)
                                    Text("- ${viewModel.formatMoney(invoice.discount)}", fontSize = 12.sp, color = Color.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = Color.Black, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL PAYABLE:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Text(viewModel.formatMoney(invoice.totalAmount), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }

    if (showDetailExportDialog) {
        ExportShareDialog(
            viewModel = viewModel,
            target = ExportTarget.INVOICE,
            selectedInvoice = invoice,
            invoiceItems = items,
            onDismiss = { showDetailExportDialog = false }
        )
    }
}
