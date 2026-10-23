package com.example.ui.admin

import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.Payment
import com.example.model.SystemSettings
import com.example.ui.components.AmarBankLogo
import com.example.ui.components.AmarBankTextFieldDefaults
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters
import com.example.utils.PdfExporter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportStep {
    SELECT_DATE_RANGE,
    VIEW_PDF_REPORT
}

@Composable
fun ReportDialog(
    customers: List<Customer>,
    loans: List<Loan>,
    payments: List<Payment>,
    systemSettings: SystemSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(ReportStep.SELECT_DATE_RANGE) }
    var selectedPreset by remember { mutableStateOf("THIS_MONTH") }
    var startDateInput by remember { mutableStateOf("2026-10-01") }
    var endDateInput by remember { mutableStateOf("2026-10-31") }
    var periodLabel by remember { mutableStateOf("Bulan Ini (Oktober 2026)") }

    // Filter calculations
    val filteredData = remember(selectedPreset, startDateInput, endDateInput, loans, payments) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var startCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        var endCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }

        when (selectedPreset) {
            "TODAY" -> {
                periodLabel = "Hari Ini (${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())})"
            }
            "7_DAYS" -> {
                startCal.add(Calendar.DAY_OF_YEAR, -7)
                periodLabel = "7 Hari Terakhir"
            }
            "THIS_MONTH" -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                periodLabel = "Bulan Ini"
            }
            "30_DAYS" -> {
                startCal.add(Calendar.DAY_OF_YEAR, -30)
                periodLabel = "30 Hari Terakhir"
            }
            "ALL" -> {
                startCal.set(2020, 0, 1)
                endCal.set(2035, 11, 31)
                periodLabel = "Semua Riwayat Transaksi"
            }
            "CUSTOM" -> {
                try {
                    val sDate = sdf.parse(startDateInput) ?: Date()
                    val eDate = sdf.parse(endDateInput) ?: Date()
                    startCal.time = sDate
                    endCal.time = eDate
                    endCal.set(Calendar.HOUR_OF_DAY, 23)
                    endCal.set(Calendar.MINUTE, 59)
                    periodLabel = "${Formatters.formatDateIndo(startDateInput)} s/d ${Formatters.formatDateIndo(endDateInput)}"
                } catch (_: Exception) {
                    periodLabel = "Rentang Kustom"
                }
            }
        }

        val filteredPayments = payments.filter { p ->
            try {
                val pDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(p.date.take(10)) ?: Date()
                pDate.time in startCal.timeInMillis..endCal.timeInMillis
            } catch (_: Exception) {
                true
            }
        }

        val filteredLoans = loans.filter { l ->
            try {
                val lDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(l.startDate.take(10)) ?: Date()
                lDate.time <= endCal.timeInMillis
            } catch (_: Exception) {
                true
            }
        }

        val totalPlafon = filteredLoans.sumOf { it.totalBill }
        val totalTerkumpul = filteredPayments.sumOf { it.amount }
        val totalSisa = filteredLoans.sumOf { it.remainingBalance }

        Triple(filteredLoans, filteredPayments, Triple(totalPlafon, totalTerkumpul, totalSisa))
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
                // Top Header
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
                                .background(AmarBankBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentStep == ReportStep.SELECT_DATE_RANGE) "Pilih Rentang Tanggal Rekapitulasi" else "Laporan Rekapitulasi PDF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (currentStep == ReportStep.SELECT_DATE_RANGE) "Tentukan periode laporan sebelum membuat dokumen" else "Format resmi bertanda tangan Kota ${systemSettings.cityName}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                if (currentStep == ReportStep.SELECT_DATE_RANGE) {
                    // STEP 1: SELECT DATE RANGE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Pilih Periode Waktu Laporan Rekapitulasi:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )

                        // Preset Range Options
                        val presets = listOf(
                            Pair("THIS_MONTH", "Bulan Ini"),
                            Pair("TODAY", "Hari Ini"),
                            Pair("7_DAYS", "7 Hari Terakhir"),
                            Pair("30_DAYS", "30 Hari Terakhir"),
                            Pair("ALL", "Semua Waktu (Seluruh Data)"),
                            Pair("CUSTOM", "Rentang Tanggal Kustom")
                        )

                        presets.forEach { (key, label) ->
                            val isSelected = selectedPreset == key
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                    .border(1.5.dp, if (isSelected) AmarBankRoyalBlue else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable { selectedPreset = key }
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
                                                .size(16.dp)
                                                .background(if (isSelected) AmarBankRoyalBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                                .border(1.5.dp, if (isSelected) AmarBankRoyalBlue else Color.Gray, RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF334155)
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Date Inputs if CUSTOM is selected
                        if (selectedPreset == "CUSTOM") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Tentukan Tanggal Mulai dan Akhir (YYYY-MM-DD):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    OutlinedTextField(
                                        value = startDateInput,
                                        onValueChange = { startDateInput = it },
                                        label = { Text("Tanggal Mulai") },
                                        placeholder = { Text("Contoh: 2026-10-01") },
                                        singleLine = true,
                                        colors = AmarBankTextFieldDefaults.colors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = endDateInput,
                                        onValueChange = { endDateInput = it },
                                        label = { Text("Tanggal Akhir") },
                                        placeholder = { Text("Contoh: 2026-10-31") },
                                        singleLine = true,
                                        colors = AmarBankTextFieldDefaults.colors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { currentStep = ReportStep.VIEW_PDF_REPORT },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                        ) {
                            Text(text = "Tampilkan Halaman Rekapitulasi PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    // STEP 2: VIEW PDF REPORT & SAVE TO DEVICE
                    val (filteredLoans, filteredPayments, stats) = filteredData
                    val (totalPlafon, totalTerkumpul, totalSisa) = stats

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Kop Resmi Amar Bank
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            AmarBankLogo(iconSize = 34.dp, showText = true)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = systemSettings.branchName, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
                                Text(text = "${systemSettings.branchAddress}, ${systemSettings.cityName}", fontSize = 9.sp, color = Color.Gray)
                                Text(text = "Telp: ${systemSettings.branchPhone}", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 2.dp, color = AmarBankBlue)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "LAPORAN REKAPITULASI PIUTANG & ANGSURAN",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = AmarBankBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Periode: $periodLabel | Cetak: ${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Metrik Finansial
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "TOTAL PLAFON", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmarBankBlue)
                                    Text(text = Formatters.formatRupiah(totalPlafon), fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankRoyalBlue)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFECFDF5), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "ANGSURAN MASUK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AmarBankEmerald)
                                    Text(text = Formatters.formatRupiah(totalTerkumpul), fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankEmerald)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFFFFBEB), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "SISA PIUTANG", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    Text(text = Formatters.formatRupiah(totalSisa), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "1. Portofolio Pinjaman (${filteredLoans.size} Akad):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(4.dp))

                        filteredLoans.take(8).forEach { l ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${l.customerName} (${l.id})", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(text = l.loanTitle, fontSize = 9.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = Formatters.formatRupiah(l.totalBill), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Sisa: ${Formatters.formatRupiah(l.remainingBalance)} (${l.status.name})", fontSize = 9.sp, color = AmarBankBlue)
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "2. Pembayaran Angsuran Masuk (${filteredPayments.size} Transaksi):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(4.dp))

                        filteredPayments.take(6).forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${p.customerName} (${p.invoiceNo})", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(text = "${Formatters.formatDateIndo(p.date)} • ${p.paymentMethod.name}", fontSize = 9.sp, color = Color.Gray)
                                }
                                Text(text = Formatters.formatRupiah(p.amount), fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankEmerald)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sign-off
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${systemSettings.cityName}, ${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())}", fontSize = 9.sp, color = Color.Gray)
                                Text(text = "Penanggung Jawab Kredit,", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(30.dp))
                                Text(text = systemSettings.responsibleName, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
                                Text(text = systemSettings.responsibleTitle, fontSize = 8.5.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Action Buttons: Simpan ke Lokal & Bagikan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val pdfFile = PdfExporter.generateRecapReportPdf(
                                        context, customers, filteredLoans, filteredPayments, systemSettings, periodLabel
                                    )
                                    if (pdfFile != null) {
                                        Toast.makeText(context, "Laporan PDF Berhasil Disimpan ke Dokumen Lokal:\n${pdfFile.name}", Toast.LENGTH_LONG).show()
                                        PdfExporter.openOrSharePdf(context, pdfFile, "Laporan Rekapitulasi Amar Bank - $periodLabel")
                                    } else {
                                        Toast.makeText(context, "Gagal mencetak dokumen PDF.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan PDF ke Perangkat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { currentStep = ReportStep.SELECT_DATE_RANGE },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ubah Rentang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
