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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.model.Loan
import com.example.model.LoanStatus
import com.example.model.Payment
import com.example.model.PaymentMethod
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters

@Composable
fun PaymentManagementDialog(
    payments: List<Payment>,
    loans: List<Loan>,
    onRecordPayment: (payment: Payment, updatedLoan: Loan) -> Unit,
    onViewReceipt: (Payment) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isRecordingNew by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val activeLoans = remember(loans) {
        loans.filter { it.remainingBalance > 0 }
    }

    var selectedLoanId by remember {
        mutableStateOf(activeLoans.firstOrNull()?.id ?: "")
    }

    val selectedLoan = remember(selectedLoanId, loans) {
        loans.find { it.id == selectedLoanId }
    }

    var payAmount by remember(selectedLoan) {
        mutableDoubleStateOf(selectedLoan?.installmentPerPeriod?.coerceAtMost(selectedLoan.remainingBalance) ?: 0.0)
    }

    var selectedMethod by remember { mutableStateOf(PaymentMethod.TRANSFER_BANK) }
    var formBankDetail by remember { mutableStateOf("BCA Virtual Account Amar") }
    var formReference by remember { mutableStateOf("TRX-${(10000000..99999999).random()}") }

    val filteredPayments = remember(payments, searchQuery) {
        if (searchQuery.isBlank()) payments
        else {
            val q = searchQuery.lowercase()
            payments.filter {
                it.invoiceNo.lowercase().contains(q) ||
                it.customerName.lowercase().contains(q) ||
                it.loanTitle.lowercase().contains(q)
            }
        }
    }

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
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AmarBankEmerald, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isRecordingNew) "Catat Pembayaran Angsuran" else "Riwayat Pembayaran & Kwitansi (${payments.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Kwitansi digital otomatis & update sisa piutang",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                if (isRecordingNew) {
                    // FORM RECORD PAYMENT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = "Pilih Akad Pinjaman Berjalan *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(4.dp))

                        if (activeLoans.isEmpty()) {
                            Text(text = "Tidak ada tagihan kredit aktif saat ini.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            // Selector for active loans
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                activeLoans.forEach { loan ->
                                    val isSelected = loan.id == selectedLoanId
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                            .border(1.dp, if (isSelected) AmarBankRoyalBlue else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                            .clickable { selectedLoanId = loan.id }
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = loan.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(text = "${loan.loanTitle} (${loan.id})", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = "Sisa: ${Formatters.formatRupiah(loan.remainingBalance)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AmarBankBlue)
                                                Text(text = "Cicilan: ${Formatters.formatRupiah(loan.installmentPerPeriod)}", fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedLoan != null) {
                            Text(text = "Nominal Pembayaran Diterima (Rp) *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = payAmount.toLong().toString(),
                                onValueChange = { payAmount = it.toDoubleOrNull() ?: 0.0 },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // Quick preset buttons
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { payAmount = selectedLoan.installmentPerPeriod.coerceAtMost(selectedLoan.remainingBalance) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("1x Angsuran", fontSize = 9.sp)
                                }
                                OutlinedButton(
                                    onClick = { payAmount = (selectedLoan.installmentPerPeriod * 2).coerceAtMost(selectedLoan.remainingBalance) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("2x Angsuran", fontSize = 9.sp)
                                }
                                OutlinedButton(
                                    onClick = { payAmount = selectedLoan.remainingBalance },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Lunas Total", fontSize = 9.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(text = "Metode Pembayaran *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PaymentMethod.values().forEach { m ->
                                    val isSelected = selectedMethod == m
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) AmarBankBlue else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedMethod = m
                                                formBankDetail = when (m) {
                                                    PaymentMethod.TUNAI -> "Kasir Cabang"
                                                    PaymentMethod.NON_TUNAI -> "Non Tunai (Transfer / QRIS)"
                                                    PaymentMethod.TRANSFER_BANK -> "BCA Virtual Account"
                                                    PaymentMethod.QRIS -> "QRIS Amar Bank"
                                                    PaymentMethod.E_WALLET -> "GoPay / DANA"
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when(m) {
                                                PaymentMethod.TUNAI -> "Tunai"
                                                PaymentMethod.NON_TUNAI -> "Non Tunai"
                                                PaymentMethod.TRANSFER_BANK -> "Transfer"
                                                PaymentMethod.QRIS -> "QRIS"
                                                PaymentMethod.E_WALLET -> "E-Wallet"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = formBankDetail,
                                onValueChange = { formBankDetail = it },
                                label = { Text("Detail Bank / Kanal") },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = formReference,
                                onValueChange = { formReference = it },
                                label = { Text("Nomor Referensi Transaksi") },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                ),
                                colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            val remainingAfter = maxOf(0.0, selectedLoan.remainingBalance - payAmount)
                            val isLunas = remainingAfter == 0.0

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isLunas) Color(0xFFECFDF5) else Color(0xFFEFF6FF), RoundedCornerShape(14.dp))
                                    .border(1.dp, if (isLunas) Color(0xFFA7F3D0) else Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Status Setelah Bayar:", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = if (isLunas) "LUNAS TOTAL" else "CICILAN BERJALAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isLunas) AmarBankEmerald else AmarBankRoyalBlue)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Sisa Piutang Akhir:", fontSize = 11.sp, color = Color.Gray)
                                        Text(text = Formatters.formatRupiah(remainingAfter), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        if (payAmount <= 0.0) {
                                            Toast.makeText(context, "Nominal pembayaran harus lebih dari 0", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        val currentInstallmentNo = (selectedLoan.totalPaid / selectedLoan.installmentPerPeriod).toInt() + 1
                                        val todayStr = Formatters.getCurrentDateYmd().replace("-", "").take(6)
                                        val invNo = "KW-$todayStr-${(100..999).random()}"

                                        val newPayment = Payment(
                                            id = "PAY-${System.currentTimeMillis()}",
                                            invoiceNo = invNo,
                                            loanId = selectedLoan.id,
                                            loanTitle = selectedLoan.loanTitle,
                                            customerId = selectedLoan.customerId,
                                            customerName = selectedLoan.customerName,
                                            customerPhone = selectedLoan.customerPhone,
                                            amount = payAmount,
                                            installmentNumber = currentInstallmentNo.coerceAtMost(selectedLoan.tenorCount),
                                            totalInstallments = selectedLoan.tenorCount,
                                            remainingAfter = remainingAfter,
                                            paymentMethod = selectedMethod,
                                            bankOrWalletDetail = formBankDetail.ifBlank { null },
                                            referenceNumber = formReference.ifBlank { null },
                                            date = Formatters.getCurrentIsoDate(),
                                            verifiedBy = "Bambang Prasetyo (Admin Amar Bank)"
                                        )

                                        val updatedLoan = selectedLoan.copy(
                                            totalPaid = selectedLoan.totalPaid + payAmount,
                                            remainingBalance = remainingAfter,
                                            status = if (isLunas) LoanStatus.LUNAS else selectedLoan.status,
                                            nextDueDate = if (isLunas) selectedLoan.dueDate else Formatters.calculateNextDueDate(selectedLoan.startDate, selectedLoan.tenorUnit.name, currentInstallmentNo + 1)
                                        )

                                        onRecordPayment(newPayment, updatedLoan)
                                        isRecordingNew = false
                                        onViewReceipt(newPayment)
                                        Toast.makeText(context, "Pembayaran berhasil dicatat!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simpan & Cetak", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { isRecordingNew = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Batal", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // LIST PAYMENTS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari faktur / nasabah...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF475569)) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { isRecordingNew = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bayar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredPayments) { pay ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(18.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                                        .clickable { onViewReceipt(pay) }
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
                                                Text(text = pay.invoiceNo, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AmarBankRoyalBlue)
                                                Text(text = pay.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                                Text(text = "${pay.loanTitle} (${Formatters.formatDateIndo(pay.date)})", fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = Formatters.formatRupiah(pay.amount), fontWeight = FontWeight.Black, fontSize = 13.sp, color = AmarBankEmerald)
                                            Text(text = "Sisa: ${Formatters.formatRupiah(pay.remainingAfter)}", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
