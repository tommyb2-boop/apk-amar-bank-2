package com.example.ui.admin

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.PaymentMethod
import com.example.model.SystemSettings
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.AmarBankAmber
import com.example.ui.theme.AmarBankAmberLight
import com.example.ui.theme.AmarBankBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankEmeraldLight
import com.example.ui.theme.AmarBankRose
import com.example.ui.theme.AmarBankRoseLight
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters
import com.example.utils.PdfExporter

@Composable
fun CustomerReceivablesListDialog(
    customer: Customer,
    loans: List<Loan>,
    systemSettings: SystemSettings,
    onRecordPayment: (Payment, Loan) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val customerLoans = remember(loans, customer.id) {
        loans.filter { it.customerId == customer.id }
    }

    // Modal state for Bayar TUNAI / NON TUNAI
    var loanToPay by remember { mutableStateOf<Loan?>(null) }
    var selectedMethod by remember { mutableStateOf<PaymentMethod>(PaymentMethod.TUNAI) }

    // Struk Pembayaran yang baru saja dibayar
    var newlyCreatedPayment by remember { mutableStateOf<Payment?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
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
                                text = "Daftar Piutang: ${customer.fullName}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${customer.id})",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AmarBankRoyalBlue
                            )
                        }
                        Text(
                            text = "Piutang jatuh tempo & belum jatuh tempo",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // Subtitle Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sisaTotal = customerLoans.sumOf { it.remainingBalance }
                    Text(
                        text = "Total Kewajiban: ${Formatters.formatRupiah(sisaTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${customerLoans.count { it.status != LoanStatus.LUNAS }} Belum Lunas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = AmarBankRose
                    )
                }

                // List Piutang Nasabah
                // Isi per baris: Nominal rupiah, tanggal jatuh tempo, dan tombol BAYAR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                ) {
                    if (customerLoans.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Tidak ada piutang untuk nasabah ini.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(customerLoans) { loan ->
                                val isLunas = loan.status == LoanStatus.LUNAS
                                val isOverdue = loan.status == LoanStatus.MENUNGGAK
                                val isDueSoon = loan.status == LoanStatus.PERHATIAN
                                val installmentAmount = if (loan.remainingBalance < loan.installmentPerPeriod && loan.remainingBalance > 0) {
                                    loan.remainingBalance
                                } else {
                                    loan.installmentPerPeriod
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        // Header Baris: Jenis & Nama Kredit
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${loan.loanTitle} (${loan.id})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        when {
                                                            isLunas -> AmarBankEmeraldLight
                                                            isOverdue -> AmarBankRoseLight
                                                            isDueSoon -> AmarBankAmberLight
                                                            else -> Color(0xFFEFF6FF)
                                                        },
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = when {
                                                        isLunas -> "LUNAS"
                                                        isOverdue -> "JATUH TEMPO"
                                                        isDueSoon -> "DEKAT TEMPO"
                                                        else -> "BELUM TEMPO"
                                                    },
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = when {
                                                        isLunas -> AmarBankEmerald
                                                        isOverdue -> AmarBankRose
                                                        isDueSoon -> AmarBankAmber
                                                        else -> AmarBankRoyalBlue
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Baris Utama: Nominal Rupiah, Tanggal Jatuh Tempo, dan Tombol BAYAR
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. Nominal Rupiah
                                            Column {
                                                Text(text = "Nominal Tagihan", fontSize = 8.sp, color = Color(0xFF64748B))
                                                Text(
                                                    text = Formatters.formatRupiah(installmentAmount),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }

                                            // 2. Tanggal Jatuh Tempo
                                            Column {
                                                Text(text = "Jatuh Tempo", fontSize = 8.sp, color = Color(0xFF64748B))
                                                Text(
                                                    text = Formatters.formatDateIndo(loan.nextDueDate),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp,
                                                    color = if (isOverdue) AmarBankRose else Color(0xFF334155)
                                                )
                                            }

                                            // 3. Tombol BAYAR
                                            if (!isLunas) {
                                                Button(
                                                    onClick = {
                                                        selectedMethod = PaymentMethod.TUNAI
                                                        loanToPay = loan
                                                    },
                                                    modifier = Modifier.height(32.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                                                ) {
                                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = "BAYAR", fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmarBankEmerald, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Lunas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmarBankEmerald)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Sisa Total Piutang: ${Formatters.formatRupiah(loan.remainingBalance)} (Tenor: ${loan.tenorCount} ${loan.tenorUnit.name.lowercase()})",
                                            fontSize = 9.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Modal Pilihan: TUNAI atau NON TUNAI + Tombol OK
    if (loanToPay != null) {
        val loan = loanToPay!!
        val defaultPayAmount = if (loan.remainingBalance < loan.installmentPerPeriod && loan.remainingBalance > 0) {
            loan.remainingBalance
        } else {
            loan.installmentPerPeriod
        }
        var manualNominalInput by remember(loanToPay) {
            mutableStateOf(defaultPayAmount.toLong().toString())
        }
        val parsedAmount = manualNominalInput.filter { it.isDigit() }.toDoubleOrNull() ?: defaultPayAmount

        Dialog(
            onDismissRequest = { loanToPay = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Konfirmasi Pembayaran Piutang",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${customer.fullName} • ${loan.loanTitle}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Manual Nominal Pembayaran
                    Text(
                        text = "Nominal yang Dibayarkan (Bisa Diubah):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = manualNominalInput,
                        onValueChange = { manualNominalInput = it.filter { ch -> ch.isDigit() } },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = AmarBankEmerald) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (parsedAmount < defaultPayAmount) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Tidak Lunas / Bayar Sebagian (Sisa Piutang: ${Formatters.formatRupiah((loan.remainingBalance - parsedAmount).coerceAtLeast(0.0))})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmarBankAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pilihan TUNAI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selectedMethod == PaymentMethod.TUNAI) Color(0xFFEFF6FF) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.5.dp, if (selectedMethod == PaymentMethod.TUNAI) AmarBankBlue else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { selectedMethod = PaymentMethod.TUNAI }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedMethod == PaymentMethod.TUNAI,
                                onClick = { selectedMethod = PaymentMethod.TUNAI },
                                colors = RadioButtonDefaults.colors(selectedColor = AmarBankBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("TUNAI", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Pembayaran langsung uang tunai di kasir/teller", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pilihan NON TUNAI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selectedMethod == PaymentMethod.NON_TUNAI) Color(0xFFEFF6FF) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.5.dp, if (selectedMethod == PaymentMethod.NON_TUNAI) AmarBankBlue else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { selectedMethod = PaymentMethod.NON_TUNAI }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedMethod == PaymentMethod.NON_TUNAI,
                                onClick = { selectedMethod = PaymentMethod.NON_TUNAI },
                                colors = RadioButtonDefaults.colors(selectedColor = AmarBankBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("NON TUNAI", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Text("Transfer Bank, QRIS, atau Dompet Digital", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tombol Batal & Tombol OK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { loanToPay = null },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Text("Batal", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                        }

                        // Tombol OK: Memproses pembayaran dan memunculkan struk pembayaran
                        Button(
                            onClick = {
                                val effectivePay = if (parsedAmount > 0) parsedAmount else defaultPayAmount
                                val remainingAfter = (loan.remainingBalance - effectivePay).coerceAtLeast(0.0)
                                val isNowLunas = remainingAfter <= 0.0

                                val payment = Payment(
                                    id = "PAY-${System.currentTimeMillis().toString().takeLast(6)}",
                                    invoiceNo = "INV/${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}/${(1000..9999).random()}",
                                    loanId = loan.id,
                                    customerId = customer.id,
                                    customerName = customer.fullName,
                                    loanTitle = loan.loanTitle,
                                    amount = effectivePay,
                                    date = Formatters.getCurrentIsoDate(),
                                    paymentMethod = selectedMethod,
                                    referenceNumber = if (selectedMethod == PaymentMethod.NON_TUNAI) "REF-${System.currentTimeMillis().toString().takeLast(4)}" else null,
                                    remainingAfter = remainingAfter,
                                    notes = "Pembayaran ${loan.loanTitle} (${selectedMethod.name})"
                                )

                                val updatedLoan = loan.copy(
                                    totalPaid = loan.totalPaid + effectivePay,
                                    remainingBalance = remainingAfter,
                                    status = if (isNowLunas) LoanStatus.LUNAS else LoanStatus.LANCAR,
                                    nextDueDate = if (isNowLunas) loan.nextDueDate else Formatters.calculateNextDueDate(loan.nextDueDate, loan.tenorUnit.name, 1)
                                )

                                onRecordPayment(payment, updatedLoan)
                                loanToPay = null
                                newlyCreatedPayment = payment
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Tampilan Struk Pembayaran dengan tombol Kirim WhatsApp (File PDF Struk)
    if (newlyCreatedPayment != null) {
        val payment = newlyCreatedPayment!!
        ReceiptDialog(
            payment = payment,
            onDismiss = { newlyCreatedPayment = null }
        )
    }
}
