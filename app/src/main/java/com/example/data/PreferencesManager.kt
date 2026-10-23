package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AdminUser
import com.example.model.AppSession
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.NotificationItem
import com.example.model.Payment
import com.example.model.SystemSettings
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("amar_bank_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ADMIN = "key_admin"
        private const val KEY_CUSTOMERS = "key_customers"
        private const val KEY_LOANS = "key_loans"
        private const val KEY_PAYMENTS = "key_payments"
        private const val KEY_NOTIFICATIONS = "key_notifications"
        private const val KEY_SETTINGS = "key_settings"
        private const val KEY_SESSION_ROLE = "key_session_role"
        private const val KEY_SESSION_ADMIN_ID = "key_session_admin_id"
        private const val KEY_SESSION_CUST_ID = "key_session_cust_id"
    }

    // --- SESSION AUTO-LOGIN ---
    fun saveSession(role: String?, adminId: String? = null, customerId: String? = null) {
        prefs.edit().apply {
            putString(KEY_SESSION_ROLE, role)
            putString(KEY_SESSION_ADMIN_ID, adminId)
            putString(KEY_SESSION_CUST_ID, customerId)
            apply()
        }
    }

    fun getSession(): AppSession {
        val role = prefs.getString(KEY_SESSION_ROLE, null)
        val adminId = prefs.getString(KEY_SESSION_ADMIN_ID, null)
        val customerId = prefs.getString(KEY_SESSION_CUST_ID, null)
        return AppSession(role, adminId, customerId)
    }

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_SESSION_ROLE)
            remove(KEY_SESSION_ADMIN_ID)
            remove(KEY_SESSION_CUST_ID)
            apply()
        }
    }

    // --- SYSTEM SETTINGS ---
    fun saveSettings(settings: SystemSettings) {
        val json = JSONObject().apply {
            put("cityName", settings.cityName)
            put("branchName", settings.branchName)
            put("divisionName", settings.divisionName)
            put("branchAddress", settings.branchAddress)
            put("branchPhone", settings.branchPhone)
            put("officialWhatsApp", settings.officialWhatsApp)
            put("responsibleName", settings.responsibleName)
            put("responsibleTitle", settings.responsibleTitle)
        }
        prefs.edit().putString(KEY_SETTINGS, json.toString()).apply()
    }

    fun getSettings(): SystemSettings {
        val str = prefs.getString(KEY_SETTINGS, null) ?: return InitialData.defaultSystemSettings
        return try {
            val obj = JSONObject(str)
            SystemSettings(
                cityName = obj.optString("cityName", "Surabaya"),
                branchName = obj.optString("branchName", "PT BANK AMAR INDONESIA Tbk"),
                divisionName = obj.optString("divisionName", "Divisi Manajemen Piutang & Pembiayaan Ritel"),
                branchAddress = obj.optString("branchAddress", "Graha Amar, Jl. Basuki Rahmat 122"),
                branchPhone = obj.optString("branchPhone", "(031) 567-8910"),
                officialWhatsApp = obj.optString("officialWhatsApp", "0812-9876-5432"),
                responsibleName = obj.optString("responsibleName", "Bambang Prasetyo, S.E."),
                responsibleTitle = obj.optString("responsibleTitle", "Head of Retail & Credit Admin")
            )
        } catch (e: Exception) {
            InitialData.defaultSystemSettings
        }
    }

    // --- ADMIN USER ---
    fun saveAdmin(admin: AdminUser) {
        val json = JSONObject().apply {
            put("id", admin.id)
            put("fullName", admin.fullName)
            put("email", admin.email)
            put("phone", admin.phone)
            put("pin", admin.pin)
            put("createdAt", admin.createdAt)
        }
        prefs.edit().putString(KEY_ADMIN, json.toString()).apply()
    }

    fun getAdmin(): AdminUser {
        val str = prefs.getString(KEY_ADMIN, null) ?: return InitialData.defaultAdmin
        return try {
            val obj = JSONObject(str)
            AdminUser(
                id = obj.optString("id", "ADM-001"),
                fullName = obj.optString("fullName", "Bambang Prasetyo"),
                email = obj.optString("email", "dicoba.ngetes@gmail.com"),
                phone = obj.optString("phone", "081298765432"),
                pin = obj.optString("pin", "987654"),
                createdAt = obj.optString("createdAt", "")
            )
        } catch (e: Exception) {
            InitialData.defaultAdmin
        }
    }

    // --- CUSTOMERS ---
    fun saveCustomers(list: List<Customer>) {
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("fullName", c.fullName)
                put("phone", c.phone)
                put("address", c.address)
                put("notes", c.notes ?: "")
                put("pin", c.pin)
                put("createdAt", c.createdAt)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOMERS, arr.toString()).apply()
    }

    fun getCustomers(): List<Customer> {
        val str = prefs.getString(KEY_CUSTOMERS, null) ?: return InitialData.customers
        return try {
            val arr = JSONArray(str)
            val list = mutableListOf<Customer>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Customer(
                        id = obj.optString("id", ""),
                        fullName = obj.optString("fullName", ""),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        pin = obj.optString("pin", "123456"),
                        createdAt = obj.optString("createdAt", "")
                    )
                )
            }
            if (list.isEmpty()) InitialData.customers else list
        } catch (e: Exception) {
            InitialData.customers
        }
    }

    // --- LOANS ---
    fun saveLoans(list: List<Loan>) {
        val arr = JSONArray()
        for (l in list) {
            val obj = JSONObject().apply {
                put("id", l.id)
                put("customerId", l.customerId)
                put("customerName", l.customerName)
                put("customerPhone", l.customerPhone)
                put("type", l.type.name)
                put("itemDescription", l.itemDescription ?: "")
                put("loanTitle", l.loanTitle)
                put("principalAmount", l.principalAmount)
                put("dpAmount", l.dpAmount)
                put("netPrincipal", l.netPrincipal)
                put("marginPercentage", l.marginPercentage)
                put("marginAmount", l.marginAmount)
                put("totalBill", l.totalBill)
                put("tenorCount", l.tenorCount)
                put("tenorUnit", l.tenorUnit.name)
                put("installmentPerPeriod", l.installmentPerPeriod)
                put("startDate", l.startDate)
                put("dueDate", l.dueDate)
                put("nextDueDate", l.nextDueDate)
                put("totalPaid", l.totalPaid)
                put("remainingBalance", l.remainingBalance)
                put("status", l.status.name)
                put("notes", l.notes ?: "")
                put("createdAt", l.createdAt)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_LOANS, arr.toString()).apply()
    }

    fun getLoans(): List<Loan> {
        val str = prefs.getString(KEY_LOANS, null) ?: return InitialData.loans
        return try {
            val arr = JSONArray(str)
            val list = mutableListOf<Loan>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("type", "BARANG")
                val tenorStr = obj.optString("tenorUnit", "BULAN")
                val statusStr = obj.optString("status", "LANCAR")
                list.add(
                    Loan(
                        id = obj.optString("id", ""),
                        customerId = obj.optString("customerId", ""),
                        customerName = obj.optString("customerName", ""),
                        customerPhone = obj.optString("customerPhone", ""),
                        type = try { com.example.model.LoanType.valueOf(typeStr) } catch (_: Exception) { com.example.model.LoanType.BARANG },
                        itemDescription = obj.optString("itemDescription").takeIf { it.isNotBlank() },
                        loanTitle = obj.optString("loanTitle", ""),
                        principalAmount = obj.optDouble("principalAmount", 0.0),
                        dpAmount = obj.optDouble("dpAmount", 0.0),
                        netPrincipal = obj.optDouble("netPrincipal", 0.0),
                        marginPercentage = obj.optDouble("marginPercentage", 5.0),
                        marginAmount = obj.optDouble("marginAmount", 0.0),
                        totalBill = obj.optDouble("totalBill", 0.0),
                        tenorCount = obj.optInt("tenorCount", 6),
                        tenorUnit = try { com.example.model.TenorUnit.valueOf(tenorStr) } catch (_: Exception) { com.example.model.TenorUnit.BULAN },
                        installmentPerPeriod = obj.optDouble("installmentPerPeriod", 0.0),
                        startDate = obj.optString("startDate", ""),
                        dueDate = obj.optString("dueDate", ""),
                        nextDueDate = obj.optString("nextDueDate", ""),
                        totalPaid = obj.optDouble("totalPaid", 0.0),
                        remainingBalance = obj.optDouble("remainingBalance", 0.0),
                        status = try { com.example.model.LoanStatus.valueOf(statusStr) } catch (_: Exception) { com.example.model.LoanStatus.LANCAR },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        createdAt = obj.optString("createdAt", "")
                    )
                )
            }
            if (list.isEmpty()) InitialData.loans else list
        } catch (e: Exception) {
            InitialData.loans
        }
    }

    // --- PAYMENTS ---
    fun savePayments(list: List<Payment>) {
        val arr = JSONArray()
        for (p in list) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("invoiceNo", p.invoiceNo)
                put("loanId", p.loanId)
                put("loanTitle", p.loanTitle)
                put("customerId", p.customerId)
                put("customerName", p.customerName)
                put("customerPhone", p.customerPhone)
                put("amount", p.amount)
                put("installmentNumber", p.installmentNumber)
                put("totalInstallments", p.totalInstallments)
                put("remainingAfter", p.remainingAfter)
                put("paymentMethod", p.paymentMethod.name)
                put("bankOrWalletDetail", p.bankOrWalletDetail ?: "")
                put("referenceNumber", p.referenceNumber ?: "")
                put("notes", p.notes ?: "")
                put("date", p.date)
                put("verifiedBy", p.verifiedBy)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_PAYMENTS, arr.toString()).apply()
    }

    fun getPayments(): List<Payment> {
        val str = prefs.getString(KEY_PAYMENTS, null) ?: return InitialData.payments
        return try {
            val arr = JSONArray(str)
            val list = mutableListOf<Payment>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val methodStr = obj.optString("paymentMethod", "TRANSFER_BANK")
                list.add(
                    Payment(
                        id = obj.optString("id", ""),
                        invoiceNo = obj.optString("invoiceNo", ""),
                        loanId = obj.optString("loanId", ""),
                        loanTitle = obj.optString("loanTitle", ""),
                        customerId = obj.optString("customerId", ""),
                        customerName = obj.optString("customerName", ""),
                        customerPhone = obj.optString("customerPhone", ""),
                        amount = obj.optDouble("amount", 0.0),
                        installmentNumber = obj.optInt("installmentNumber", 1),
                        totalInstallments = obj.optInt("totalInstallments", 1),
                        remainingAfter = obj.optDouble("remainingAfter", 0.0),
                        paymentMethod = try { com.example.model.PaymentMethod.valueOf(methodStr) } catch (_: Exception) { com.example.model.PaymentMethod.TRANSFER_BANK },
                        bankOrWalletDetail = obj.optString("bankOrWalletDetail").takeIf { it.isNotBlank() },
                        referenceNumber = obj.optString("referenceNumber").takeIf { it.isNotBlank() },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        date = obj.optString("date", ""),
                        verifiedBy = obj.optString("verifiedBy", "Bambang Prasetyo (Admin Amar Bank)")
                    )
                )
            }
            if (list.isEmpty()) InitialData.payments else list
        } catch (e: Exception) {
            InitialData.payments
        }
    }

    // --- NOTIFICATIONS ---
    fun saveNotifications(list: List<NotificationItem>) {
        val arr = JSONArray()
        for (n in list) {
            val obj = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("message", n.message)
                put("type", n.type.name)
                put("loanId", n.loanId ?: "")
                put("customerId", n.customerId ?: "")
                put("date", n.date)
                put("isRead", n.isRead)
                put("daysRemaining", n.daysRemaining ?: 999)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, arr.toString()).apply()
    }

    fun getNotifications(): List<NotificationItem> {
        val str = prefs.getString(KEY_NOTIFICATIONS, null) ?: return InitialData.notifications
        return try {
            val arr = JSONArray(str)
            val list = mutableListOf<NotificationItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val typeStr = obj.optString("type", "SYSTEM")
                list.add(
                    NotificationItem(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        message = obj.optString("message", ""),
                        type = try { com.example.model.NotificationType.valueOf(typeStr) } catch (_: Exception) { com.example.model.NotificationType.SYSTEM },
                        loanId = obj.optString("loanId").takeIf { it.isNotBlank() },
                        customerId = obj.optString("customerId").takeIf { it.isNotBlank() },
                        date = obj.optString("date", ""),
                        isRead = obj.optBoolean("isRead", false),
                        daysRemaining = if (obj.has("daysRemaining")) obj.optInt("daysRemaining") else null
                    )
                )
            }
            if (list.isEmpty()) InitialData.notifications else list
        } catch (e: Exception) {
            InitialData.notifications
        }
    }

    fun clearAllData() {
        prefs.edit().apply {
            remove(KEY_CUSTOMERS)
            remove(KEY_LOANS)
            remove(KEY_PAYMENTS)
            remove(KEY_NOTIFICATIONS)
            apply()
        }
    }
}
