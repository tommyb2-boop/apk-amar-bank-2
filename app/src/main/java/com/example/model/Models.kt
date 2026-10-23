package com.example.model

enum class LoanType {
    BARANG,
    TUNAI
}

enum class TenorUnit {
    BULAN,
    MINGGU,
    HARI
}

enum class LoanStatus {
    LANCAR,
    PERHATIAN,
    MENUNGGAK,
    LUNAS
}

enum class PaymentMethod {
    TUNAI,
    NON_TUNAI,
    TRANSFER_BANK,
    QRIS,
    E_WALLET
}

enum class NotificationType {
    DUE_SOON,
    OVERDUE,
    PAYMENT,
    SYSTEM
}

data class AdminUser(
    val id: String = "ADM-001",
    val fullName: String = "Bambang Prasetyo",
    val email: String = "dicoba.ngetes@gmail.com",
    val phone: String = "081298765432",
    val pin: String = "987654",
    val createdAt: String = ""
)

data class Customer(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String? = null,
    val pin: String = "123456",
    val createdAt: String = ""
)

data class Loan(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val type: LoanType = LoanType.BARANG,
    val itemDescription: String? = null,
    val loanTitle: String = "",
    val principalAmount: Double = 0.0,
    val dpAmount: Double = 0.0,
    val netPrincipal: Double = 0.0,
    val marginPercentage: Double = 5.0,
    val marginAmount: Double = 0.0,
    val totalBill: Double = 0.0,
    val tenorCount: Int = 6,
    val tenorUnit: TenorUnit = TenorUnit.BULAN,
    val installmentPerPeriod: Double = 0.0,
    val startDate: String = "",
    val dueDate: String = "",
    val nextDueDate: String = "",
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val status: LoanStatus = LoanStatus.LANCAR,
    val notes: String? = null,
    val createdAt: String = ""
)

data class Payment(
    val id: String = "",
    val invoiceNo: String = "",
    val loanId: String = "",
    val loanTitle: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val amount: Double = 0.0,
    val installmentNumber: Int = 1,
    val totalInstallments: Int = 1,
    val remainingAfter: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.TRANSFER_BANK,
    val bankOrWalletDetail: String? = null,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val date: String = "",
    val verifiedBy: String = "Bambang Prasetyo (Admin Amar Bank)"
)

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val loanId: String? = null,
    val customerId: String? = null,
    val date: String = "",
    val isRead: Boolean = false,
    val daysRemaining: Int? = null
)

data class SystemSettings(
    val cityName: String = "Surabaya",
    val branchName: String = "PT BANK AMAR INDONESIA Tbk",
    val divisionName: String = "Divisi Manajemen Piutang & Pembiayaan Ritel",
    val branchAddress: String = "Graha Amar, Jl. Basuki Rahmat 122",
    val branchPhone: String = "(031) 567-8910",
    val officialWhatsApp: String = "0812-9876-5432",
    val responsibleName: String = "Bambang Prasetyo, S.E.",
    val responsibleTitle: String = "Head of Retail & Credit Admin"
)

data class AppSession(
    val role: String? = null, // "ADMIN", "CUSTOMER", null
    val adminId: String? = null,
    val customerId: String? = null
)
