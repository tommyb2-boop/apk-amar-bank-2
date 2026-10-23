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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.LoanStatus
import com.example.model.LoanType
import com.example.model.TenorUnit
import com.example.ui.components.AmarBankTextFieldDefaults
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanManagementDialog(
    loans: List<Loan>,
    customers: List<Customer>,
    onSaveLoan: (Loan) -> Unit,
    onViewLoanReceipt: (Loan) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Tab Type
    var selectedLoanType by remember { mutableStateOf(LoanType.BARANG) }

    // Dropdown Nasabah
    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }

    // Form inputs
    var formTitle by remember { mutableStateOf("Kredit Elektronik") }
    var formItemDesc by remember { mutableStateOf("") }
    var formPrincipalText by remember { mutableStateOf("5000000") }
    var formDpText by remember { mutableStateOf("0") }

    // Dua kotak keuntungan yang saling terkoneksi
    var formProfitRupiahText by remember { mutableStateOf("500000") }
    var formProfitPercentText by remember { mutableStateOf("10.0") }

    // Kotak setoran rupiah & durasi tenor (saling terhubung)
    var formSetoranRupiahText by remember { mutableStateOf("550000") }
    var formTenorCountText by remember { mutableStateOf("10") }
    var formTenorUnit by remember { mutableStateOf(TenorUnit.BULAN) }
    var isTenorDropdownExpanded by remember { mutableStateOf(false) }

    var formStartDate by remember { mutableStateOf(Formatters.getCurrentDateYmd()) }

    // Helpers to recalculate
    val principal = formPrincipalText.toDoubleOrNull() ?: 0.0
    val dp = formDpText.toDoubleOrNull() ?: 0.0
    val netPrincipal = maxOf(0.0, principal - dp)

    // Total Piutang = Pokok Bersih + Keuntungan Rupiah
    val profitRupiah = formProfitRupiahText.toDoubleOrNull() ?: 0.0
    val totalPiutang = netPrincipal + profitRupiah

    val setoranRupiah = formSetoranRupiahText.toDoubleOrNull() ?: 0.0
    val tenorCount = formTenorCountText.toIntOrNull() ?: 1

    val firstDueDate = remember(formStartDate, formTenorUnit) {
        Formatters.calculateNextDueDate(formStartDate, formTenorUnit.name, 1)
    }
    val finalDueDate = remember(formStartDate, tenorCount, formTenorUnit) {
        Formatters.calculateDueDate(formStartDate, tenorCount, formTenorUnit.name)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Lembar Kerja
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
                                .size(34.dp)
                                .background(AmarBankBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lembar Kerja Akad Kredit Baru",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Pencatatan pembiayaan nasabah secara langsung",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pilihan Tipe: Kredit Barang vs Pinjaman Tunai
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selectedLoanType == LoanType.BARANG) AmarBankBlue else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLoanType = LoanType.BARANG
                                if (formTitle.isBlank() || formTitle.contains("Tunai")) formTitle = "Kredit Elektronik/Barang"
                            }
                            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "KREDIT BARANG",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedLoanType == LoanType.BARANG) Color.White else Color(0xFF334155)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selectedLoanType == LoanType.TUNAI) AmarBankBlue else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLoanType = LoanType.TUNAI
                                if (formTitle.isBlank() || formTitle.contains("Barang")) formTitle = "Pinjaman Dana Tunai"
                            }
                            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PINJAMAN TUNAI",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedLoanType == LoanType.TUNAI) Color.White else Color(0xFF334155)
                            )
                        }
                    }

                    // 1. DROPDOWN PILIH NASABAH PEMINJAM
                    Column {
                        Text(
                            text = "Nasabah Peminjam (Pilih Debitur) *",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, AmarBankBlue, RoundedCornerShape(12.dp))
                                .clickable { isCustomerDropdownExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedCustomer != null) {
                                    Column {
                                        Text(
                                            text = selectedCustomer!!.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "ID: ${selectedCustomer!!.id} • WA: ${selectedCustomer!!.phone}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                } else {
                                    Text("Pilih Nasabah Peminjam...", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmarBankBlue)
                            }

                            DropdownMenu(
                                expanded = isCustomerDropdownExpanded,
                                onDismissRequest = { isCustomerDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                if (customers.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Belum ada nasabah terdaftar", fontSize = 12.sp) },
                                        onClick = { isCustomerDropdownExpanded = false }
                                    )
                                } else {
                                    customers.forEach { cust ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(cust.fullName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                    Text("${cust.id} • ${cust.phone}", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                selectedCustomer = cust
                                                isCustomerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Nama Akad / Judul Pinjaman
                    OutlinedTextField(
                        value = formTitle,
                        onValueChange = { formTitle = it },
                        label = { Text("Nama / Keperluan Kredit") },
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Jika Kredit Barang: Rincian barang & Uang Muka DP
                    if (selectedLoanType == LoanType.BARANG) {
                        OutlinedTextField(
                            value = formItemDesc,
                            onValueChange = { formItemDesc = it },
                            label = { Text("Deskripsi Barang / Jaminan (Opsional)") },
                            placeholder = { Text("Contoh: Honda Beat ESP 2024 / Laptop Asus") },
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp),
                            colors = AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formPrincipalText,
                                onValueChange = { formPrincipalText = it },
                                label = { Text("Harga Barang (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                colors = AmarBankTextFieldDefaults.colors(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = formDpText,
                                onValueChange = { formDpText = it },
                                label = { Text("Uang Muka DP (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                colors = AmarBankTextFieldDefaults.colors(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    } else {
                        // Pinjaman Tunai
                        OutlinedTextField(
                            value = formPrincipalText,
                            onValueChange = { formPrincipalText = it },
                            label = { Text("Nominal Pinjaman Tunai (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                            colors = AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 2. KOTAK NOMINAL RUPIAH & KOTAK PERSEN (%) YANG SALING TERKONEKSI
                    Text(
                        text = "Margin Keuntungan (Ketik Nominal Rp ATAU Ketik Persen %):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Kotak Nominal Rupiah Keuntungan
                        OutlinedTextField(
                            value = formProfitRupiahText,
                            onValueChange = { newRupiahStr ->
                                formProfitRupiahText = newRupiahStr
                                val rupiahVal = newRupiahStr.toDoubleOrNull() ?: 0.0
                                if (netPrincipal > 0) {
                                    val calculatedPercent = (rupiahVal / netPrincipal) * 100.0
                                    formProfitPercentText = String.format(java.util.Locale.US, "%.1f", calculatedPercent)
                                }
                            },
                            label = { Text("Keuntungan (Rp)") },
                            placeholder = { Text("Ketik Rp...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            colors = AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Kotak Persen Keuntungan (%)
                        OutlinedTextField(
                            value = formProfitPercentText,
                            onValueChange = { newPercentStr ->
                                formProfitPercentText = newPercentStr
                                val percentVal = newPercentStr.toDoubleOrNull() ?: 0.0
                                if (netPrincipal > 0) {
                                    val calculatedRupiah = (netPrincipal * percentVal) / 100.0
                                    formProfitRupiahText = calculatedRupiah.toLong().toString()
                                }
                            },
                            label = { Text("Keuntungan (%)") },
                            placeholder = { Text("%") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            colors = AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 3. KOTAK JUMLAH SETORAN RUPIAH & KOTAK DURASI TENOR KECIL (SALING TERKONEKSI)
                    Text(
                        text = "Angsuran & Durasi Tenor:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kotak Jumlah Setoran Rupiah
                        OutlinedTextField(
                            value = formSetoranRupiahText,
                            onValueChange = { newSetoranStr ->
                                formSetoranRupiahText = newSetoranStr
                                val setoranVal = newSetoranStr.toDoubleOrNull() ?: 0.0
                                if (setoranVal > 0 && totalPiutang > 0) {
                                    val autoTenor = ceil(totalPiutang / setoranVal).toInt().coerceAtLeast(1)
                                    formTenorCountText = autoTenor.toString()
                                }
                            },
                            label = { Text("Jumlah Setoran (Rp)") },
                            placeholder = { Text("Setoran per periode") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            colors = AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Kotak Durasi Tenor Kecil
                        OutlinedTextField(
                            value = formTenorCountText,
                            onValueChange = { newTenorStr ->
                                formTenorCountText = newTenorStr
                                val tenorVal = newTenorStr.toIntOrNull() ?: 0
                                if (tenorVal > 0 && totalPiutang > 0) {
                                    val autoSetoran = ceil(totalPiutang / tenorVal).toLong()
                                    formSetoranRupiahText = autoSetoran.toString()
                                }
                            },
                            label = { Text("Tenor") },
                            placeholder = { Text("Jml") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            colors = AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Pilihan Unit Tenor (Bulan/Minggu/Hari)
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .height(56.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .clickable { isTenorDropdownExpanded = true }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formTenorUnit.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = AmarBankBlue
                            )
                            DropdownMenu(
                                expanded = isTenorDropdownExpanded,
                                onDismissRequest = { isTenorDropdownExpanded = false }
                            ) {
                                TenorUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.name, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                        onClick = {
                                            formTenorUnit = unit
                                            isTenorDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Tanggal Mulai
                    OutlinedTextField(
                        value = formStartDate,
                        onValueChange = { formStartDate = it },
                        label = { Text("Tanggal Mulai Akad (YYYY-MM-DD)") },
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Kalkulasi Ringkasan Total Piutang Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pokok Pembiayaan Bersih:", fontSize = 10.sp, color = Color(0xFF475569))
                                Text(Formatters.formatRupiah(netPrincipal), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nominal Keuntungan Ditambahkan:", fontSize = 10.sp, color = Color(0xFF475569))
                                Text(Formatters.formatRupiah(profitRupiah), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmarBankRoyalBlue)
                            }
                            HorizontalDivider(color = Color(0xFFBFDBFE))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL PIUTANG KESELURUHAN:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankBlue)
                                Text(Formatters.formatRupiah(totalPiutang), fontSize = 13.sp, fontWeight = FontWeight.Black, color = AmarBankBlue)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Setoran Cicilan:", fontSize = 10.sp, color = Color(0xFF475569))
                                Text("${Formatters.formatRupiah(setoranRupiah)} x $tenorCount ${formTenorUnit.name.lowercase()}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = AmarBankEmerald)
                            }
                        }
                    }
                }

                // Bottom Save Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Text("Batal", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                Toast.makeText(context, "Silakan pilih nasabah peminjam terlebih dahulu!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (totalPiutang <= 0) {
                                Toast.makeText(context, "Total piutang harus lebih dari 0!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newLoan = Loan(
                                id = "LOAN-${System.currentTimeMillis().toString().takeLast(6)}",
                                customerId = selectedCustomer!!.id,
                                customerName = selectedCustomer!!.fullName,
                                type = selectedLoanType,
                                itemDescription = if (selectedLoanType == LoanType.BARANG) formItemDesc.ifBlank { formTitle } else null,
                                loanTitle = formTitle.ifBlank { "Akad Pembiayaan" },
                                principalAmount = netPrincipal,
                                dpAmount = dp,
                                marginPercentage = formProfitPercentText.toDoubleOrNull() ?: 0.0,
                                totalBill = totalPiutang,
                                totalPaid = 0.0,
                                remainingBalance = totalPiutang,
                                tenorCount = tenorCount,
                                tenorUnit = formTenorUnit,
                                installmentPerPeriod = setoranRupiah,
                                startDate = formStartDate,
                                dueDate = finalDueDate,
                                nextDueDate = firstDueDate,
                                status = LoanStatus.LANCAR
                            )

                            onSaveLoan(newLoan)
                            Toast.makeText(context, "Akad Kredit Berhasil Diterbitkan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.5f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terbitkan Akad", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
