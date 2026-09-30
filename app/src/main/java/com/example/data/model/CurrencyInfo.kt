package com.example.data.model

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val decimals: Int = 2,
    val flag: String = ""
)

object Currencies {
    val ALL = listOf(
        CurrencyInfo(code = "OMR", name = "Omani Rial", symbol = "OMR", decimals = 3, flag = "🇴🇲"),
        CurrencyInfo(code = "SAR", name = "Saudi Riyal", symbol = "SAR", decimals = 2, flag = "🇸🇦"),
        CurrencyInfo(code = "AED", name = "UAE Dirham", symbol = "AED", decimals = 2, flag = "🇦🇪"),
        CurrencyInfo(code = "KWD", name = "Kuwaiti Dinar", symbol = "KWD", decimals = 3, flag = "🇰🇼"),
        CurrencyInfo(code = "BHD", name = "Bahraini Dinar", symbol = "BHD", decimals = 3, flag = "🇧🇭"),
        CurrencyInfo(code = "QAR", name = "Qatari Riyal", symbol = "QAR", decimals = 2, flag = "🇶🇦"),
        CurrencyInfo(code = "BDT", name = "Bangladeshi Taka", symbol = "৳", decimals = 2, flag = "🇧🇩"),
        CurrencyInfo(code = "INR", name = "Indian Rupee", symbol = "₹", decimals = 2, flag = "🇮🇳"),
        CurrencyInfo(code = "PKR", name = "Pakistani Rupee", symbol = "₨", decimals = 2, flag = "🇵🇰"),
        CurrencyInfo(code = "USD", name = "US Dollar", symbol = "$", decimals = 2, flag = "🇺🇸"),
        CurrencyInfo(code = "EUR", name = "Euro", symbol = "€", decimals = 2, flag = "🇪🇺"),
        CurrencyInfo(code = "GBP", name = "British Pound", symbol = "£", decimals = 2, flag = "🇬🇧"),
        CurrencyInfo(code = "MYR", name = "Malaysian Ringgit", symbol = "RM", decimals = 2, flag = "🇲🇾"),
        CurrencyInfo(code = "SGD", name = "Singapore Dollar", symbol = "S$", decimals = 2, flag = "🇸🇬"),
        CurrencyInfo(code = "CAD", name = "Canadian Dollar", symbol = "CA$", decimals = 2, flag = "🇨🇦"),
        CurrencyInfo(code = "AUD", name = "Australian Dollar", symbol = "AU$", decimals = 2, flag = "🇦🇺"),
        CurrencyInfo(code = "TRY", name = "Turkish Lira", symbol = "₺", decimals = 2, flag = "🇹🇷"),
        CurrencyInfo(code = "EGP", name = "Egyptian Pound", symbol = "E£", decimals = 2, flag = "🇪🇬"),
        CurrencyInfo(code = "JPY", name = "Japanese Yen", symbol = "¥", decimals = 0, flag = "🇯🇵"),
        CurrencyInfo(code = "CNY", name = "Chinese Yuan", symbol = "¥", decimals = 2, flag = "🇨🇳")
    )

    fun find(code: String): CurrencyInfo {
        return ALL.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: CurrencyInfo(code = code, name = code, symbol = code, decimals = 2, flag = "🌐")
    }

    fun format(amount: Double, currency: CurrencyInfo): String {
        val pattern = when (currency.decimals) {
            0 -> "%,.0f"
            3 -> "%,.3f"
            else -> "%,.2f"
        }
        val formattedNumber = String.format(java.util.Locale.US, pattern, amount)
        return if (currency.symbol.length <= 2) {
            "${currency.symbol} $formattedNumber"
        } else {
            "$formattedNumber ${currency.symbol}"
        }
    }
}
