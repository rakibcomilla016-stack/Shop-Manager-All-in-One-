package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyLedgerEntry
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.ReturnItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ShopProfile::class,
        Invoice::class,
        InvoiceItem::class,
        DailyLedgerEntry::class,
        StockItem::class,
        ReturnItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shop_manager_all_in_one.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.shopDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ShopDao) {
            // 1. Initial Shop Profile
            dao.insertProfile(
                ShopProfile(
                    id = 1,
                    shopName = "Sholat Al Mujad Al Shamilah Trad.",
                    shopNameArabic = "شركة شعلة المجد الشاملة للتجارة",
                    crNumber = "1208612",
                    vatin = "OM1208612000",
                    country = "Sultanate of Oman",
                    branch = "Main Branch",
                    phone = "+968 92152565",
                    whatsapp = "+968 92152565",
                    email = "rakib.comilla016@gmail.com",
                    currencyCode = "OMR",
                    languageCode = "en",
                    loginType = "GMAIL",
                    loginIdentifier = "rakib.comilla016@gmail.com",
                    isLoggedIn = true
                )
            )

            // 2. Pre-seed Invoices
            val invId1 = dao.insertInvoice(
                Invoice(
                    invoiceNo = "INV-2026-001",
                    date = "2026-09-30",
                    branch = "Main Branch",
                    customerName = "Al-Baraka Supermarket",
                    customerPhone = "+968 98765432",
                    status = "PAID",
                    subtotal = 345.500,
                    vatRate = 5.0,
                    vatAmount = 17.275,
                    discount = 12.775,
                    totalAmount = 350.000,
                    notes = "Delivered to central depot."
                )
            )
            dao.insertInvoiceItems(
                listOf(
                    InvoiceItem(invoiceId = invId1, itemNumber = 1, itemName = "Royal Gala Apple", quantity = 15.0, unit = "Ctn", unitPrice = 7.000, vatRate = 5.0, totalValue = 105.000),
                    InvoiceItem(invoiceId = invId1, itemNumber = 2, itemName = "Fresh Potato 25kg", quantity = 20.0, unit = "Bag", unitPrice = 4.200, vatRate = 5.0, totalValue = 84.000),
                    InvoiceItem(invoiceId = invId1, itemNumber = 3, itemName = "Oman Banana 7kg", quantity = 25.0, unit = "Ctn", unitPrice = 3.800, vatRate = 5.0, totalValue = 95.000),
                    InvoiceItem(invoiceId = invId1, itemNumber = 4, itemName = "Oasis Water 500ml", quantity = 35.0, unit = "Pkt", unitPrice = 1.750, vatRate = 5.0, totalValue = 61.250)
                )
            )

            val invId2 = dao.insertInvoice(
                Invoice(
                    invoiceNo = "INV-2026-002",
                    date = "2026-09-29",
                    branch = "Bu-Hassan Shop",
                    customerName = "Nasser Fresh Produce",
                    customerPhone = "+968 91234567",
                    status = "DUE",
                    subtotal = 188.000,
                    vatRate = 5.0,
                    vatAmount = 9.400,
                    discount = 5.000,
                    totalAmount = 192.400,
                    notes = "Payment due by Oct 5"
                )
            )
            dao.insertInvoiceItems(
                listOf(
                    InvoiceItem(invoiceId = invId2, itemNumber = 1, itemName = "Anar Yemen Pomegranate", quantity = 10.0, unit = "Kg", unitPrice = 1.200, vatRate = 5.0, totalValue = 12.000),
                    InvoiceItem(invoiceId = invId2, itemNumber = 2, itemName = "Dry Lemon Oman", quantity = 15.0, unit = "Kg", unitPrice = 1.500, vatRate = 5.0, totalValue = 22.500),
                    InvoiceItem(invoiceId = invId2, itemNumber = 3, itemName = "Dates Sagai 3kg", quantity = 18.0, unit = "Ctn", unitPrice = 4.800, vatRate = 5.0, totalValue = 86.400),
                    InvoiceItem(invoiceId = invId2, itemNumber = 4, itemName = "Tomato Local Box", quantity = 15.0, unit = "Box", unitPrice = 4.500, vatRate = 5.0, totalValue = 67.500)
                )
            )

            // 3. Pre-seed September 2026 Sales and Expense Ledger (exact matching user screenshot!)
            val ledgerData = listOf(
                DailyLedgerEntry(date = "2026-09-01", monthYear = "2026-09", sale = 257.000, storePurchase = 0.0, localPurchaseAndExpense = 72.200, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-02", monthYear = "2026-09", sale = 391.600, storePurchase = 782.490, localPurchaseAndExpense = 111.600, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-03", monthYear = "2026-09", sale = 435.900, storePurchase = 0.0, localPurchaseAndExpense = 103.900, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-04", monthYear = "2026-09", sale = 405.100, storePurchase = 603.600, localPurchaseAndExpense = 75.200, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-05", monthYear = "2026-09", sale = 817.800, storePurchase = 235.800, localPurchaseAndExpense = 163.600, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-06", monthYear = "2026-09", sale = 472.500, storePurchase = 550.990, localPurchaseAndExpense = 118.900, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-07", monthYear = "2026-09", sale = 367.800, storePurchase = 0.0, localPurchaseAndExpense = 125.900, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-08", monthYear = "2026-09", sale = 413.500, storePurchase = 584.450, localPurchaseAndExpense = 97.900, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-09", monthYear = "2026-09", sale = 346.800, storePurchase = 0.0, localPurchaseAndExpense = 65.900, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-10", monthYear = "2026-09", sale = 459.100, storePurchase = 632.750, localPurchaseAndExpense = 62.500, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-11", monthYear = "2026-09", sale = 525.900, storePurchase = 164.850, localPurchaseAndExpense = 186.100, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-12", monthYear = "2026-09", sale = 510.700, storePurchase = 0.0, localPurchaseAndExpense = 187.400, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-13", monthYear = "2026-09", sale = 331.400, storePurchase = 0.0, localPurchaseAndExpense = 134.800, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-14", monthYear = "2026-09", sale = 341.900, storePurchase = 0.0, localPurchaseAndExpense = 71.100, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-15", monthYear = "2026-09", sale = 367.600, storePurchase = 0.0, localPurchaseAndExpense = 101.600, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-16", monthYear = "2026-09", sale = 464.400, storePurchase = 0.0, localPurchaseAndExpense = 129.400, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-17", monthYear = "2026-09", sale = 523.000, storePurchase = 0.0, localPurchaseAndExpense = 146.300, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-18", monthYear = "2026-09", sale = 578.100, storePurchase = 0.0, localPurchaseAndExpense = 145.500, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-19", monthYear = "2026-09", sale = 546.500, storePurchase = 0.0, localPurchaseAndExpense = 202.400, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-20", monthYear = "2026-09", sale = 393.950, storePurchase = 0.0, localPurchaseAndExpense = 115.100, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-21", monthYear = "2026-09", sale = 358.400, storePurchase = 0.0, localPurchaseAndExpense = 154.200, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-22", monthYear = "2026-09", sale = 460.800, storePurchase = 0.0, localPurchaseAndExpense = 131.600, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-23", monthYear = "2026-09", sale = 653.700, storePurchase = 0.0, localPurchaseAndExpense = 81.300, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-24", monthYear = "2026-09", sale = 300.600, storePurchase = 0.0, localPurchaseAndExpense = 127.400, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-25", monthYear = "2026-09", sale = 662.800, storePurchase = 0.0, localPurchaseAndExpense = 118.600, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-26", monthYear = "2026-09", sale = 693.300, storePurchase = 0.0, localPurchaseAndExpense = 168.300, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-27", monthYear = "2026-09", sale = 433.600, storePurchase = 0.0, localPurchaseAndExpense = 87.700, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-28", monthYear = "2026-09", sale = 370.200, storePurchase = 0.0, localPurchaseAndExpense = 109.800, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-29", monthYear = "2026-09", sale = 425.000, storePurchase = 120.000, localPurchaseAndExpense = 95.000, returns = 0.0),
                DailyLedgerEntry(date = "2026-09-30", monthYear = "2026-09", sale = 512.400, storePurchase = 0.0, localPurchaseAndExpense = 112.500, returns = 15.000)
            )
            dao.insertLedgers(ledgerData)

            // 4. Pre-seed Stock Items (from Vegetable Requisition Sheet)
            val stockData = listOf(
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 1, itemName = "AFNADI SMALL", quantity = 12.0, unit = "Kg", rate = 0.850, amount = 10.200, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 2, itemName = "ANAR - YEMEN", quantity = 18.0, unit = "Kg", rate = 1.200, amount = 21.600, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 4, itemName = "APPLE GREEN CHINA", quantity = 8.0, unit = "Ctn", rate = 6.500, amount = 52.000, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 6, itemName = "APPLE ROYAL GALA", quantity = 7.5, unit = "Ctn", rate = 7.000, amount = 52.500, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 8, itemName = "AVOCADO", quantity = 3.5, unit = "Kg", rate = 1.800, amount = 6.300, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 10, itemName = "BANANA PS", quantity = 1.5, unit = "Ctn", rate = 4.200, amount = 6.300, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 12, itemName = "BANANA 7 KG IND", quantity = 27.0, unit = "Ctn", rate = 3.800, amount = 102.600, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 16, itemName = "GRAPES BLACK", quantity = 7.0, unit = "Ctn", rate = 1.400, amount = 9.800, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 76, itemName = "POTATO 25 KG", quantity = 20.0, unit = "Bag", rate = 4.200, amount = 84.000, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Vegetable Requisition", itemNumber = 106, itemName = "ONION RED BAG", quantity = 39.0, unit = "Bag", rate = 1.300, amount = 50.700, preparedBy = "Staff", receivedBy = "Store Keeper"),
                // Additional Category
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Dates, Water & Groceries", itemNumber = 172, itemName = "Dates Syrup 1kg", quantity = 24.0, unit = "Btl", rate = 0.950, amount = 22.800, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Dates, Water & Groceries", itemNumber = 179, itemName = "Dates Sagai 3kg Ctn", quantity = 15.0, unit = "Ctn", rate = 4.800, amount = 72.000, preparedBy = "Staff", receivedBy = "Store Keeper"),
                StockItem(refNo = "08489", date = "2026-08-31", location = "Bu-Hassan Shop", category = "Dates, Water & Groceries", itemNumber = 190, itemName = "Oasis Water 500ML X 24P", quantity = 40.0, unit = "Ctn", rate = 1.250, amount = 50.000, preparedBy = "Staff", receivedBy = "Store Keeper")
            )
            dao.insertStockItems(stockData)

            // 5. Pre-seed Return Items (matching Return Items Register)
            val returnData = listOf(
                ReturnItem(registerNo = "REG-400", date = "2026-09-30", checkedBy = "Supervisor Tariq", itemNumber = 1, itemName = "Damaged Onion Bag 20kg", quantity = 2.0, unit = "Bag", rate = 1.300, amount = 2.600, reason = "Spotted rot during unbagging"),
                ReturnItem(registerNo = "REG-400", date = "2026-09-30", checkedBy = "Supervisor Tariq", itemNumber = 2, itemName = "Overripe Bananas 7kg", quantity = 3.0, unit = "Ctn", rate = 3.800, amount = 11.400, reason = "Excessive softening"),
                ReturnItem(registerNo = "REG-400", date = "2026-09-29", checkedBy = "Supervisor Tariq", itemNumber = 3, itemName = "Cracked Tomato Box", quantity = 1.0, unit = "Box", rate = 4.500, amount = 4.500, reason = "Transport crush"),
                ReturnItem(registerNo = "REG-400", date = "2026-09-28", checkedBy = "Supervisor Tariq", itemNumber = 4, itemName = "Leaking Oasis Water Ctn", quantity = 2.0, unit = "Ctn", rate = 1.250, amount = 2.500, reason = "Seal breach in transit")
            )
            dao.insertReturnItems(returnData)
        }
    }
}
