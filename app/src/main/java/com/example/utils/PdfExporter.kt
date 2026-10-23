package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.Payment
import com.example.model.SystemSettings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateCustomerPortfolioPdf(
        context: Context,
        customer: Customer,
        loans: List<Loan>,
        payments: List<Payment>,
        settings: SystemSettings
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842 // Standard A4 points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = Color.DKGRAY
            }
            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(13, 71, 161) // Banking Blue
            }
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val boldPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }

            var yPos = 40f

            // 1. Kop Resmi Amar Bank
            paint.color = Color.rgb(13, 71, 161)
            canvas.drawRect(40f, yPos, 65f, yPos + 25f, paint)
            canvas.drawText("AMAR BANK", 75f, yPos + 18f, titlePaint)
            
            paint.textSize = 8f
            paint.color = Color.GRAY
            canvas.drawText("KREDIT & PIUTANG NASABAH", 75f, yPos + 28f, paint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(settings.branchName, (pageWidth - 40).toFloat(), yPos + 10f, textPaint)
            canvas.drawText("${settings.branchAddress}, ${settings.cityName}", (pageWidth - 40).toFloat(), yPos + 22f, textPaint)
            canvas.drawText("Telp: ${settings.branchPhone} | WA: ${settings.officialWhatsApp}", (pageWidth - 40).toFloat(), yPos + 34f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            yPos += 50f
            paint.strokeWidth = 1.5f
            paint.color = Color.rgb(13, 71, 161)
            canvas.drawLine(40f, yPos, (pageWidth - 40).toFloat(), yPos, paint)

            yPos += 20f
            // 2. Judul Dokumen
            headerPaint.textAlign = Paint.Align.CENTER
            headerPaint.textSize = 13f
            canvas.drawText("LEMBAR PORTOFOLIO & REKAPITULASI PIUTANG NASABAH", (pageWidth / 2).toFloat(), yPos, headerPaint)
            headerPaint.textAlign = Paint.Align.LEFT
            headerPaint.textSize = 11f

            yPos += 14f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Tanggal Cetak: ${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())}", (pageWidth / 2).toFloat(), yPos, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            yPos += 25f
            // 3. Data Identitas Nasabah Box
            paint.color = Color.rgb(241, 245, 249) // Slate 100
            canvas.drawRoundRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 55f, 8f, 8f, paint)

            canvas.drawText("ID Nasabah  : ${customer.id}", 55f, yPos + 18f, boldPaint)
            canvas.drawText("Nama Lengkap: ${customer.fullName}", 55f, yPos + 32f, boldPaint)
            canvas.drawText("No. WhatsApp: ${customer.phone}", 55f, yPos + 46f, textPaint)

            canvas.drawText("Alamat : ${customer.address.take(45)}", 300f, yPos + 18f, textPaint)
            canvas.drawText("Catatan: ${customer.notes ?: "-"}", 300f, yPos + 32f, textPaint)

            yPos += 70f
            // 4. Ringkasan Keuangan Nasabah
            val myTotalPlafon = loans.sumOf { it.totalBill }
            val myTotalPaid = loans.sumOf { it.totalPaid }
            val myTotalRemaining = loans.sumOf { it.remainingBalance }

            val colW = (pageWidth - 80) / 3f
            // Box 1: Total Plafon
            paint.color = Color.rgb(238, 242, 255)
            canvas.drawRoundRect(40f, yPos, 40f + colW - 5f, yPos + 40f, 6f, 6f, paint)
            textPaint.color = Color.rgb(67, 56, 202)
            canvas.drawText("TOTAL PIUTANG", 50f, yPos + 15f, textPaint)
            canvas.drawText(Formatters.formatRupiah(myTotalPlafon), 50f, yPos + 30f, boldPaint)

            // Box 2: Total Terbayar
            paint.color = Color.rgb(236, 253, 245)
            canvas.drawRoundRect(40f + colW, yPos, 40f + 2 * colW - 5f, yPos + 40f, 6f, 6f, paint)
            textPaint.color = Color.rgb(5, 150, 105)
            canvas.drawText("TOTAL TERBAYAR", 50f + colW, yPos + 15f, textPaint)
            canvas.drawText(Formatters.formatRupiah(myTotalPaid), 50f + colW, yPos + 30f, boldPaint)

            // Box 3: Sisa Piutang
            paint.color = Color.rgb(254, 243, 199)
            canvas.drawRoundRect(40f + 2 * colW, yPos, (pageWidth - 40).toFloat(), yPos + 40f, 6f, 6f, paint)
            textPaint.color = Color.rgb(180, 83, 9)
            canvas.drawText("SISA PIUTANG", 50f + 2 * colW, yPos + 15f, textPaint)
            canvas.drawText(Formatters.formatRupiah(myTotalRemaining), 50f + 2 * colW, yPos + 30f, boldPaint)

            textPaint.color = Color.DKGRAY

            yPos += 60f
            // 5. Tabel Akad Kredit Nasabah
            canvas.drawText("RINCIAN AKAD KREDIT / PINJAMAN:", 40f, yPos, headerPaint)
            yPos += 15f

            // Table Header
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 20f, paint)
            boldPaint.textSize = 9f
            canvas.drawText("ID Akad", 45f, yPos + 14f, boldPaint)
            canvas.drawText("Nama Pinjaman", 130f, yPos + 14f, boldPaint)
            canvas.drawText("Plafon", 270f, yPos + 14f, boldPaint)
            canvas.drawText("Angsuran", 355f, yPos + 14f, boldPaint)
            canvas.drawText("Sisa Piutang", 440f, yPos + 14f, boldPaint)
            canvas.drawText("Status", 515f, yPos + 14f, boldPaint)

            yPos += 20f
            textPaint.textSize = 8.5f
            if (loans.isEmpty()) {
                canvas.drawText("Belum ada riwayat akad kredit.", 45f, yPos + 14f, textPaint)
                yPos += 20f
            } else {
                loans.forEach { loan ->
                    canvas.drawText(loan.id, 45f, yPos + 14f, textPaint)
                    canvas.drawText(loan.loanTitle.take(22), 130f, yPos + 14f, textPaint)
                    canvas.drawText(Formatters.formatRupiah(loan.totalBill), 270f, yPos + 14f, textPaint)
                    canvas.drawText(Formatters.formatRupiah(loan.installmentPerPeriod), 355f, yPos + 14f, textPaint)
                    canvas.drawText(Formatters.formatRupiah(loan.remainingBalance), 440f, yPos + 14f, textPaint)
                    canvas.drawText(loan.status.name, 515f, yPos + 14f, textPaint)

                    paint.color = Color.rgb(241, 245, 249)
                    canvas.drawLine(40f, yPos + 18f, (pageWidth - 40).toFloat(), yPos + 18f, paint)
                    yPos += 20f
                }
            }

            yPos += 20f
            // 6. Tabel Riwayat Pembayaran Masuk
            canvas.drawText("RIWAYAT PEMBAYARAN ANGSURAN TERAKHIR:", 40f, yPos, headerPaint)
            yPos += 15f

            paint.color = Color.rgb(226, 232, 240)
            canvas.drawRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 20f, paint)
            canvas.drawText("No. Faktur", 45f, yPos + 14f, boldPaint)
            canvas.drawText("Tanggal", 140f, yPos + 14f, boldPaint)
            canvas.drawText("Angsuran", 250f, yPos + 14f, boldPaint)
            canvas.drawText("Metode", 330f, yPos + 14f, boldPaint)
            canvas.drawText("Nominal Bayar", 420f, yPos + 14f, boldPaint)
            canvas.drawText("Sisa", 505f, yPos + 14f, boldPaint)

            yPos += 20f
            if (payments.isEmpty()) {
                canvas.drawText("Belum ada mutasi pembayaran.", 45f, yPos + 14f, textPaint)
                yPos += 20f
            } else {
                payments.take(8).forEach { pay ->
                    canvas.drawText(pay.invoiceNo, 45f, yPos + 14f, textPaint)
                    canvas.drawText(Formatters.formatDateIndo(pay.date).take(18), 140f, yPos + 14f, textPaint)
                    canvas.drawText("Ke-${pay.installmentNumber}", 250f, yPos + 14f, textPaint)
                    canvas.drawText(pay.paymentMethod.name, 330f, yPos + 14f, textPaint)
                    canvas.drawText(Formatters.formatRupiah(pay.amount), 420f, yPos + 14f, textPaint)
                    canvas.drawText(Formatters.formatRupiah(pay.remainingAfter), 505f, yPos + 14f, textPaint)

                    paint.color = Color.rgb(241, 245, 249)
                    canvas.drawLine(40f, yPos + 18f, (pageWidth - 40).toFloat(), yPos + 18f, paint)
                    yPos += 20f
                }
            }

            // 7. Sign-off Footer
            val signY = (pageHeight - 110).toFloat()
            textPaint.textSize = 9f
            canvas.drawText("Dokumen dicetak resmi oleh Amar Bank Cloud System", 40f, signY + 40f, textPaint)

            val rightAlignX = (pageWidth - 160).toFloat()
            canvas.drawText("${settings.cityName}, ${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())}", rightAlignX, signY, textPaint)
            canvas.drawText("Penanggung Jawab Kredit,", rightAlignX, signY + 14f, boldPaint)
            
            paint.color = Color.LTGRAY
            canvas.drawLine(rightAlignX, signY + 50f, (pageWidth - 40).toFloat(), signY + 50f, paint)

            canvas.drawText(settings.responsibleName, rightAlignX, signY + 62f, boldPaint)
            canvas.drawText(settings.responsibleTitle, rightAlignX, signY + 74f, textPaint)

            pdfDocument.finishPage(page)

            // Save to internal Documents folder
            val docsDir = context.getExternalFilesDir("Documents") ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()
            val fileName = "AmarBank_Portofolio_${customer.id}_${System.currentTimeMillis()}.pdf"
            val file = File(docsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateRecapReportPdf(
        context: Context,
        customers: List<Customer>,
        loans: List<Loan>,
        payments: List<Payment>,
        settings: SystemSettings,
        periodLabel: String
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9f
                color = Color.DKGRAY
            }
            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(13, 71, 161)
            }
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val boldPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }

            var yPos = 40f

            // Kop
            paint.color = Color.rgb(13, 71, 161)
            canvas.drawRect(40f, yPos, 65f, yPos + 25f, paint)
            canvas.drawText("AMAR BANK", 75f, yPos + 18f, titlePaint)
            paint.textSize = 8f
            paint.color = Color.GRAY
            canvas.drawText("SISTEM REKAPITULASI RESMI", 75f, yPos + 28f, paint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(settings.branchName, (pageWidth - 40).toFloat(), yPos + 10f, textPaint)
            canvas.drawText("${settings.branchAddress}, ${settings.cityName}", (pageWidth - 40).toFloat(), yPos + 22f, textPaint)
            canvas.drawText("Telp: ${settings.branchPhone}", (pageWidth - 40).toFloat(), yPos + 34f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            yPos += 50f
            paint.strokeWidth = 1.5f
            paint.color = Color.rgb(13, 71, 161)
            canvas.drawLine(40f, yPos, (pageWidth - 40).toFloat(), yPos, paint)

            yPos += 20f
            headerPaint.textAlign = Paint.Align.CENTER
            headerPaint.textSize = 13f
            canvas.drawText("LAPORAN REKAPITULASI PIUTANG & ANGSURAN", (pageWidth / 2).toFloat(), yPos, headerPaint)
            headerPaint.textAlign = Paint.Align.LEFT

            yPos += 14f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Periode: $periodLabel | Dicetak: ${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())}", (pageWidth / 2).toFloat(), yPos, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            yPos += 25f
            val totalPlafon = loans.sumOf { it.totalBill }
            val totalTerkumpul = payments.sumOf { it.amount }
            val totalSisa = loans.sumOf { it.remainingBalance }

            val colW = (pageWidth - 80) / 3f
            paint.color = Color.rgb(238, 242, 255)
            canvas.drawRoundRect(40f, yPos, 40f + colW - 5f, yPos + 40f, 6f, 6f, paint)
            textPaint.color = Color.rgb(67, 56, 202)
            canvas.drawText("TOTAL PLAFON", 50f, yPos + 15f, textPaint)
            canvas.drawText(Formatters.formatRupiah(totalPlafon), 50f, yPos + 30f, boldPaint)

            paint.color = Color.rgb(236, 253, 245)
            canvas.drawRoundRect(40f + colW, yPos, 40f + 2 * colW - 5f, yPos + 40f, 6f, 6f, paint)
            textPaint.color = Color.rgb(5, 150, 105)
            canvas.drawText("TOTAL ANGSURAN MASUK", 50f + colW, yPos + 15f, textPaint)
            canvas.drawText(Formatters.formatRupiah(totalTerkumpul), 50f + colW, yPos + 30f, boldPaint)

            paint.color = Color.rgb(254, 243, 199)
            canvas.drawRoundRect(40f + 2 * colW, yPos, (pageWidth - 40).toFloat(), yPos + 40f, 6f, 6f, paint)
            textPaint.color = Color.rgb(180, 83, 9)
            canvas.drawText("SISA PIUTANG BERJALAN", 50f + 2 * colW, yPos + 15f, textPaint)
            canvas.drawText(Formatters.formatRupiah(totalSisa), 50f + 2 * colW, yPos + 30f, boldPaint)

            textPaint.color = Color.DKGRAY

            yPos += 55f
            canvas.drawText("1. PORTOFOLIO PIUTANG AKAD KREDIT (${loans.size} AKAD):", 40f, yPos, headerPaint)
            yPos += 14f

            paint.color = Color.rgb(226, 232, 240)
            canvas.drawRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 18f, paint)
            canvas.drawText("ID Akad", 45f, yPos + 13f, boldPaint)
            canvas.drawText("Nasabah", 130f, yPos + 13f, boldPaint)
            canvas.drawText("Plafon", 250f, yPos + 13f, boldPaint)
            canvas.drawText("Terkumpul", 335f, yPos + 13f, boldPaint)
            canvas.drawText("Sisa Piutang", 420f, yPos + 13f, boldPaint)
            canvas.drawText("Status", 510f, yPos + 13f, boldPaint)

            yPos += 18f
            loans.take(12).forEach { loan ->
                canvas.drawText(loan.id, 45f, yPos + 13f, textPaint)
                canvas.drawText(loan.customerName.take(18), 130f, yPos + 13f, textPaint)
                canvas.drawText(Formatters.formatRupiah(loan.totalBill), 250f, yPos + 13f, textPaint)
                canvas.drawText(Formatters.formatRupiah(loan.totalPaid), 335f, yPos + 13f, textPaint)
                canvas.drawText(Formatters.formatRupiah(loan.remainingBalance), 420f, yPos + 13f, textPaint)
                canvas.drawText(loan.status.name, 510f, yPos + 13f, textPaint)

                paint.color = Color.rgb(241, 245, 249)
                canvas.drawLine(40f, yPos + 16f, (pageWidth - 40).toFloat(), yPos + 16f, paint)
                yPos += 18f
            }

            yPos += 15f
            canvas.drawText("2. MUTASI PEMBAYARAN MASUK (${payments.size} TRANSAKSI):", 40f, yPos, headerPaint)
            yPos += 14f

            paint.color = Color.rgb(226, 232, 240)
            canvas.drawRect(40f, yPos, (pageWidth - 40).toFloat(), yPos + 18f, paint)
            canvas.drawText("No Faktur", 45f, yPos + 13f, boldPaint)
            canvas.drawText("Nasabah", 140f, yPos + 13f, boldPaint)
            canvas.drawText("Tanggal", 260f, yPos + 13f, boldPaint)
            canvas.drawText("Metode", 355f, yPos + 13f, boldPaint)
            canvas.drawText("Nominal Bayar", 450f, yPos + 13f, boldPaint)

            yPos += 18f
            payments.take(8).forEach { pay ->
                canvas.drawText(pay.invoiceNo, 45f, yPos + 13f, textPaint)
                canvas.drawText(pay.customerName.take(18), 140f, yPos + 13f, textPaint)
                canvas.drawText(Formatters.formatDateIndo(pay.date).take(18), 260f, yPos + 13f, textPaint)
                canvas.drawText(pay.paymentMethod.name, 355f, yPos + 13f, textPaint)
                canvas.drawText(Formatters.formatRupiah(pay.amount), 450f, yPos + 13f, textPaint)

                paint.color = Color.rgb(241, 245, 249)
                canvas.drawLine(40f, yPos + 16f, (pageWidth - 40).toFloat(), yPos + 16f, paint)
                yPos += 18f
            }

            // Sign-off
            val signY = (pageHeight - 110).toFloat()
            val rightAlignX = (pageWidth - 160).toFloat()
            canvas.drawText("${settings.cityName}, ${Formatters.formatDateIndo(Formatters.getCurrentIsoDate())}", rightAlignX, signY, textPaint)
            canvas.drawText("Penanggung Jawab Piutang,", rightAlignX, signY + 14f, boldPaint)

            paint.color = Color.LTGRAY
            canvas.drawLine(rightAlignX, signY + 50f, (pageWidth - 40).toFloat(), signY + 50f, paint)

            canvas.drawText(settings.responsibleName, rightAlignX, signY + 62f, boldPaint)
            canvas.drawText(settings.responsibleTitle, rightAlignX, signY + 74f, textPaint)

            pdfDocument.finishPage(page)

            val docsDir = context.getExternalFilesDir("Documents") ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()
            val fileName = "AmarBank_Rekapitulasi_${System.currentTimeMillis()}.pdf"
            val file = File(docsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateReceiptPdf(
        context: Context,
        payment: Payment,
        settings: SystemSettings
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 400
            val pageHeight = 600 // Compact Receipt Size
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(13, 71, 161)
            }
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = Color.DKGRAY
            }
            val boldPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }

            var yPos = 35f

            // Kop Amar Bank
            paint.color = Color.rgb(13, 71, 161)
            canvas.drawRect(30f, yPos, 50f, yPos + 20f, paint)
            canvas.drawText("AMAR BANK", 58f, yPos + 15f, titlePaint)
            
            yPos += 26f
            textPaint.textSize = 8.5f
            canvas.drawText("${settings.branchName} • ${settings.cityName}", 30f, yPos, textPaint)
            yPos += 12f
            canvas.drawText("Telp: ${settings.branchPhone} • WA: ${settings.officialWhatsApp}", 30f, yPos, textPaint)

            yPos += 12f
            paint.color = Color.rgb(13, 71, 161)
            paint.strokeWidth = 1.5f
            canvas.drawLine(30f, yPos, (pageWidth - 30).toFloat(), yPos, paint)

            yPos += 22f
            headerPaint.textAlign = Paint.Align.CENTER
            headerPaint.textSize = 12f
            canvas.drawText("BUKTI PEMBAYARAN ANGSURAN RESMI", (pageWidth / 2).toFloat(), yPos, headerPaint)
            headerPaint.textAlign = Paint.Align.LEFT

            yPos += 14f
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 9f
            canvas.drawText("No. Faktur: ${payment.invoiceNo}", (pageWidth / 2).toFloat(), yPos, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 10f

            yPos += 22f
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(30f, yPos, (pageWidth - 30).toFloat(), yPos + 160f, paint)

            val leftCol = 42f
            val rightCol = 160f
            var rowY = yPos + 22f

            fun drawRow(label: String, value: String, isValueBold: Boolean = false) {
                canvas.drawText(label, leftCol, rowY, textPaint)
                canvas.drawText(":", 150f, rowY, textPaint)
                canvas.drawText(value, rightCol, rowY, if (isValueBold) boldPaint else textPaint)
                rowY += 19f
            }

            drawRow("Tanggal Bayar", Formatters.formatDateIndo(payment.date))
            drawRow("Nama Nasabah", payment.customerName)
            drawRow("ID Nasabah", payment.customerId)
            drawRow("Akad Pinjaman", payment.loanTitle)
            drawRow("Metode Bayar", payment.paymentMethod.name)
            drawRow("Jumlah Bayar", Formatters.formatRupiah(payment.amount), isValueBold = true)
            drawRow("Sisa Piutang", Formatters.formatRupiah(payment.remainingAfter), isValueBold = true)

            // Stamp Box
            yPos += 175f
            paint.color = Color.rgb(236, 253, 245)
            canvas.drawRoundRect(30f, yPos, 160f, yPos + 48f, 10f, 10f, paint)
            paint.color = Color.rgb(16, 185, 129)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f
            canvas.drawRoundRect(30f, yPos, 160f, yPos + 48f, 10f, 10f, paint)
            paint.style = Paint.Style.FILL

            val stampPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(5, 150, 105)
            }
            canvas.drawText("TERVERIFIKASI SAH", 38f, yPos + 20f, stampPaint)
            stampPaint.textSize = 8.5f
            canvas.drawText("AMAR BANK SYSTEM", 38f, yPos + 34f, stampPaint)

            // Sign-off
            val rightSignX = 220f
            canvas.drawText("${settings.cityName}, ${Formatters.formatDateIndo(payment.date)}", rightSignX, yPos + 12f, textPaint)
            canvas.drawText("Kasir / Admin Penerima,", rightSignX, yPos + 25f, textPaint)
            canvas.drawText(settings.responsibleName, rightSignX, yPos + 56f, boldPaint)

            yPos += 75f
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 8f
            canvas.drawText("Simpan struk ini sebagai bukti pembayaran angsuran yang sah.", (pageWidth / 2).toFloat(), yPos, textPaint)

            pdfDocument.finishPage(page)

            val docsDir = context.getExternalFilesDir("Documents") ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()
            val fileName = "Struk_${payment.invoiceNo}.pdf"
            val file = File(docsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdfToWhatsApp(context: Context, file: File, phone: String, messageText: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val cleanPhone = Formatters.cleanPhoneNumber(phone)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                putExtra("jid", "$cleanPhone@s.whatsapp.net")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Prefer WhatsApp directly
            val pm = context.packageManager
            val waIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (waIntent.resolveActivity(pm) != null) {
                context.startActivity(waIntent)
            } else {
                context.startActivity(Intent.createChooser(shareIntent, "Kirim Struk PDF via WhatsApp"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Membuka WhatsApp...", Toast.LENGTH_SHORT).show()
        }
    }

    fun openOrSharePdf(context: Context, file: File, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if there's an app to open PDF, else share
            if (viewIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(viewIntent)
            } else {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, title))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "File PDF tersimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}
