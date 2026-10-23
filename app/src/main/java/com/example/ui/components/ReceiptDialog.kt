package com.example.ui.components

import android.content.Context
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
import com.example.model.Payment
import com.example.model.PaymentMethod
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankEmeraldLight
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters

@Composable
fun ReceiptDialog(
    payment: Payment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    val receiptSummaryText = remember(payment) {
        val isLunas = payment.remainingAfter == 0.0
        """
        *KWITANSI DIGITAL RESMI - AMAR BANK*
        Nomor Faktur : ${payment.invoiceNo}
        Tanggal & Jam: ${Formatters.formatDateTimeIndo(payment.date)}
        --------------------------------------
        ID Nasabah   : ${payment.customerId}
        Nama         : ${payment.customerName}
        Akad Kredit  : ${payment.loanTitle}
        Angsuran     : Ke-${payment.installmentNumber} dari ${payment.totalInstallments}
        Jumlah Bayar : ${Formatters.formatRupiah(payment.amount)}
        Metode       : ${payment.paymentMethod.name} (${payment.bankOrWalletDetail ?: "-"})
        Sisa Piutang : ${Formatters.formatRupiah(payment.remainingAfter)}
        Status       : ${if (isLunas) "LUNAS TOTAL (100%)" else "CICILAN BERJALAN"}
        Verifikasi   : ${payment.verifiedBy}
        --------------------------------------
        Terima kasih atas pembayaran tepat waktu Anda.
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
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AmarBankEmerald, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "E-RECEIPT RESMI AMAR BANK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AmarBankBlue
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Gray
                        )
                    }
                }

                // Printable / Visual Receipt Body
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
                                    text = "FAKTUR LUNAS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmarBankRoyalBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = payment.invoiceNo,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = Formatters.formatDateTimeIndo(payment.date),
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
                                    text = "Penerima Tagihan",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = payment.customerId,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AmarBankRoyalBlue
                                )
                            }
                            Text(
                                text = payment.customerName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = payment.loanTitle,
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail Items
                    ReceiptRow(label = "Angsuran Periode", value = "Ke-${payment.installmentNumber} dari ${payment.totalInstallments}")
                    ReceiptRow(
                        label = "Metode Pembayaran",
                        value = "${when (payment.paymentMethod) {
                            PaymentMethod.TUNAI -> "Kas Tunai"
                            PaymentMethod.NON_TUNAI -> "Non Tunai"
                            PaymentMethod.TRANSFER_BANK -> "Transfer Bank"
                            PaymentMethod.QRIS -> "QRIS Dinamis"
                            PaymentMethod.E_WALLET -> "E-Wallet"
                        }} ${if (!payment.bankOrWalletDetail.isNullOrBlank()) "(${payment.bankOrWalletDetail})" else ""}"
                    )
                    if (!payment.referenceNumber.isNullOrBlank()) {
                        ReceiptRow(label = "No. Referensi", value = payment.referenceNumber)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Highlight Jumlah Bayar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nominal Dibayarkan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmarBankBlue
                            )
                            Text(
                                text = Formatters.formatRupiah(payment.amount),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = AmarBankRoyalBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptRow(label = "Sisa Piutang Berjalan", value = Formatters.formatRupiah(payment.remainingAfter), isBold = true)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verification & Official Stamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = AmarBankEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Terverifikasi Sistem",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmarBankEmerald
                                )
                            }
                            Text(
                                text = "Petugas: ${payment.verifiedBy}",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }

                        // Stempel Resmi Amar Bank
                        Box(
                            modifier = Modifier
                                .rotate(-6f)
                                .border(1.5.dp, AmarBankEmerald, RoundedCornerShape(8.dp))
                                .background(AmarBankEmeraldLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AMAR BANK RESMI",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmarBankEmerald,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (payment.remainingAfter == 0.0) "LUNAS TOTAL" else "DITERIMA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmarBankEmerald
                                )
                                Text(
                                    text = "KASIR AMAR",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AmarBankEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Simpan kwitansi digital ini sebagai bukti sah pembayaran angsuran Amar Bank.",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Buttons (WhatsApp & Copy)
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
                                val pdfFile = com.example.utils.PdfExporter.generateReceiptPdf(
                                    context,
                                    payment,
                                    com.example.model.SystemSettings()
                                )
                                if (pdfFile != null) {
                                    Toast.makeText(context, "Menyiapkan file PDF struk...", Toast.LENGTH_SHORT).show()
                                    com.example.utils.PdfExporter.sharePdfToWhatsApp(
                                        context,
                                        pdfFile,
                                        payment.customerPhone.ifBlank { "081234567890" },
                                        receiptSummaryText
                                    )
                                } else {
                                    val url = Formatters.createWhatsAppUrl(
                                        payment.customerPhone.ifBlank { "081234567890" },
                                        receiptSummaryText
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Kirim PDF ke WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(receiptSummaryText))
                                isCopied = true
                                Toast.makeText(context, "Kwitansi berhasil disalin!", Toast.LENGTH_SHORT).show()
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
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF0F172A)
        )
    }
}
