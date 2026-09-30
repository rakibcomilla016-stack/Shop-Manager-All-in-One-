package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyLedgerEntry
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.ReturnItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    // Shop Profile
    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ShopProfile?>

    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ShopProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ShopProfile)

    @Update
    suspend fun updateProfile(profile: ShopProfile)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    // Invoice Items
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY itemNumber ASC")
    fun getInvoiceItems(invoiceId: Long): Flow<List<InvoiceItem>>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY itemNumber ASC")
    suspend fun getInvoiceItemsSync(invoiceId: Long): List<InvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItemsByInvoiceId(invoiceId: Long)

    // Daily Ledger
    @Query("SELECT * FROM daily_ledger WHERE monthYear = :monthYear ORDER BY date ASC")
    fun getLedgerForMonth(monthYear: String): Flow<List<DailyLedgerEntry>>

    @Query("SELECT * FROM daily_ledger ORDER BY date DESC")
    fun getAllLedger(): Flow<List<DailyLedgerEntry>>

    @Query("SELECT * FROM daily_ledger WHERE date = :date LIMIT 1")
    suspend fun getLedgerByDate(date: String): DailyLedgerEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(entry: DailyLedgerEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgers(entries: List<DailyLedgerEntry>)

    @Update
    suspend fun updateLedger(entry: DailyLedgerEntry)

    @Query("DELETE FROM daily_ledger WHERE id = :id")
    suspend fun deleteLedger(id: Long)

    // Stock Items
    @Query("SELECT * FROM stock_items ORDER BY itemNumber ASC")
    fun getAllStockItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE category = :category ORDER BY itemNumber ASC")
    fun getStockItemsByCategory(category: String): Flow<List<StockItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItems(items: List<StockItem>)

    @Update
    suspend fun updateStockItem(item: StockItem)

    @Query("DELETE FROM stock_items WHERE id = :id")
    suspend fun deleteStockItem(id: Long)

    // Return Items
    @Query("SELECT * FROM return_items ORDER BY itemNumber ASC")
    fun getAllReturnItems(): Flow<List<ReturnItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnItem(item: ReturnItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnItems(items: List<ReturnItem>)

    @Update
    suspend fun updateReturnItem(item: ReturnItem)

    @Query("DELETE FROM return_items WHERE id = :id")
    suspend fun deleteReturnItem(id: Long)
}
