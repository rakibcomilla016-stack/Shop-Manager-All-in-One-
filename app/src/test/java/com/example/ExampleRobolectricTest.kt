package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.i18n.Strings
import com.example.util.ExcelExportManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Shop Manager All in One", appName)
    }

    @Test
    fun `test currency formatting with OMR 3 decimals`() {
        val omr = Currencies.find("OMR")
        assertEquals(3, omr.decimals)
        val formatted = Currencies.format(12883.95, omr)
        assertTrue(formatted.contains("12,883.950") || formatted.contains("OMR"))
    }

    @Test
    fun `test multi-language strings and RTL support`() {
        val titleEn = Strings.get("app_title", "en")
        val titleAr = Strings.get("app_title", "ar")
        val titleBn = Strings.get("app_title", "bn")

        assertEquals("Shop Manager All in One", titleEn)
        assertEquals("مدير المتجر الشامل", titleAr)
        assertEquals("শপ ম্যানেজার অল ইন ওয়ান", titleBn)

        assertTrue(Strings.isRtl("ar"))
        assertTrue(Strings.isRtl("ur"))
        assertTrue(!Strings.isRtl("en"))
        assertTrue(!Strings.isRtl("bn"))
    }

    @Test
    fun `test Excel format file generation for Monthly Ledger`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = ShopProfile(shopName = "Test Shop", currencyCode = "OMR")
        val entries = listOf(
            DailyLedgerEntry(date = "2026-09-01", monthYear = "2026-09", sale = 257.0, storePurchase = 0.0, localPurchaseAndExpense = 72.2, returns = 0.0)
        )
        val excelFile = ExcelExportManager.exportLedgerToExcel(context, profile, "2026-09", entries, Currencies.find("OMR"))
        assertTrue(excelFile.exists())
        assertTrue(excelFile.length() > 0)
        val content = excelFile.readText()
        assertTrue(content.contains("Monthly Sales and Expense Ledger"))
        assertTrue(content.contains("2026-09-01"))
        assertTrue(content.contains("257.000"))
    }
}
