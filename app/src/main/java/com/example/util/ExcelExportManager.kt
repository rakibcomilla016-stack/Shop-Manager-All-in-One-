package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelExportManager {

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    // --- 1. MONTHLY SALES & EXPENSE LEDGER EXCEL EXPORT (Matching Screenshot 3) ---
    fun exportLedgerToExcel(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        entries: List<DailyLedgerEntry>,
        currency: CurrencyInfo
    ): File {
        val totalSale = entries.sumOf { it.sale }
        val totalStore = entries.sumOf { it.storePurchase }
        val totalLocal = entries.sumOf { it.localPurchaseAndExpense }
        val totalRet = entries.sumOf { it.returns }
        val totalExp = totalStore + totalLocal + totalRet
        val netMargin = totalSale - totalExp

        val file = File(context.cacheDir, "Monthly_Ledger_${monthYear}.csv")

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Excel opens Arabic, Bengali, etc. without character mangling
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Title and Summary Block (Matching Screenshot 3 top cards)
                writer.write("Monthly Sales and Expense Ledger - $monthYear\n")
                writer.write("${escapeCsv(profile.shopName)},${escapeCsv(profile.shopNameArabic)},CR: ${profile.crNumber},VATIN: ${profile.vatin}\n\n")

                writer.write("Total Sales,${String.format("%.3f", totalSale)} ${currency.code},Net Profit (Cash Basis),${String.format("%.3f", netMargin)} ${currency.code}\n")
                writer.write("Total Store Purchase,${String.format("%.3f", totalStore)} ${currency.code},Total Daily Expenses,${String.format("%.3f", totalExp)} ${currency.code}\n")
                writer.write("Total Local Purchase & Exp,${String.format("%.3f", totalLocal)} ${currency.code}\n")
                writer.write("Total Returns,${String.format("%.3f", totalRet)} ${currency.code}\n\n")

                // Table Header Row
                writer.write("Date,Sale,Store Purchase,Local Purchase & Exp,Returns,Total Daily Expenses,Daily Net Margin,Notes\n")

                // Data Rows
                entries.forEach { e ->
                    val saleStr = String.format("%.3f", e.sale)
                    val storeStr = if (e.storePurchase > 0) String.format("%.3f", e.storePurchase) else "0.000"
                    val localStr = if (e.localPurchaseAndExpense > 0) String.format("%.3f", e.localPurchaseAndExpense) else "0.000"
                    val retStr = if (e.returns > 0) String.format("%.3f", e.returns) else "0.000"
                    val expStr = String.format("%.3f", e.totalDailyExpenses)
                    val marginStr = String.format("%.3f", e.dailyNetMargin)

                    writer.write("${e.date},$saleStr,$storeStr,$localStr,$retStr,$expStr,$marginStr,${escapeCsv(e.notes)}\n")
                }

                // Total Summary Row
                writer.write("TOTAL,${String.format("%.3f", totalSale)},${String.format("%.3f", totalStore)},${String.format("%.3f", totalLocal)},${String.format("%.3f", totalRet)},${String.format("%.3f", totalExp)},${String.format("%.3f", netMargin)},\n")
            }
        }
        return file
    }

    // --- 2. RETURN ITEMS REGISTER EXCEL EXPORT (Matching Screenshot 1) ---
    fun exportReturnItemsToExcel(
        context: Context,
        profile: ShopProfile,
        items: List<ReturnItem>,
        currency: CurrencyInfo
    ): File {
        val totalAmount = items.sumOf { it.amount }
        val file = File(context.cacheDir, "Return_Items_Register_400_Items.csv")

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Header (Matching Screenshot 1 Excel header exactly)
                writer.write("RETURN ITEMS REGISTER (400 ITEMS)\n")
                writer.write("${escapeCsv(profile.shopName)},${escapeCsv(profile.shopNameArabic)}\n")
                writer.write("Date:,2026-09-30,Register No:,REG-400,Checked By:,Supervisor\n\n")

                // Table Columns
                writer.write("Sl. No.,Return Item's Name,Quantity (Kg / Pkt),Rate,Amount (${currency.code}),Reason\n")

                // Items
                items.forEach { item ->
                    val qtyStr = "${item.quantity} ${item.unit}"
                    val rateStr = String.format("%.3f", item.rate)
                    val amountStr = String.format("%.3f", item.amount)

                    writer.write("${item.itemNumber},${escapeCsv(item.itemName)},$qtyStr,$rateStr,$amountStr,${escapeCsv(item.reason)}\n")
                }

                // Bottom Total
                writer.write("TOTAL,,,${String.format("%.3f", totalAmount)},\n")
            }
        }
        return file
    }

    // --- 3. STOCK REQUISITION SHEET EXCEL EXPORT (Matching Screenshot 4) ---
    fun exportStockToExcel(
        context: Context,
        profile: ShopProfile,
        items: List<StockItem>,
        currency: CurrencyInfo
    ): File {
        val totalAmount = items.sumOf { it.amount }
        val file = File(context.cacheDir, "Stock_Requisition_Sheet_08489.csv")

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("${escapeCsv(profile.shopName)},REF NO: 08489,DATE: 31/08/2026,TOTAL: ${String.format("%.3f", totalAmount)} ${currency.code}\n")
                writer.write("LOCATION: Stock List Of ${escapeCsv(profile.branch)}\n\n")

                writer.write("No.,Category,Items Name,Quantity,Unit,Rate (${currency.code}),Amount (${currency.code})\n")

                items.forEach { item ->
                    writer.write("${item.itemNumber},${escapeCsv(item.category)},${escapeCsv(item.itemName)},${item.quantity},${item.unit},${String.format("%.3f", item.rate)},${String.format("%.3f", item.amount)}\n")
                }

                writer.write("TOTAL,,,,,${String.format("%.3f", totalAmount)}\n\n")
                writer.write("Prepared / Counted By:,Staff Member,Received / Checked By:,Store Supervisor,Authorized Stamp:,Approved\n")
            }
        }
        return file
    }

    // --- 4. INVOICES EXCEL EXPORT ---
    fun exportInvoicesToExcel(
        context: Context,
        profile: ShopProfile,
        invoices: List<Invoice>,
        currency: CurrencyInfo
    ): File {
        val file = File(context.cacheDir, "Invoices_Register.csv")

        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write("TAX INVOICES REGISTER - ${escapeCsv(profile.shopName)}\n")
                writer.write("Invoice No,Date,Branch,Customer Name,Customer Phone,Status,Subtotal,VAT Amount,Discount,Total Amount (${currency.code})\n")

                invoices.forEach { inv ->
                    writer.write("${inv.invoiceNo},${inv.date},${escapeCsv(inv.branch)},${escapeCsv(inv.customerName)},${inv.customerPhone},${inv.status},${String.format("%.3f", inv.subtotal)},${String.format("%.3f", inv.vatAmount)},${String.format("%.3f", inv.discount)},${String.format("%.3f", inv.totalAmount)}\n")
                }
            }
        }
        return file
    }

    // --- 5. OPEN EXCEL FILE (IN MICROSOFT EXCEL / GOOGLE SHEETS) ---
    fun openExcelFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open in Excel or Google Sheets"))
        } catch (e: Exception) {
            shareExcelFile(context, file, "Excel Spreadsheet")
        }
    }

    fun openInGoogleSheets(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                setPackage("com.google.android.apps.docs.editors.sheets")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openExcelFile(context, file)
        }
    }

    fun openInExcel(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                setPackage("com.microsoft.office.excel")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openExcelFile(context, file)
        }
    }

    // --- 6. SHARE EXCEL FILE (VIA WHATSAPP / GMAIL / DRIVE) ---
    fun shareExcelFile(context: Context, file: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Here is the Excel spreadsheet export: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Excel File via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share Excel file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
