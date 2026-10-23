package com.example.utils

import android.content.Context
import android.content.Intent
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

object ExcelExporter {

    /**
     * Generates a multi-sheet Microsoft Excel (.xls) file using XML Spreadsheet 2003 format.
     * Fully compatible with MS Excel, Google Sheets, WPS Office, and Android Office viewers.
     */
    fun generateExcelBackup(
        context: Context,
        customers: List<Customer>,
        loans: List<Loan>,
        payments: List<Payment>,
        settings: SystemSettings
    ): File? {
        return try {
            val docsDir = context.getExternalFilesDir("Documents") ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(docsDir, "AmarBank_Backup_$timeStamp.xls")

            val sb = StringBuilder()
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
            sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
            sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
            sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
            sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
            sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")

            // Styles
            sb.append("<Styles>\n")
            sb.append(" <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
            sb.append("  <Alignment ss:Vertical=\"Bottom\"/>\n")
            sb.append("  <Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Color=\"#000000\"/>\n")
            sb.append(" </Style>\n")
            sb.append(" <Style ss:ID=\"HeaderStyle\">\n")
            sb.append("  <Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/>\n")
            sb.append("  <Interior ss:Color=\"#0D47A1\" ss:Pattern=\"Solid\"/>\n")
            sb.append("  <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
            sb.append(" </Style>\n")
            sb.append(" <Style ss:ID=\"TitleStyle\">\n")
            sb.append("  <Font ss:FontName=\"Calibri\" ss:Size=\"14\" ss:Bold=\"1\" ss:Color=\"#0D47A1\"/>\n")
            sb.append(" </Style>\n")
            sb.append("</Styles>\n")

            // ================== SHEET 1: NASABAH ==================
            sb.append("<Worksheet ss:Name=\"Data Nasabah\">\n")
            sb.append("<Table>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"150\"/>\n")
            sb.append("<Column ss:Width=\"110\"/>\n")
            sb.append("<Column ss:Width=\"180\"/>\n")
            sb.append("<Column ss:Width=\"60\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")

            // Header row
            sb.append("<Row ss:StyleID=\"HeaderStyle\">\n")
            sb.append("<Cell><Data ss:Type=\"String\">ID Nasabah</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Nama Lengkap</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Nomor WhatsApp</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Alamat</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">PIN</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Tgl Daftar</Data></Cell>\n")
            sb.append("</Row>\n")

            for (c in customers) {
                sb.append("<Row>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.id)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.fullName)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.phone)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.address)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.pin)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.createdAt)}</Data></Cell>\n")
                sb.append("</Row>\n")
            }
            sb.append("</Table>\n")
            sb.append("</Worksheet>\n")

            // ================== SHEET 2: AKAD KREDIT ==================
            sb.append("<Worksheet ss:Name=\"Akad Pinjaman\">\n")
            sb.append("<Table>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"140\"/>\n")
            sb.append("<Column ss:Width=\"80\"/>\n")
            sb.append("<Column ss:Width=\"150\"/>\n")
            sb.append("<Column ss:Width=\"100\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"100\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")

            // Header row
            sb.append("<Row ss:StyleID=\"HeaderStyle\">\n")
            sb.append("<Cell><Data ss:Type=\"String\">ID Akad</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">ID Nasabah</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Nama Nasabah</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Jenis Akad</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Nama Barang/Pinjaman</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Total Plafon (Rp)</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Terbayar (Rp)</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Sisa Piutang (Rp)</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Cicilan/Periode</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Jatuh Tempo</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Status</Data></Cell>\n")
            sb.append("</Row>\n")

            for (l in loans) {
                sb.append("<Row>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(l.id)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(l.customerId)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(l.customerName)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${l.type.name}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(l.loanTitle)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"Number\">${l.totalBill}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"Number\">${l.totalPaid}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"Number\">${l.remainingBalance}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"Number\">${l.installmentPerPeriod}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(l.nextDueDate)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${l.status.name}</Data></Cell>\n")
                sb.append("</Row>\n")
            }
            sb.append("</Table>\n")
            sb.append("</Worksheet>\n")

            // ================== SHEET 3: MUTASI PEMBAYARAN ==================
            sb.append("<Worksheet ss:Name=\"Mutasi Pembayaran\">\n")
            sb.append("<Table>\n")
            sb.append("<Column ss:Width=\"100\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"130\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"90\"/>\n")
            sb.append("<Column ss:Width=\"80\"/>\n")
            sb.append("<Column ss:Width=\"100\"/>\n")
            sb.append("<Column ss:Width=\"100\"/>\n")

            // Header row
            sb.append("<Row ss:StyleID=\"HeaderStyle\">\n")
            sb.append("<Cell><Data ss:Type=\"String\">No Faktur</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">ID Akad</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Nama Nasabah</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Tanggal Bayar</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Jumlah (Rp)</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Metode</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Sisa Setelahnya</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">Catatan</Data></Cell>\n")
            sb.append("</Row>\n")

            for (p in payments) {
                sb.append("<Row>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(p.invoiceNo)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(p.loanId)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(p.customerName)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(p.date)}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"Number\">${p.amount}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${p.paymentMethod.name}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"Number\">${p.remainingAfter}</Data></Cell>\n")
                sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(p.notes ?: "")}</Data></Cell>\n")
                sb.append("</Row>\n")
            }
            sb.append("</Table>\n")
            sb.append("</Worksheet>\n")

            sb.append("</Workbook>\n")

            val fos = FileOutputStream(file)
            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun openOrShareExcel(context: Context, file: File, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "File Cadangan Database Amar Bank (.xls): ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "File tersimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareExcelFile(context: Context, file: File) {
        openOrShareExcel(context, file, "Simpan / Bagikan Backup Excel")
    }
}
