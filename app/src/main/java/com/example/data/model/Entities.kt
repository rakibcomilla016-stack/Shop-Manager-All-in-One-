package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "Sholat Al Mujad Al Shamilah Trad.",
    val shopNameArabic: String = "شركة شعلة المجد الشاملة للتجارة",
    val crNumber: String = "1208612",
    val vatin: String = "OM1208612000",
    val country: String = "Sultanate of Oman",
    val branch: String = "Main Branch",
    val phone: String = "+968 92152565",
    val whatsapp: String = "+968 92152565",
    val email: String = "rakib.comilla016@gmail.com",
    val currencyCode: String = "OMR",
    val languageCode: String = "en",
    val loginType: String = "GMAIL", // "GMAIL", "PHONE", "WHATSAPP"
    val loginIdentifier: String = "rakib.comilla016@gmail.com",
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val date: String,
    val branch: String = "Main Branch",
    val customerName: String,
    val customerPhone: String = "",
    val status: String = "PAID", // "PAID", "DUE", "DRAFT"
    val subtotal: Double = 0.0,
    val vatRate: Double = 5.0,
    val vatAmount: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long = 0,
    val itemNumber: Int = 1,
    val itemName: String,
    val quantity: Double,
    val unit: String = "Pcs",
    val unitPrice: Double,
    val vatRate: Double = 5.0,
    val totalValue: Double = 0.0
)

@Entity(tableName = "daily_ledger")
data class DailyLedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val monthYear: String, // YYYY-MM
    val sale: Double = 0.0,
    val storePurchase: Double = 0.0,
    val localPurchaseAndExpense: Double = 0.0,
    val returns: Double = 0.0,
    val notes: String = ""
) {
    val totalDailyExpenses: Double
        get() = storePurchase + localPurchaseAndExpense + returns

    val dailyNetMargin: Double
        get() = sale - totalDailyExpenses
}

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val refNo: String = "08489",
    val date: String = "2026-08-31",
    val location: String = "Bu-Hassan Shop",
    val category: String = "Vegetable Requisition", // or "Dates, Water & Groceries", "General"
    val itemNumber: Int = 1,
    val itemName: String,
    val quantity: Double,
    val unit: String = "Kg",
    val rate: Double,
    val amount: Double = quantity * rate,
    val preparedBy: String = "Staff Member",
    val receivedBy: String = "Store Manager"
)

@Entity(tableName = "return_items")
data class ReturnItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val registerNo: String = "REG-400",
    val date: String = "2026-09-30",
    val checkedBy: String = "Supervisor",
    val itemNumber: Int = 1,
    val itemName: String,
    val quantity: Double,
    val unit: String = "Kg",
    val rate: Double,
    val amount: Double = quantity * rate,
    val reason: String = "Customer Return / Quality Check"
)
