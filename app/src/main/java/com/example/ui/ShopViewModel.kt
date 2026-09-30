package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Currencies
import com.example.data.model.CurrencyInfo
import com.example.data.model.DailyLedgerEntry
import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.ReturnItem
import com.example.data.model.ShopProfile
import com.example.data.model.StockItem
import com.example.data.repository.ShopRepository
import com.example.i18n.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository
    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ShopRepository(database.shopDao())
    }

    // Active Profile
    val profile: StateFlow<ShopProfile?> = repository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Current Language and Currency
    private val _currentLang = MutableStateFlow("en")
    val currentLang: StateFlow<String> = _currentLang.asStateFlow()

    private val _currentCurrency = MutableStateFlow(Currencies.find("OMR"))
    val currentCurrency: StateFlow<CurrencyInfo> = _currentCurrency.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profile.collect { p ->
                if (p != null) {
                    _currentLang.value = p.languageCode
                    _currentCurrency.value = Currencies.find(p.currencyCode)
                }
            }
        }
    }

    // Ledger selected month
    private val _selectedMonth = MutableStateFlow("2026-09")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    val monthlyLedger: StateFlow<List<DailyLedgerEntry>> = combine(
        repository.allLedgers,
        _selectedMonth
    ) { ledgers, month ->
        ledgers.filter { it.monthYear == month }.sortedBy { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Invoices
    val invoices: StateFlow<List<Invoice>> = repository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Stock Items
    private val _stockCategoryFilter = MutableStateFlow("All")
    val stockCategoryFilter: StateFlow<String> = _stockCategoryFilter.asStateFlow()

    fun setStockCategoryFilter(category: String) {
        _stockCategoryFilter.value = category
    }

    val stockItems: StateFlow<List<StockItem>> = combine(
        repository.allStockItems,
        _stockCategoryFilter
    ) { items, filter ->
        if (filter == "All") items else items.filter { it.category == filter }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Return Items
    val returnItems: StateFlow<List<ReturnItem>> = repository.allReturnItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Actions
    fun setLanguage(langCode: String) {
        _currentLang.value = langCode
        val currentP = profile.value
        if (currentP != null) {
            viewModelScope.launch {
                repository.updateProfile(currentP.copy(languageCode = langCode))
            }
        }
    }

    fun setCurrency(currencyCode: String) {
        val curr = Currencies.find(currencyCode)
        _currentCurrency.value = curr
        val currentP = profile.value
        if (currentP != null) {
            viewModelScope.launch {
                repository.updateProfile(currentP.copy(currencyCode = currencyCode))
            }
        }
    }

    fun tr(key: String): String {
        return Strings.get(key, _currentLang.value)
    }

    fun formatMoney(amount: Double): String {
        return Currencies.format(amount, _currentCurrency.value)
    }

    fun loginOrSignUp(
        type: String, // GMAIL, PHONE, WHATSAPP
        identifier: String,
        shopName: String,
        shopNameArabic: String,
        crNumber: String,
        vatin: String,
        branch: String,
        phone: String,
        currencyCode: String,
        languageCode: String
    ) {
        viewModelScope.launch {
            val updatedProfile = ShopProfile(
                id = 1,
                shopName = shopName.ifBlank { "Sholat Al Mujad Al Shamilah Trad." },
                shopNameArabic = shopNameArabic.ifBlank { "شركة شعلة المجد الشاملة للتجارة" },
                crNumber = crNumber.ifBlank { "1208612" },
                vatin = vatin.ifBlank { "OM1208612000" },
                country = "Sultanate of Oman",
                branch = branch.ifBlank { "Main Branch" },
                phone = phone.ifBlank { identifier },
                whatsapp = if (type == "WHATSAPP") identifier else phone,
                email = if (type == "GMAIL") identifier else "rakib.comilla016@gmail.com",
                currencyCode = currencyCode,
                languageCode = languageCode,
                loginType = type,
                loginIdentifier = identifier,
                isLoggedIn = true
            )
            repository.saveProfile(updatedProfile)
            _currentLang.value = languageCode
            _currentCurrency.value = Currencies.find(currencyCode)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val p = profile.value
            if (p != null) {
                repository.updateProfile(p.copy(isLoggedIn = false))
            }
        }
    }

    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            if (invoice.id == 0L) {
                repository.saveInvoice(invoice, items)
            } else {
                repository.updateInvoice(invoice, items)
            }
            onSaved()
        }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
        }
    }

    fun saveDailyLedger(entry: DailyLedgerEntry, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveLedger(entry)
            onSaved()
        }
    }

    fun deleteDailyLedger(id: Long) {
        viewModelScope.launch {
            repository.deleteLedger(id)
        }
    }

    fun saveStockItem(item: StockItem, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.saveStockItem(item)
            } else {
                repository.updateStockItem(item)
            }
            onSaved()
        }
    }

    fun deleteStockItem(id: Long) {
        viewModelScope.launch {
            repository.deleteStockItem(id)
        }
    }

    fun saveReturnItem(item: ReturnItem, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.saveReturnItem(item)
            } else {
                repository.updateReturnItem(item)
            }
            onSaved()
        }
    }

    fun deleteReturnItem(id: Long) {
        viewModelScope.launch {
            repository.deleteReturnItem(id)
        }
    }

    fun syncReturnToLedger(returnItem: ReturnItem, onSynced: (String) -> Unit) {
        viewModelScope.launch {
            val date = returnItem.date
            val monthYear = if (date.length >= 7) date.substring(0, 7) else "2026-09"
            val existing = repository.getLedgerByDate(date)
            if (existing != null) {
                val updated = existing.copy(returns = existing.returns + returnItem.amount)
                repository.saveLedger(updated)
            } else {
                val newEntry = DailyLedgerEntry(
                    date = date,
                    monthYear = monthYear,
                    sale = 0.0,
                    storePurchase = 0.0,
                    localPurchaseAndExpense = 0.0,
                    returns = returnItem.amount,
                    notes = "Synced from Return: ${returnItem.itemName}"
                )
                repository.saveLedger(newEntry)
            }
            onSynced("Added ${returnItem.amount} to $date returns")
        }
    }

    suspend fun getInvoiceItems(invoiceId: Long): List<InvoiceItem> {
        return repository.getInvoiceItemsSync(invoiceId)
    }
}
