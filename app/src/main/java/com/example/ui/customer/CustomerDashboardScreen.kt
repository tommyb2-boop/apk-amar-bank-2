package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.LoanStatus
import com.example.model.LoanType
import com.example.model.Payment
import com.example.ui.components.AmarBankLogo
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.AmarBankAmber
import com.example.ui.theme.AmarBankAmberLight
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankEmeraldLight
import com.example.ui.theme.AmarBankRose
import com.example.ui.theme.AmarBankRoseLight
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters

@Composable
fun CustomerDashboardScreen(
    customer: Customer,
    allLoans: List<Loan>,
    allPayments: List<Payment>,
    adminPhone: String,
    isUpdatingCloud: Boolean,
    onUpdateCloud: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedReceiptPayment by remember { mutableStateOf<Payment?>(null) }

    // STRICT ZERO-LEAKAGE DATA ISOLATION: Filter ONLY current customer's data!
    val myLoans = remember(allLoans, customer.id) {
        allLoans.filter { it.customerId == customer.id }
    }
    val myPayments = remember(allPayments, customer.id) {
        allPayments.filter { it.customerId == customer.id }
    }

    val totalPaid = remember(myLoans) { myLoans.sumOf { it.totalPaid } }
    val remainingBalance = remember(myLoans) { myLoans.sumOf { it.remainingBalance } }
    val activeLoans = remember(myLoans) { myLoans.filter { it.remainingBalance > 0 } }
    val lunasLoansCount = remember(myLoans) { myLoans.count { it.status == LoanStatus.LUNAS } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // 1. Top Header App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AmarBankLogo(iconSize = 34.dp, showText = true)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Update Button
                    Button(
                        onClick = onUpdateCloud,
                        enabled = !isUpdatingCloud,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue),
                        modifier = Modifier.height(34.dp)
                    ) {
                        if (isUpdatingCloud) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF67E8F9))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = customer.fullName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
                        Text(text = customer.id, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = AmarBankRoyalBlue, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onLogout, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // 2. Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Welcome Greeting & Customer Card (Gradient Dark Blue/Navy to Indigo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF002D72),
                                Color(0xFF1E3A8A),
                                Color(0xFF312E81)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(text = "Portal Mandiri Nasabah", fontSize = 11.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Medium)
                            Text(
                                text = customer.fullName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = customer.id,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Sisa Tagihan Anda", fontSize = 11.sp, color = Color(0xFFBFDBFE))
                            Text(
                                text = Formatters.formatRupiah(remainingBalance),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Sudah Dibayar", fontSize = 11.sp, color = Color(0xFFBFDBFE))
                            Text(
                                text = Formatters.formatRupiah(totalPaid),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0x22FFFFFF))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Konfirmasi WhatsApp Primary CTA
                    Button(
                        onClick = {
                            val todayStr = Formatters.formatDateIndo(Formatters.getCurrentIsoDate())
                            val targetLoan = activeLoans.firstOrNull() ?: myLoans.firstOrNull()
                            val msg = """
                            Halo Admin Amar Bank,
                            Saya ingin melakukan konfirmasi informasi tagihan/angsuran saya:
                            - ID Nasabah   : *${customer.id}*
                            - Nama Lengkap : *${customer.fullName}*
                            - Akad Kredit  : *${targetLoan?.loanTitle ?: "Pinjaman Amar Bank"}*
                            - Sisa Tagihan : *${if (targetLoan != null) Formatters.formatRupiah(targetLoan.remainingBalance) else Formatters.formatRupiah(remainingBalance)}*
                            - Tanggal Cek  : $todayStr
                            Mohon bantuannya untuk informasi rekening tujuan pembayaran atau verifikasi transaksi. Terima kasih.
                            """.trimIndent()
                            val url = Formatters.createWhatsAppUrl(adminPhone, msg)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Konfirmasi via WhatsApp ke Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Section 1: Daftar Akad Kredit Pribadi
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = AmarBankBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Akad Kredit Anda (${myLoans.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "${activeLoans.size} Berjalan • $lunasLoansCount Lunas",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (myLoans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Belum ada akad kredit yang tercatat atas nama Anda.", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        myLoans.forEach { loan ->
                            val isLunas = loan.status == LoanStatus.LUNAS
                            val isOverdue = loan.status == LoanStatus.MENUNGGAK
                            val isDueSoon = loan.status == LoanStatus.PERHATIAN
                            val percentPaid = if (loan.totalBill > 0) ((loan.totalPaid / loan.totalBill) * 100).toInt() else 0

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = loan.id,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = AmarBankRoyalBlue
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (loan.type == LoanType.BARANG) "Kredit Barang" else "Pinjaman Tunai",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Text(
                                                text = loan.loanTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (!loan.itemDescription.isNullOrBlank()) {
                                                Text(
                                                    text = loan.itemDescription,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        // Status Pill: LUNAS, MACET, DEKAT TEMPO, atau LANCAR
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    when {
                                                        isLunas -> AmarBankEmeraldLight
                                                        isOverdue -> AmarBankRoseLight
                                                        isDueSoon -> AmarBankAmberLight
                                                        else -> AmarBankEmeraldLight
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    isLunas -> "LUNAS"
                                                    isOverdue -> "MACET"
                                                    isDueSoon -> "DEKAT TEMPO"
                                                    else -> "LANCAR"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isLunas -> AmarBankEmerald
                                                    isOverdue -> AmarBankRose
                                                    isDueSoon -> AmarBankAmber
                                                    else -> AmarBankEmerald
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Progress Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Progres Pelunasan ($percentPaid%)", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = "${Formatters.formatRupiah(loan.totalPaid)} / ${Formatters.formatRupiah(loan.totalBill)}",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (percentPaid / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = if (isLunas) AmarBankEmerald else AmarBankRoyalBlue,
                                        trackColor = Color(0xFFE2E8F0)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "Angsuran / Periode:", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                text = Formatters.formatRupiah(loan.installmentPerPeriod),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Per ${loan.tenorUnit.name.lowercase()} (Total ${loan.tenorCount}x)",
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "Sisa Tagihan:", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                text = Formatters.formatRupiah(loan.remainingBalance),
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                color = if (isLunas) AmarBankEmerald else AmarBankBlue
                                            )
                                            if (!isLunas) {
                                                Text(
                                                    text = "Tempo: ${Formatters.formatDateIndo(loan.nextDueDate)}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AmarBankAmber
                                                )
                                            }
                                        }
                                    }

                                    if (!isLunas) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = {
                                                val msg = "Halo Admin Amar Bank, saya ingin konfirmasi pembayaran untuk tagihan ${loan.loanTitle} (ID: ${loan.id}, Sisa: ${Formatters.formatRupiah(loan.remainingBalance)}). Mohon petunjuk rekening transfer. Terima kasih."
                                                val url = Formatters.createWhatsAppUrl(adminPhone, msg)
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmarBankEmerald)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Konfirmasi Pembayaran Tagihan Ini", fontSize = 11.sp, color = Color(0xFF334155))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Riwayat Transaksi & Kwitansi Digital Resmi
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = AmarBankRoyalBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Riwayat Transaksi & Kwitansi (${myPayments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(text = "Ketuk untuk buka kwitansi", fontSize = 10.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (myPayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Belum ada riwayat pembayaran yang tercatat.", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        myPayments.forEach { pay ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(18.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                                    .clickable { selectedReceiptPayment = pay }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, tint = AmarBankRoyalBlue, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = pay.invoiceNo, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AmarBankRoyalBlue)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(AmarBankEmeraldLight, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(text = "LUNAS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmarBankEmerald)
                                                }
                                            }
                                            Text(
                                                text = "${Formatters.formatDateIndo(pay.date)} • Angsuran ke-${pay.installmentNumber}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = Formatters.formatRupiah(pay.amount),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = AmarBankEmerald
                                            )
                                            Text(
                                                text = pay.paymentMethod.name,
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer: The Update
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "THE UPDATE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
            }
        }
    }

    // Kwitansi Modal Popup
    if (selectedReceiptPayment != null) {
        ReceiptDialog(
            payment = selectedReceiptPayment!!,
            onDismiss = { selectedReceiptPayment = null }
        )
    }
}
