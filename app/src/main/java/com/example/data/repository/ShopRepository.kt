package com.example.data.repository

import com.example.data.local.ShopDao
import com.example.data.model.DailyLedgerEntry
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.ReturnItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockItem
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val dao: ShopDao) {

    // Profile
    val profile: Flow<ShopProfile?> = dao.getProfileFlow()

    suspend fun getProfileSync(): ShopProfile? = dao.getProfile()

    suspend fun saveProfile(profile: ShopProfile) {
        dao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: ShopProfile) {
        dao.updateProfile(profile)
    }

    // Invoices
    val allInvoices: Flow<List<Invoice>> = dao.getAllInvoices()

    suspend fun getInvoiceById(id: Long): Invoice? = dao.getInvoiceById(id)

    fun getInvoiceItems(invoiceId: Long): Flow<List<InvoiceItem>> = dao.getInvoiceItems(invoiceId)

    suspend fun getInvoiceItemsSync(invoiceId: Long): List<InvoiceItem> = dao.getInvoiceItemsSync(invoiceId)

    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        val invoiceId = dao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        dao.insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    suspend fun updateInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        dao.updateInvoice(invoice)
        dao.deleteInvoiceItemsByInvoiceId(invoice.id)
        val itemsWithId = items.map { it.copy(invoiceId = invoice.id) }
        dao.insertInvoiceItems(itemsWithId)
    }

    suspend fun deleteInvoice(id: Long) {
        dao.deleteInvoiceItemsByInvoiceId(id)
        dao.deleteInvoice(id)
    }

    // Daily Ledger
    fun getLedgerForMonth(monthYear: String): Flow<List<DailyLedgerEntry>> =
        dao.getLedgerForMonth(monthYear)

    val allLedgers: Flow<List<DailyLedgerEntry>> = dao.getAllLedger()

    suspend fun getLedgerByDate(date: String): DailyLedgerEntry? = dao.getLedgerByDate(date)

    suspend fun saveLedger(entry: DailyLedgerEntry) {
        dao.insertLedger(entry)
    }

    suspend fun deleteLedger(id: Long) {
        dao.deleteLedger(id)
    }

    // Stock Items
    val allStockItems: Flow<List<StockItem>> = dao.getAllStockItems()

    fun getStockItemsByCategory(category: String): Flow<List<StockItem>> =
        dao.getStockItemsByCategory(category)

    suspend fun saveStockItem(item: StockItem) {
        dao.insertStockItem(item)
    }

    suspend fun updateStockItem(item: StockItem) {
        dao.updateStockItem(item)
    }

    suspend fun deleteStockItem(id: Long) {
        dao.deleteStockItem(id)
    }

    // Return Items
    val allReturnItems: Flow<List<ReturnItem>> = dao.getAllReturnItems()

    suspend fun saveReturnItem(item: ReturnItem) {
        dao.insertReturnItem(item)
    }

    suspend fun updateReturnItem(item: ReturnItem) {
        dao.updateReturnItem(item)
    }

    suspend fun deleteReturnItem(id: Long) {
        dao.deleteReturnItem(id)
    }
}
