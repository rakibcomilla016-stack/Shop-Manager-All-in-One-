package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfPrintManager {

    // --- 1. TAX INVOICE PDF ---
    fun createInvoicePdf(
        context: Context,
        profile: ShopProfile,
        invoice: Invoice,
        items: List<InvoiceItem>,
        currency: CurrencyInfo
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        var y = 45f

        // Header - Shop Details
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.shopName, 297.5f, y, paint)
        y += 18f

        if (profile.shopNameArabic.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 13f
            paint.color = Color.DKGRAY
            canvas.drawText(profile.shopNameArabic, 297.5f, y, paint)
            y += 16f
        }

        paint.textSize = 9.5f
        paint.color = Color.DKGRAY
        canvas.drawText("C.R. ${profile.crNumber}  •  ${profile.country}", 297.5f, y, paint)
        y += 13f
        canvas.drawText("PH: ${profile.phone}  •  VATIN No: ${profile.vatin}", 297.5f, y, paint)
        y += 16f

        // TAX INVOICE Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12.5f
        paint.color = Color.BLACK
        canvas.drawText("TAX INVOICE", 297.5f, y, paint)
        y += 6f
        paint.strokeWidth = 1.2f
        canvas.drawLine(220f, y, 375f, y, paint)
        y += 20f

        // Invoice Meta Box
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.color = Color.BLACK

        canvas.drawText("Branch: ${invoice.branch}", 35f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Invoice #: ${invoice.invoiceNo}", 560f, y, paint)
        y += 14f

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Customer: ${invoice.customerName}", 35f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Date: ${invoice.date}", 560f, y, paint)
        y += 14f

        if (invoice.customerPhone.isNotBlank()) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Phone: ${invoice.customerPhone}", 35f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Status: ${invoice.status}", 560f, y, paint)
            y += 14f
        }

        y += 8f
        // Table Header
        paint.color = Color.rgb(240, 243, 246)
        canvas.drawRect(35f, y - 10f, 560f, y + 14f, paint)

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("No", 40f, y + 6f, paint)
        canvas.drawText("Item Description", 65f, y + 6f, paint)
        canvas.drawText("Qty", 285f, y + 6f, paint)
        canvas.drawText("Unit", 330f, y + 6f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Price (${currency.symbol})", 440f, y + 6f, paint)
        canvas.drawText("VAT", 485f, y + 6f, paint)
        canvas.drawText("Total (${currency.symbol})", 555f, y + 6f, paint)

        paint.strokeWidth = 1f
        canvas.drawLine(35f, y + 14f, 560f, y + 14f, paint)
        y += 28f

        // Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        items.forEach { item ->
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${item.itemNumber}", 40f, y, paint)

            val displayName = if (item.itemName.length > 32) item.itemName.substring(0, 30) + ".." else item.itemName
            canvas.drawText(displayName, 65f, y, paint)
            canvas.drawText(String.format("%.1f", item.quantity), 285f, y, paint)
            canvas.drawText(item.unit, 330f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.3f", item.unitPrice), 440f, y, paint)
            canvas.drawText("${item.vatRate.toInt()}%", 485f, y, paint)
            canvas.drawText(String.format("%.3f", item.totalValue), 555f, y, paint)

            paint.color = Color.rgb(220, 225, 230)
            canvas.drawLine(35f, y + 6f, 560f, y + 6f, paint)
            paint.color = Color.BLACK
            y += 18f
        }

        y += 10f
        // Totals Section
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 10f

        canvas.drawText("Subtotal:", 460f, y, paint)
        canvas.drawText(Currencies.format(invoice.subtotal, currency), 555f, y, paint)
        y += 15f

        canvas.drawText("VAT (5%):", 460f, y, paint)
        canvas.drawText(Currencies.format(invoice.vatAmount, currency), 555f, y, paint)
        y += 15f

        if (invoice.discount > 0) {
            canvas.drawText("Discount:", 460f, y, paint)
            canvas.drawText("- " + Currencies.format(invoice.discount, currency), 555f, y, paint)
            y += 15f
        }

        paint.strokeWidth = 1.5f
        canvas.drawLine(380f, y - 4f, 560f, y - 4f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("TOTAL PAYABLE:", 460f, y + 10f, paint)
        canvas.drawText(Currencies.format(invoice.totalAmount, currency), 555f, y + 10f, paint)

        // Footer Thank You
        y += 60f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.color = Color.GRAY
        canvas.drawText("Thank you for choosing ${profile.shopName}", 297.5f, y, paint)

        document.finishPage(page)

        val file = File(context.cacheDir, "Invoice_${invoice.invoiceNo}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    // --- 2. SALES LEDGER PDF ---
    fun createLedgerPdf(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        entries: List<DailyLedgerEntry>,
        currency: CurrencyInfo
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = 40f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.shopName, 297.5f, y, paint)
        y += 18f

        paint.textSize = 12f
        canvas.drawText("Monthly Sales and Expense Ledger - $monthYear", 297.5f, y, paint)
        y += 24f

        // Top Summary Block
        val totalSale = entries.sumOf { it.sale }
        val totalStore = entries.sumOf { it.storePurchase }
        val totalLocal = entries.sumOf { it.localPurchaseAndExpense }
        val totalRet = entries.sumOf { it.returns }
        val netMargin = totalSale - (totalStore + totalLocal + totalRet)

        paint.textSize = 9f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Total Sales: ${Currencies.format(totalSale, currency)}", 40f, y, paint)
        canvas.drawText("Store Purchases: ${Currencies.format(totalStore, currency)}", 220f, y, paint)
        canvas.drawText("Local Purchases: ${Currencies.format(totalLocal, currency)}", 400f, y, paint)
        y += 14f
        canvas.drawText("Total Returns: ${Currencies.format(totalRet, currency)}", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Net Profit (Cash Margin): ${Currencies.format(netMargin, currency)}", 220f, y, paint)
        y += 18f

        // Table Header
        paint.color = Color.rgb(20, 80, 80)
        canvas.drawRect(35f, y - 8f, 560f, y + 14f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        canvas.drawText("Date", 40f, y + 6f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Sale", 140f, y + 6f, paint)
        canvas.drawText("Store Pur", 220f, y + 6f, paint)
        canvas.drawText("Local Pur & Exp", 320f, y + 6f, paint)
        canvas.drawText("Returns", 390f, y + 6f, paint)
        canvas.drawText("Total Exp", 470f, y + 6f, paint)
        canvas.drawText("Net Margin", 555f, y + 6f, paint)
        y += 22f

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        entries.take(31).forEach { e ->
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(e.date, 40f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.3f", e.sale), 140f, y, paint)
            canvas.drawText(if (e.storePurchase > 0) String.format("%.3f", e.storePurchase) else "-", 220f, y, paint)
            canvas.drawText(if (e.localPurchaseAndExpense > 0) String.format("%.3f", e.localPurchaseAndExpense) else "-", 320f, y, paint)
            canvas.drawText(if (e.returns > 0) String.format("%.3f", e.returns) else "-", 390f, y, paint)
            canvas.drawText(String.format("%.3f", e.totalDailyExpenses), 470f, y, paint)
            canvas.drawText(String.format("%.3f", e.dailyNetMargin), 555f, y, paint)

            paint.color = Color.rgb(230, 235, 240)
            canvas.drawLine(35f, y + 4f, 560f, y + 4f, paint)
            paint.color = Color.BLACK
            y += 14f
        }

        // Totals
        y += 6f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format("%.3f", totalSale), 140f, y, paint)
        canvas.drawText(String.format("%.3f", totalStore), 220f, y, paint)
        canvas.drawText(String.format("%.3f", totalLocal), 320f, y, paint)
        canvas.drawText(String.format("%.3f", totalRet), 390f, y, paint)
        canvas.drawText(String.format("%.3f", totalStore + totalLocal + totalRet), 470f, y, paint)
        canvas.drawText(String.format("%.3f", netMargin), 555f, y, paint)

        document.finishPage(page)

        val file = File(context.cacheDir, "Ledger_$monthYear.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    // --- STOCK REQUISITION PDF ---
    fun createStockPdf(
        context: Context,
        profile: ShopProfile,
        items: List<StockItem>,
        currency: CurrencyInfo
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = 40f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.shopName, 297.5f, y, paint)
        y += 18f

        paint.textSize = 12f
        canvas.drawText("VEGETABLE REQUISITION / STOCK SHEET", 297.5f, y, paint)
        y += 18f

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("LOCATION: Stock List Of ${profile.branch}", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("REF NO: 08489  •  DATE: 31/08/2026", 555f, y, paint)
        y += 16f

        // Table Header
        paint.color = Color.rgb(30, 58, 138)
        canvas.drawRect(35f, y - 8f, 560f, y + 14f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("No", 40f, y + 6f, paint)
        canvas.drawText("Item Name", 75f, y + 6f, paint)
        canvas.drawText("Quantity", 300f, y + 6f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rate", 440f, y + 6f, paint)
        canvas.drawText("Amount (${currency.symbol})", 555f, y + 6f, paint)
        y += 20f

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var total = 0.0
        items.forEach { item ->
            total += item.amount
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${item.itemNumber}", 40f, y, paint)
            canvas.drawText(item.itemName, 75f, y, paint)
            canvas.drawText("${item.quantity} ${item.unit}", 300f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.3f", item.rate), 440f, y, paint)
            canvas.drawText(String.format("%.3f", item.amount), 555f, y, paint)

            paint.color = Color.rgb(230, 235, 240)
            canvas.drawLine(35f, y + 4f, 560f, y + 4f, paint)
            paint.color = Color.BLACK
            y += 15f
        }

        y += 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL REQUISITION: ${Currencies.format(total, currency)}", 555f, y, paint)

        // Signatures
        y += 40f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        canvas.drawText("Prepared / Counted By: ____________________", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Received / Checked By: ____________________", 555f, y, paint)

        document.finishPage(page)

        val file = File(context.cacheDir, "Stock_Requisition.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    // --- RETURN ITEMS REGISTER PDF ---
    fun createReturnPdf(
        context: Context,
        profile: ShopProfile,
        items: List<ReturnItem>,
        currency: CurrencyInfo
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = 40f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.shopName, 297.5f, y, paint)
        y += 18f

        paint.textSize = 12f
        canvas.drawText("RETURN ITEMS REGISTER (400 ITEMS)", 297.5f, y, paint)
        y += 18f

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Date: 2026-09-30  •  Register No: REG-400", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Checked By: Supervisor", 555f, y, paint)
        y += 16f

        // Table Header
        paint.color = Color.rgb(225, 29, 72) // Rose
        canvas.drawRect(35f, y - 8f, 560f, y + 14f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Sl. No.", 40f, y + 6f, paint)
        canvas.drawText("Return Item's Name", 85f, y + 6f, paint)
        canvas.drawText("Quantity", 280f, y + 6f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rate", 410f, y + 6f, paint)
        canvas.drawText("Amount (${currency.symbol})", 490f, y + 6f, paint)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Reason", 510f, y + 6f, paint)
        y += 20f

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var total = 0.0
        items.forEach { item ->
            total += item.amount
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${item.itemNumber}", 40f, y, paint)
            canvas.drawText(item.itemName, 85f, y, paint)
            canvas.drawText("${item.quantity} ${item.unit}", 280f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.3f", item.rate), 410f, y, paint)
            canvas.drawText(String.format("%.3f", item.amount), 490f, y, paint)

            paint.textAlign = Paint.Align.LEFT
            val reasonShort = if (item.reason.length > 12) item.reason.substring(0, 10) + ".." else item.reason
            canvas.drawText(reasonShort, 510f, y, paint)

            paint.color = Color.rgb(230, 235, 240)
            canvas.drawLine(35f, y + 4f, 560f, y + 4f, paint)
            paint.color = Color.BLACK
            y += 16f
        }

        y += 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL RETURNS: ${Currencies.format(total, currency)}", 555f, y, paint)

        document.finishPage(page)

        val file = File(context.cacheDir, "Return_Items_Register.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    // --- PRINT DOCUMENT VIA ANDROID PRINT MANAGER ---
    fun printPdf(context: Context, pdfFile: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val input = FileInputStream(pdfFile)
                    val output = FileOutputStream(destination?.fileDescriptor)

                    val buf = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buf).also { bytesRead = it } > 0) {
                        output.write(buf, 0, bytesRead)
                    }

                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    input.close()
                    output.close()
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

    // --- 4. SHARE PDF VIA ANDROID INTENT (WhatsApp, Gmail, Drive, etc.) ---
    fun sharePdf(context: Context, pdfFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Document via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 5. OPEN / VIEW PDF ---
    fun openPdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            // Fallback to sharing if no PDF viewer app is installed
            sharePdf(context, pdfFile, "Tax Invoice")
        }
    }
}
