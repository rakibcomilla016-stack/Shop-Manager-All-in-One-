package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.ShopViewModel
import com.example.ui.theme.*
import com.example.util.ExcelExportManager
import com.example.util.PdfPrintManager
import com.example.util.SlidesExportManager

enum class ExportTarget {
    ALL_IN_ONE,
    LEDGER,
    INVOICE,
    STOCK,
    RETURNS
}

@Composable
fun ExportShareDialog(
    viewModel: ShopViewModel,
    target: ExportTarget = ExportTarget.ALL_IN_ONE,
    selectedInvoice: Invoice? = null,
    invoiceItems: List<InvoiceItem> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val monthlyLedger by viewModel.monthlyLedger.collectAsState()
    val stockItems by viewModel.stockItems.collectAsState()
    val returnItems by viewModel.returnItems.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val p = profile ?: ShopProfile()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .testTag("export_share_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ShopEmerald.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = ShopEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export, Print & Share",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (target) {
                                    ExportTarget.ALL_IN_ONE -> "Executive Business Hub • $selectedMonth"
                                    ExportTarget.LEDGER -> "Monthly Sales & Expense Ledger • $selectedMonth"
                                    ExportTarget.INVOICE -> "Tax Invoice #${selectedInvoice?.invoiceNo ?: ""}"
                                    ExportTarget.STOCK -> "Stock Requisition Sheet"
                                    ExportTarget.RETURNS -> "Return Items Register (400 Items)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ShopSlate200)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content of options
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. EXCEL & GOOGLE SHEETS SECTION
                    ExportSectionHeader(
                        title = "1. Excel File & Google Sheets",
                        subtitle = "Full spreadsheet table with numbers, formulas & headers"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportOptionTile(
                            modifier = Modifier.weight(1f),
                            title = "Microsoft Excel",
                            subtitle = "Open in Excel (.csv / .xls)",
                            icon = Icons.Default.TableChart,
                            badge = ".XLSX",
                            badgeColor = Color(0xFF107C41),
                            containerColor = Color(0xFFE8F5E9),
                            iconColor = Color(0xFF107C41),
                            onClick = {
                                when (target) {
                                    ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
                                        val file = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        ExcelExportManager.openInExcel(context, file)
                                    }
                                    ExportTarget.STOCK -> {
                                        val file = ExcelExportManager.exportStockToExcel(context, p, stockItems, currentCurrency)
                                        ExcelExportManager.openInExcel(context, file)
                                    }
                                    ExportTarget.RETURNS -> {
                                        val file = ExcelExportManager.exportReturnItemsToExcel(context, p, returnItems, currentCurrency)
                                        ExcelExportManager.openInExcel(context, file)
                                    }
                                    ExportTarget.INVOICE -> {
                                        val file = ExcelExportManager.exportInvoicesToExcel(context, p, listOfNotNull(selectedInvoice), currentCurrency)
                                        ExcelExportManager.openInExcel(context, file)
                                    }
                                }
                            }
                        )

                        ExportOptionTile(
                            modifier = Modifier.weight(1f),
                            title = "Google Sheets",
                            subtitle = "Open in Google Sheets",
                            icon = Icons.Default.GridOn,
                            badge = "SHEETS",
                            badgeColor = Color(0xFF0F9D58),
                            containerColor = Color(0xFFE8F5E9),
                            iconColor = Color(0xFF0F9D58),
                            onClick = {
                                when (target) {
                                    ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
                                        val file = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        ExcelExportManager.openInGoogleSheets(context, file)
                                    }
                                    ExportTarget.STOCK -> {
                                        val file = ExcelExportManager.exportStockToExcel(context, p, stockItems, currentCurrency)
                                        ExcelExportManager.openInGoogleSheets(context, file)
                                    }
                                    ExportTarget.RETURNS -> {
                                        val file = ExcelExportManager.exportReturnItemsToExcel(context, p, returnItems, currentCurrency)
                                        ExcelExportManager.openInGoogleSheets(context, file)
                                    }
                                    ExportTarget.INVOICE -> {
                                        val file = ExcelExportManager.exportInvoicesToExcel(context, p, listOfNotNull(selectedInvoice), currentCurrency)
                                        ExcelExportManager.openInGoogleSheets(context, file)
                                    }
                                }
                            }
                        )
                    }

                    // Share Excel file button
                    OutlinedButton(
                        onClick = {
                            when (target) {
                                ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
                                    val file = ExcelExportManager.exportLedgerToExcel(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                    ExcelExportManager.shareExcelFile(context, file, "Sales & Expense Ledger ($selectedMonth)")
                                }
                                ExportTarget.STOCK -> {
                                    val file = ExcelExportManager.exportStockToExcel(context, p, stockItems, currentCurrency)
                                    ExcelExportManager.shareExcelFile(context, file, "Stock Requisition Sheet")
                                }
                                ExportTarget.RETURNS -> {
                                    val file = ExcelExportManager.exportReturnItemsToExcel(context, p, returnItems, currentCurrency)
                                    ExcelExportManager.shareExcelFile(context, file, "Return Items Register (400 Items)")
                                }
                                ExportTarget.INVOICE -> {
                                    val file = ExcelExportManager.exportInvoicesToExcel(context, p, listOfNotNull(selectedInvoice), currentCurrency)
                                    ExcelExportManager.shareExcelFile(context, file, "Tax Invoice #${selectedInvoice?.invoiceNo}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF107C41), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Spreadsheet File via...", color = Color(0xFF107C41), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. GOOGLE SLIDES SECTION
                    ExportSectionHeader(
                        title = "2. Google Slides & Executive Presentation",
                        subtitle = "16:9 Landscape slide deck with charts, breakdown & analysis"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportOptionTile(
                            modifier = Modifier.weight(1f),
                            title = "Google Slides",
                            subtitle = "View 16:9 presentation slides",
                            icon = Icons.Default.Slideshow,
                            badge = "16:9 SLIDES",
                            badgeColor = Color(0xFFF4B400),
                            containerColor = Color(0xFFFEF3C7),
                            iconColor = Color(0xFFD97706),
                            onClick = {
                                val file = SlidesExportManager.createPresentationSlidesPdf(
                                    context, p, selectedMonth, monthlyLedger, stockItems, returnItems, currentCurrency
                                )
                                SlidesExportManager.openInGoogleSlides(context, file)
                            }
                        )

                        ExportOptionTile(
                            modifier = Modifier.weight(1f),
                            title = "Share Slides Deck",
                            subtitle = "Send slides via WhatsApp/Drive",
                            icon = Icons.Default.SendToMobile,
                            badge = "PRESENTATION",
                            badgeColor = Color(0xFFD97706),
                            containerColor = Color(0xFFFEF3C7),
                            iconColor = Color(0xFFB45309),
                            onClick = {
                                val file = SlidesExportManager.createPresentationSlidesPdf(
                                    context, p, selectedMonth, monthlyLedger, stockItems, returnItems, currentCurrency
                                )
                                SlidesExportManager.sharePresentation(context, file, "Executive Presentation - $selectedMonth")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. PDF DOCUMENT & PRINT SECTION
                    ExportSectionHeader(
                        title = "3. PDF Document & Print Options",
                        subtitle = "Professional formatted print-ready documents & invoices"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportOptionTile(
                            modifier = Modifier.weight(1f),
                            title = "PDF Document",
                            subtitle = "View / Open styled PDF",
                            icon = Icons.Default.PictureAsPdf,
                            badge = "A4 PDF",
                            badgeColor = ShopRose,
                            containerColor = Color(0xFFFFE4E6),
                            iconColor = ShopRose,
                            onClick = {
                                when (target) {
                                    ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
                                        val file = PdfPrintManager.createLedgerPdf(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        PdfPrintManager.openPdf(context, file)
                                    }
                                    ExportTarget.STOCK -> {
                                        val file = PdfPrintManager.createStockPdf(context, p, stockItems, currentCurrency)
                                        PdfPrintManager.openPdf(context, file)
                                    }
                                    ExportTarget.RETURNS -> {
                                        val file = PdfPrintManager.createReturnPdf(context, p, returnItems, currentCurrency)
                                        PdfPrintManager.openPdf(context, file)
                                    }
                                    ExportTarget.INVOICE -> {
                                        val inv = selectedInvoice ?: return@ExportOptionTile
                                        val file = PdfPrintManager.createInvoicePdf(context, p, inv, invoiceItems, currentCurrency)
                                        PdfPrintManager.openPdf(context, file)
                                    }
                                }
                            }
                        )

                        ExportOptionTile(
                            modifier = Modifier.weight(1f),
                            title = "Print Document",
                            subtitle = "Direct Android PrintManager",
                            icon = Icons.Default.Print,
                            badge = "PRINT",
                            badgeColor = ShopNavy,
                            containerColor = Color(0xFFE0E7FF),
                            iconColor = ShopNavy,
                            onClick = {
                                when (target) {
                                    ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
                                        val file = PdfPrintManager.createLedgerPdf(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                        PdfPrintManager.printPdf(context, file, "Ledger_$selectedMonth")
                                    }
                                    ExportTarget.STOCK -> {
                                        val file = PdfPrintManager.createStockPdf(context, p, stockItems, currentCurrency)
                                        PdfPrintManager.printPdf(context, file, "Stock_Requisition")
                                    }
                                    ExportTarget.RETURNS -> {
                                        val file = PdfPrintManager.createReturnPdf(context, p, returnItems, currentCurrency)
                                        PdfPrintManager.printPdf(context, file, "Return_Register")
                                    }
                                    ExportTarget.INVOICE -> {
                                        val inv = selectedInvoice ?: return@ExportOptionTile
                                        val file = PdfPrintManager.createInvoicePdf(context, p, inv, invoiceItems, currentCurrency)
                                        PdfPrintManager.printPdf(context, file, "Invoice_${inv.invoiceNo}")
                                    }
                                }
                            }
                        )
                    }

                    // Share PDF button
                    OutlinedButton(
                        onClick = {
                            when (target) {
                                ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
                                    val file = PdfPrintManager.createLedgerPdf(context, p, selectedMonth, monthlyLedger, currentCurrency)
                                    PdfPrintManager.sharePdf(context, file, "Monthly Sales Ledger - $selectedMonth")
                                }
                                ExportTarget.STOCK -> {
                                    val file = PdfPrintManager.createStockPdf(context, p, stockItems, currentCurrency)
                                    PdfPrintManager.sharePdf(context, file, "Stock Requisition Sheet")
                                }
                                ExportTarget.RETURNS -> {
                                    val file = PdfPrintManager.createReturnPdf(context, p, returnItems, currentCurrency)
                                    PdfPrintManager.sharePdf(context, file, "Return Items Register")
                                }
                                ExportTarget.INVOICE -> {
                                    val inv = selectedInvoice ?: return@ExportOptionTile
                                    val file = PdfPrintManager.createInvoicePdf(context, p, inv, invoiceItems, currentCurrency)
                                    PdfPrintManager.sharePdf(context, file, "Tax Invoice #${inv.invoiceNo}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = ShopRose, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share PDF via WhatsApp / Email...", color = ShopRose, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. INSTANT WHATSAPP TEXT MESSAGE SHARING
                    ExportSectionHeader(
                        title = "4. Quick WhatsApp / SMS Text Summary",
                        subtitle = "Send instant formatted text summary directly to customer or boss"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val textSummary = buildWhatsAppSummary(target, p, selectedMonth, monthlyLedger, stockItems, returnItems, selectedInvoice, invoiceItems, currentCurrency)
                                shareToWhatsApp(context, textSummary)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send to WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val textSummary = buildWhatsAppSummary(target, p, selectedMonth, monthlyLedger, stockItems, returnItems, selectedInvoice, invoiceItems, currentCurrency)
                                copyToClipboard(context, textSummary)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Summary Text", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExportSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExportOptionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, ShopSlate200, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = ShopSlate700,
                maxLines = 1
            )
        }
    }
}

// Format clean text summaries for instant WhatsApp messaging
private fun buildWhatsAppSummary(
    target: ExportTarget,
    profile: ShopProfile,
    monthYear: String,
    ledgerEntries: List<DailyLedgerEntry>,
    stockItems: List<StockItem>,
    returnItems: List<ReturnItem>,
    invoice: Invoice?,
    invoiceItems: List<InvoiceItem>,
    currency: CurrencyInfo
): String {
    return when (target) {
        ExportTarget.ALL_IN_ONE, ExportTarget.LEDGER -> {
            val totalSale = ledgerEntries.sumOf { it.sale }
            val totalStore = ledgerEntries.sumOf { it.storePurchase }
            val totalLocal = ledgerEntries.sumOf { it.localPurchaseAndExpense }
            val totalRet = ledgerEntries.sumOf { it.returns }
            val totalExp = totalStore + totalLocal + totalRet
            val netMargin = totalSale - totalExp

            """
            🏪 *${profile.shopName}*
            📊 *MONTHLY SALES & EXPENSE LEDGER* ($monthYear)
            CR: ${profile.crNumber} | VATIN: ${profile.vatin}
            ------------------------------------
            💰 *Total Sales:* ${String.format("%.3f", totalSale)} ${currency.code}
            🛒 *Store Purchases:* ${String.format("%.3f", totalStore)} ${currency.code}
            🏷️ *Local Expenses:* ${String.format("%.3f", totalLocal)} ${currency.code}
            🔄 *Total Returns:* ${String.format("%.3f", totalRet)} ${currency.code}
            📉 *Total Expenses:* ${String.format("%.3f", totalExp)} ${currency.code}
            ------------------------------------
            ✅ *NET PROFIT (CASH MARGIN):* ${String.format("%.3f", netMargin)} ${currency.code}
            ------------------------------------
            Generated with Shop Manager All in One
            """.trimIndent()
        }

        ExportTarget.STOCK -> {
            val total = stockItems.sumOf { it.amount }
            val itemsText = stockItems.take(10).joinToString("\n") {
                "• ${it.itemName}: ${it.quantity} ${it.unit} @ ${String.format("%.3f", it.rate)} = ${String.format("%.3f", it.amount)} ${currency.code}"
            }

            """
            🏪 *${profile.shopName}*
            📋 *VEGETABLE & GROCERY STOCK REQUISITION*
            Ref: 08489 | Location: ${profile.branch}
            ------------------------------------
            $itemsText
            ${if (stockItems.size > 10) "• ...and ${stockItems.size - 10} more items" else ""}
            ------------------------------------
            💵 *TOTAL REQUISITION VALUE:* ${String.format("%.3f", total)} ${currency.code}
            Checked By: Store Supervisor
            """.trimIndent()
        }

        ExportTarget.RETURNS -> {
            val total = returnItems.sumOf { it.amount }
            val itemsText = returnItems.take(8).joinToString("\n") {
                "• ${it.itemName} (${it.quantity} ${it.unit}): ${String.format("%.3f", it.amount)} ${currency.code} [Reason: ${it.reason}]"
            }

            """
            🏪 *${profile.shopName}*
            🔄 *RETURN ITEMS REGISTER (400 ITEMS)*
            Date: 2026-09-30 | Reg: REG-400
            ------------------------------------
            $itemsText
            ------------------------------------
            ⚠️ *TOTAL RETURNS VALUE:* ${String.format("%.3f", total)} ${currency.code}
            Loss deducted from Daily Sales Ledger.
            """.trimIndent()
        }

        ExportTarget.INVOICE -> {
            val inv = invoice ?: return "Tax Invoice"
            val itemsText = invoiceItems.joinToString("\n") {
                "• ${it.itemName} x ${it.quantity} ${it.unit} = ${String.format("%.3f", it.totalValue)} ${currency.code}"
            }

            """
            🧾 *TAX INVOICE #${inv.invoiceNo}*
            🏪 *${profile.shopName}*
            Branch: ${inv.branch} | Date: ${inv.date}
            Customer: ${inv.customerName}
            ------------------------------------
            $itemsText
            ------------------------------------
            Subtotal: ${String.format("%.3f", inv.subtotal)} ${currency.code}
            VAT (5%): ${String.format("%.3f", inv.vatAmount)} ${currency.code}
            ${if (inv.discount > 0) "Discount: -${String.format("%.3f", inv.discount)} ${currency.code}\n" else ""}
            💰 *TOTAL PAYABLE:* ${String.format("%.3f", inv.totalAmount)} ${currency.code}
            Status: ${inv.status}
            Thank you for your business!
            """.trimIndent()
        }
    }
}

private fun shareToWhatsApp(context: Context, message: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to general text share chooser if WhatsApp isn't installed
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Share Summary via..."))
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Shop Report", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
}
