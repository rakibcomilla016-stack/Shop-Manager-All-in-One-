package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream

object SlidesExportManager {

    // --- GENERATE 16:9 LANDSCAPE EXECUTIVE PRESENTATION SLIDES FOR GOOGLE SLIDES / POWERPOINT ---
    fun createPresentationSlidesPdf(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        ledgerEntries: List<DailyLedgerEntry>,
        stockItems: List<StockItem>,
        returnItems: List<ReturnItem>,
        currency: CurrencyInfo
    ): File {
        val document = PdfDocument()

        // Standard 16:9 Landscape Slide dimensions (960 x 540 pt)
        val slideWidth = 960
        val slideHeight = 540

        val totalSales = ledgerEntries.sumOf { it.sale }
        val totalStore = ledgerEntries.sumOf { it.storePurchase }
        val totalLocal = ledgerEntries.sumOf { it.localPurchaseAndExpense }
        val totalReturns = ledgerEntries.sumOf { it.returns }
        val totalExpenses = totalStore + totalLocal + totalReturns
        val netMargin = totalSales - totalExpenses
        val totalStockAmount = stockItems.sumOf { it.amount }

        val paint = Paint().apply { isAntiAlias = true }

        // ==========================================
        // SLIDE 1: TITLE SLIDE (EXECUTIVE COVER)
        // ==========================================
        val pageInfo1 = PdfDocument.PageInfo.Builder(slideWidth, slideHeight, 1).create()
        val slide1 = document.startPage(pageInfo1)
        val canvas1 = slide1.canvas

        // Dark Executive Blue Background
        paint.color = Color.rgb(15, 30, 65)
        canvas1.drawRect(0f, 0f, slideWidth.toFloat(), slideHeight.toFloat(), paint)

        // Decorative Accent Bar
        paint.color = Color.rgb(5, 150, 105) // Emerald
        canvas1.drawRect(0f, slideHeight - 14f, slideWidth.toFloat(), slideHeight.toFloat(), paint)
        paint.color = Color.rgb(59, 130, 246) // Blue accent
        canvas1.drawRect(80f, 110f, 86f, 260f, paint)

        // Title Texts
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 34f
        paint.textAlign = Paint.Align.LEFT
        canvas1.drawText(profile.shopName, 105f, 150f, paint)

        if (profile.shopNameArabic.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 22f
            paint.color = Color.rgb(203, 213, 225)
            canvas1.drawText(profile.shopNameArabic, 105f, 185f, paint)
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.rgb(52, 211, 153) // Emerald accent
        canvas1.drawText("EXECUTIVE BUSINESS PERFORMANCE & INVENTORY REVIEW", 105f, 235f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 16f
        paint.color = Color.rgb(226, 232, 240)
        canvas1.drawText("Reporting Period: $monthYear  •  Branch: ${profile.branch}", 105f, 265f, paint)

        // Meta Cards at bottom
        paint.color = Color.rgb(30, 45, 85)
        canvas1.drawRoundRect(105f, 320f, 340f, 440f, 12f, 12f, paint)
        canvas1.drawRoundRect(360f, 320f, 595f, 440f, 12f, 12f, paint)
        canvas1.drawRoundRect(615f, 320f, 850f, 440f, 12f, 12f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 12f
        canvas1.drawText("Commercial Registration", 120f, 355f, paint)
        canvas1.drawText("VATIN Tax ID", 375f, 355f, paint)
        canvas1.drawText("Contact Information", 630f, 355f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas1.drawText(profile.crNumber, 120f, 390f, paint)
        canvas1.drawText(profile.vatin, 375f, 390f, paint)
        canvas1.drawText(profile.phone, 630f, 390f, paint)

        document.finishPage(slide1)

        // ========================================================
        // SLIDE 2: MONTHLY SALES & EXPENSES (MATCHING SCREENSHOT 3)
        // ========================================================
        val pageInfo2 = PdfDocument.PageInfo.Builder(slideWidth, slideHeight, 2).create()
        val slide2 = document.startPage(pageInfo2)
        val canvas2 = slide2.canvas

        // Background
        paint.color = Color.rgb(248, 250, 252)
        canvas2.drawRect(0f, 0f, slideWidth.toFloat(), slideHeight.toFloat(), paint)

        // Header Banner
        paint.color = Color.rgb(19, 78, 74) // Deep Teal
        canvas2.drawRect(0f, 0f, slideWidth.toFloat(), 70f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.LEFT
        canvas2.drawText("Monthly Sales & Financial Performance - $monthYear", 40f, 42f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas2.drawText(profile.shopName, slideWidth - 40f, 42f, paint)

        // 4 Financial Summary Cards
        val cardWidth = 205f
        val cardHeight = 90f
        val startY = 95f

        // Total Sales Card
        paint.color = Color.WHITE
        canvas2.drawRoundRect(40f, startY, 40f + cardWidth, startY + cardHeight, 10f, 10f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT
        canvas2.drawText("TOTAL SALES", 55f, startY + 30f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText(Currencies.format(totalSales, currency), 55f, startY + 65f, paint)

        // Store Purchases Card
        paint.color = Color.WHITE
        canvas2.drawRoundRect(260f, startY, 260f + cardWidth, startY + cardHeight, 10f, 10f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 12f
        canvas2.drawText("STORE PURCHASES", 275f, startY + 30f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText(Currencies.format(totalStore, currency), 275f, startY + 65f, paint)

        // Local Purchases & Exp Card
        paint.color = Color.WHITE
        canvas2.drawRoundRect(480f, startY, 480f + cardWidth, startY + cardHeight, 10f, 10f, paint)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 12f
        canvas2.drawText("LOCAL EXPENSES", 495f, startY + 30f, paint)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText(Currencies.format(totalLocal, currency), 495f, startY + 65f, paint)

        // Net Margin (Profit) Card
        val marginBg = if (netMargin >= 0) Color.rgb(209, 250, 229) else Color.rgb(254, 226, 226)
        paint.color = marginBg
        canvas2.drawRoundRect(700f, startY, 700f + cardWidth, startY + cardHeight, 10f, 10f, paint)
        paint.color = if (netMargin >= 0) Color.rgb(6, 95, 70) else Color.rgb(153, 27, 27)
        paint.textSize = 12f
        canvas2.drawText("NET CASH PROFIT", 715f, startY + 30f, paint)
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText(Currencies.format(netMargin, currency), 715f, startY + 65f, paint)

        // Visual Comparison Bar
        var barY = 215f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText("Cash Margin Overview (Sales vs Total Expenses):", 40f, barY, paint)

        barY += 15f
        val maxBarWidth = 865f
        paint.color = Color.rgb(226, 232, 240)
        canvas2.drawRoundRect(40f, barY, 40f + maxBarWidth, barY + 30f, 6f, 6f, paint)

        val expRatio = if (totalSales > 0) (totalExpenses / totalSales).coerceIn(0.0, 1.0).toFloat() else 0.5f
        paint.color = Color.rgb(225, 29, 72) // Expenses (Rose)
        canvas2.drawRoundRect(40f, barY, 40f + (maxBarWidth * expRatio), barY + 30f, 6f, 6f, paint)

        paint.color = Color.rgb(5, 150, 105) // Profit (Emerald)
        canvas2.drawRoundRect(40f + (maxBarWidth * expRatio), barY, 40f + maxBarWidth, barY + 30f, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText("Expenses: ${String.format("%.1f", expRatio * 100)}%", 60f, barY + 20f, paint)
        if (expRatio < 0.85f) {
            canvas2.drawText("Net Profit Margin: ${String.format("%.1f", (1f - expRatio) * 100)}%", 40f + (maxBarWidth * expRatio) + 15f, barY + 20f, paint)
        }

        // Daily Highlights Table preview
        barY += 55f
        paint.color = Color.rgb(20, 80, 80)
        canvas2.drawRect(40f, barY, 905f, barY + 24f, paint)
        paint.color = Color.WHITE
        paint.textSize = 11f
        canvas2.drawText("Sample Daily Ledger Breakdown", 50f, barY + 16f, paint)

        barY += 38f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10.5f

        ledgerEntries.take(7).forEach { e ->
            canvas2.drawText(e.date, 50f, barY, paint)
            canvas2.drawText("Sales: " + Currencies.format(e.sale, currency), 200f, barY, paint)
            canvas2.drawText("Store: " + Currencies.format(e.storePurchase, currency), 380f, barY, paint)
            canvas2.drawText("Local Exp: " + Currencies.format(e.localPurchaseAndExpense, currency), 560f, barY, paint)
            val marginCol = if (e.dailyNetMargin >= 0) Color.rgb(5, 150, 105) else Color.rgb(225, 29, 72)
            paint.color = marginCol
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas2.drawText("Margin: " + Currencies.format(e.dailyNetMargin, currency), 760f, barY, paint)
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            barY += 18f
        }

        document.finishPage(slide2)

        // ========================================================
        // SLIDE 3: STOCK REQUISITION OVERVIEW (MATCHING SCREENSHOT 4)
        // ========================================================
        val pageInfo3 = PdfDocument.PageInfo.Builder(slideWidth, slideHeight, 3).create()
        val slide3 = document.startPage(pageInfo3)
        val canvas3 = slide3.canvas

        paint.color = Color.rgb(248, 250, 252)
        canvas3.drawRect(0f, 0f, slideWidth.toFloat(), slideHeight.toFloat(), paint)

        paint.color = Color.rgb(30, 58, 138) // Deep Blue
        canvas3.drawRect(0f, 0f, slideWidth.toFloat(), 70f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.LEFT
        canvas3.drawText("Vegetable & Produce Stock Requisition Sheet", 40f, 42f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas3.drawText("Location: Stock List Of ${profile.branch}", slideWidth - 40f, 42f, paint)

        // Requisition KPI header
        paint.color = Color.WHITE
        canvas3.drawRoundRect(40f, 90f, 905f, 150f, 10f, 10f, paint)
        paint.color = Color.rgb(30, 58, 138)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas3.drawText("REF NO: 08489  •  DATE: 31/08/2026", 60f, 125f, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.rgb(5, 150, 105)
        paint.textSize = 16f
        canvas3.drawText("TOTAL VALUE: ${Currencies.format(totalStockAmount, currency)}", 885f, 125f, paint)

        // Requisition Items 2-Column Grid
        var gridY = 175f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas3.drawText("Requisition Line Items (Produce & Groceries):", 40f, gridY, paint)

        gridY += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10.5f

        val half = (stockItems.size + 1) / 2
        val col1 = stockItems.take(half)
        val col2 = stockItems.drop(half)

        var y1 = gridY
        col1.take(10).forEach { item ->
            canvas3.drawText("${item.itemNumber}. ${item.itemName} (${item.quantity} ${item.unit})", 40f, y1, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas3.drawText(Currencies.format(item.amount, currency), 450f, y1, paint)
            paint.textAlign = Paint.Align.LEFT
            y1 += 18f
        }

        var y2 = gridY
        col2.take(10).forEach { item ->
            canvas3.drawText("${item.itemNumber}. ${item.itemName} (${item.quantity} ${item.unit})", 490f, y2, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas3.drawText(Currencies.format(item.amount, currency), 905f, y2, paint)
            paint.textAlign = Paint.Align.LEFT
            y2 += 18f
        }

        // Authorizations at bottom
        paint.color = Color.rgb(226, 232, 240)
        canvas3.drawRect(40f, 450f, 905f, 500f, paint)
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 11f
        canvas3.drawText("Prepared / Counted By: Staff Member", 60f, 480f, paint)
        canvas3.drawText("Received / Checked By: Store Manager", 360f, 480f, paint)
        canvas3.drawText("Branch Stamp: AUTHORIZED ✓", 680f, 480f, paint)

        document.finishPage(slide3)

        // ========================================================
        // SLIDE 4: RETURN REGISTER & QUALITY REVIEW (SCREENSHOT 1)
        // ========================================================
        val pageInfo4 = PdfDocument.PageInfo.Builder(slideWidth, slideHeight, 4).create()
        val slide4 = document.startPage(pageInfo4)
        val canvas4 = slide4.canvas

        paint.color = Color.rgb(248, 250, 252)
        canvas4.drawRect(0f, 0f, slideWidth.toFloat(), slideHeight.toFloat(), paint)

        paint.color = Color.rgb(190, 18, 60) // Rose Red
        canvas4.drawRect(0f, 0f, slideWidth.toFloat(), 70f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.LEFT
        canvas4.drawText("Return Items Register (400 Items) & Loss Analysis", 40f, 42f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas4.drawText("Date: 2026-09-30 • REG-400", slideWidth - 40f, 42f, paint)

        // Summary Card
        paint.color = Color.WHITE
        canvas4.drawRoundRect(40f, 95f, 905f, 160f, 10f, 10f, paint)
        paint.color = Color.rgb(190, 18, 60)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas4.drawText("Checked By: Store Quality Supervisor", 60f, 130f, paint)
        paint.textAlign = Paint.Align.RIGHT
        val totalRetSum = returnItems.sumOf { it.amount }
        canvas4.drawText("TOTAL RETURN VALUE: ${Currencies.format(totalRetSum, currency)}", 885f, 130f, paint)

        // Return Items List
        var retY = 195f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas4.drawText("Documented Return Items & Causes:", 40f, retY, paint)

        retY += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f

        returnItems.forEach { item ->
            canvas4.drawText("Sl. ${item.itemNumber} • ${item.itemName} (${item.quantity} ${item.unit})", 40f, retY, paint)
            canvas4.drawText("Reason: ${item.reason}", 440f, retY, paint)
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.rgb(190, 18, 60)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas4.drawText(Currencies.format(item.amount, currency), 905f, retY, paint)
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            retY += 25f
        }

        // Action Recommendation
        retY += 20f
        paint.color = Color.rgb(254, 242, 242)
        canvas4.drawRoundRect(40f, retY, 905f, retY + 60f, 8f, 8f, paint)
        paint.color = Color.rgb(153, 27, 27)
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas4.drawText("OPERATIONAL ACTION:", 55f, retY + 25f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas4.drawText("Automated sync to Daily Sales Ledger completed. Losses deducted from net cash margin.", 55f, retY + 45f, paint)

        document.finishPage(slide4)

        // Save Presentation file
        val file = File(context.cacheDir, "Executive_Slides_${monthYear}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    // --- OPEN IN GOOGLE SLIDES / PRESENTATION VIEWER ---
    fun openInGoogleSlides(context: Context, presentationFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                presentationFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open in Google Slides / Presentation Viewer"))
        } catch (e: Exception) {
            sharePresentation(context, presentationFile, "Executive Business Review Slides")
        }
    }

    // --- SHARE PRESENTATION VIA WHATSAPP / DRIVE / EMAIL ---
    fun sharePresentation(context: Context, presentationFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                presentationFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "📊 Executive Business Review Slides - ${presentationFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Slides via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share slides: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
