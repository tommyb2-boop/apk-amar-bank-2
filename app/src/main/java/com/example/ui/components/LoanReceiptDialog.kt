package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Loan
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankLightBlue
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters

@Composable
fun LoanReceiptDialog(
    loan: Loan,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    val invoiceNo = "AKAD-${loan.id.replace("LN-", "")}"
    val summaryText = remember(loan) {
        """
        *KWITANSI RESMI AKAD PEMBIAYAAN - AMAR BANK*
        Nomor Registrasi : $invoiceNo
        Tanggal Akad     : ${Formatters.formatDateIndo(loan.startDate)}
        ---------------------------------------------
        ID Nasabah    : ${loan.customerId}
        Nama Nasabah  : ${loan.customerName}
        Akad Kredit   : ${loan.loanTitle}
        ${if (!loan.itemDescription.isNullOrBlank()) "Deskripsi     : ${loan.itemDescription}\n" else ""}Tipe          : ${if (loan.type.name == "BARANG") "Kredit Barang" else "Pinjaman Tunai"}
        Pokok Bersih  : ${Formatters.formatRupiah(loan.netPrincipal)} ${if (loan.dpAmount > 0) "(DP: ${Formatters.formatRupiah(loan.dpAmount)})" else ""}
        Margin (%)    : ${loan.marginPercentage}% (+${Formatters.formatRupiah(loan.marginAmount)})
        ---------------------------------------------
        TOTAL PLAFON  : ${Formatters.formatRupiah(loan.totalBill)}
        ANGSURAN      : ${Formatters.formatRupiah(loan.installmentPerPeriod)} / ${loan.tenorUnit.name.lowercase()}
        Tenor         : ${loan.tenorCount} ${loan.tenorUnit.name.lowercase()}
        Jatuh Tempo 1 : ${Formatters.formatDateIndo(loan.nextDueDate)}
        Jatuh Tempo Akhir: ${Formatters.formatDateIndo(loan.dueDate)}
        ---------------------------------------------
        Status Akad   : DISETUJUI & AKTIF RESMI
        Verifikasi    : Kantor Pelayanan Amar Bank
        Selamat atas penerbitan akad pembiayaan Anda!
        Amar Bank - Mitra Keuangan Amanah.
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F2FE))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AmarBankRoyalBlue, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUKTI AKAD RESMI DITERBITKAN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AmarBankBlue
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // Printable Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Kop Resmi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        AmarBankLogo(iconSize = 32.dp, showText = true)
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0F2FE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AKAD SAH",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmarBankRoyalBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = invoiceNo,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = Formatters.formatDateIndo(loan.startDate),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Customer & Akad Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Nasabah Penerima Fasilitas",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = loan.customerId,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AmarBankRoyalBlue
                                )
                            }
                            Text(
                                text = loan.customerName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = loan.loanTitle,
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            if (!loan.itemDescription.isNullOrBlank()) {
                                Text(
                                    text = loan.itemDescription,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail Rows
                    ReceiptRow(label = "Pokok Bersih (Stlh DP)", value = Formatters.formatRupiah(loan.netPrincipal))
                    if (loan.dpAmount > 0) {
                        ReceiptRow(label = "Uang Muka (DP)", value = Formatters.formatRupiah(loan.dpAmount))
                    }
                    ReceiptRow(label = "Margin Keuntungan (${loan.marginPercentage}%)", value = "+${Formatters.formatRupiah(loan.marginAmount)}")

                    Spacer(modifier = Modifier.height(8.dp))

                    // Total Plafon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AmarBankLightBlue, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Plafon Pinjaman", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmarBankBlue)
                            Text(
                                text = Formatters.formatRupiah(loan.totalBill),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = AmarBankRoyalBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptRow(
                        label = "Angsuran / Periode",
                        value = "${Formatters.formatRupiah(loan.installmentPerPeriod)} / ${loan.tenorUnit.name.lowercase()}",
                        isBold = true
                    )
                    ReceiptRow(label = "Jangka Waktu Tenor", value = "${loan.tenorCount} Kali (${loan.tenorUnit.name})")
                    ReceiptRow(label = "Jatuh Tempo Pertama", value = Formatters.formatDateIndo(loan.nextDueDate))
                    ReceiptRow(label = "Jatuh Tempo Akhir", value = Formatters.formatDateIndo(loan.dueDate))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stempel Sah
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = AmarBankRoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Akad Disetujui", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmarBankRoyalBlue)
                            }
                            Text(text = "Petugas Verifikator Amar Bank", fontSize = 9.sp, color = Color.Gray)
                        }

                        Box(
                            modifier = Modifier
                                .rotate(-5f)
                                .border(1.5.dp, AmarBankRoyalBlue, RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "AMAR BANK RESMI", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = AmarBankBlue, letterSpacing = 0.5.sp)
                                Text(text = "AKAD SAH", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmarBankRoyalBlue)
                                Text(text = "DITERBITKAN", fontSize = 7.sp, fontWeight = FontWeight.Medium, color = AmarBankBlue)
                            }
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val url = Formatters.createWhatsAppUrl(
                                    loan.customerPhone.ifBlank { "081234567890" },
                                    summaryText
                                )
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Kirim WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(summaryText))
                                isCopied = true
                                Toast.makeText(context, "Teks bukti akad berhasil disalin!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isCopied) AmarBankEmerald else Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCopied) "Tersalin!" else "Salin Teks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 11.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = Color(0xFF0F172A))
    }
}
