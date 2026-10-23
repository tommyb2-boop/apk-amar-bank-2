package com.example.ui.admin

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.LoanStatus
import com.example.model.Payment
import com.example.model.SystemSettings
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
fun CustomerDetailDialog(
    customer: Customer,
    loans: List<Loan>,
    payments: List<Payment>,
    systemSettings: SystemSettings,
    onViewLoanReceipt: (Loan) -> Unit,
    onViewPaymentReceipt: (Payment) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val customerLoans = remember(loans, customer.id) {
        loans.filter { it.customerId == customer.id }
    }
    val customerPayments = remember(payments, customer.id) {
        payments.filter { it.customerId == customer.id }
    }

    val totalPlafon = remember(customerLoans) { customerLoans.sumOf { it.totalBill } }
    val totalPaid = remember(customerLoans) { customerLoans.sumOf { it.totalPaid } }
    val remainingBalance = remember(customerLoans) { customerLoans.sumOf { it.remainingBalance } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header: Nama Pelanggan (CUST-....), Nomor WhatsApp, dan PIN Login
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customer.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${customer.id})",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmarBankRoyalBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "WhatsApp: ${customer.phone} • PIN Login: ${customer.pin}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // 3 Kolom Kop Keuangan Nasabah: Total Piutang ; Terbayar ; Sisa Piutang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Total Piutang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "TOTAL PIUTANG", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmarBankBlue)
                            Text(text = Formatters.formatRupiah(totalPlafon), fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankRoyalBlue)
                        }
                    }

                    // Terbayar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "TERBAYAR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmarBankEmerald)
                            Text(text = Formatters.formatRupiah(totalPaid), fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankEmerald)
                        }
                    }

                    // Sisa Piutang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "SISA PIUTANG", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmarBankAmber)
                            Text(text = Formatters.formatRupiah(remainingBalance), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Judul Section Piutang (Tab dihapus, langsung seluruh piutang)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Piutang Nasabah (${customerLoans.size} Akad)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                // List Seluruh Piutang Nasabah:
                // Nominal rupiah, tanggal jatuh tempo, keterangan LUNAS jika sudah dibayar dan NON jika belum dibayar,
                // di samping sebelah kanannya tombol KWITANSI.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                ) {
                    if (customerLoans.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Nasabah belum memiliki catatan piutang.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(customerLoans) { loan ->
                                val isLunas = loan.status == LoanStatus.LUNAS
                                val latestPayment = customerPayments.firstOrNull { it.loanId == loan.id }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left: Detail nominal, judul akad, tanggal jatuh tempo, dan keterangan LUNAS/NON
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${loan.loanTitle} (${loan.id})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = Formatters.formatRupiah(loan.totalBill),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 12.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• Tempo: ${Formatters.formatDateIndo(loan.nextDueDate)}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Sisa: ${Formatters.formatRupiah(loan.remainingBalance)}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                // Keterangan LUNAS jika sudah dibayar, NON jika belum dibayar
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (isLunas) AmarBankEmeraldLight else AmarBankRoseLight,
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                                ) {
                                                    Text(
                                                        text = if (isLunas) "LUNAS" else "NON",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isLunas) AmarBankEmerald else AmarBankRose
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Right: Tombol Kwitansi
                                        Button(
                                            onClick = {
                                                if (latestPayment != null) {
                                                    onViewPaymentReceipt(latestPayment)
                                                } else {
                                                    onViewLoanReceipt(loan)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Kwitansi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Footer Actions: HANYA Tombol Kirim WhatsApp (tombol simpan pdf lokal dihapus)
                // Pesan WhatsApp berupa teks ringkasan: Total nominal piutang, Total yang sudah dibayarkan, Total sisa piutang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            val msg = """
                            Halo Bapak/Ibu ${customer.fullName},
                            Berikut ringkasan tagihan kredit & piutang Anda di Amar Bank (ID: ${customer.id}):

                            - Total Nominal Piutang : ${Formatters.formatRupiah(totalPlafon)}
                            - Total yang Sudah Dibayarkan : ${Formatters.formatRupiah(totalPaid)}
                            - Total Sisa Piutang : ${Formatters.formatRupiah(remainingBalance)}

                            Terima kasih atas kepercayaan Anda kepada Amar Bank.
                            """.trimIndent()
                            val url = Formatters.createWhatsAppUrl(customer.phone, msg)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Membuka WhatsApp...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim WhatsApp ke Nasabah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
