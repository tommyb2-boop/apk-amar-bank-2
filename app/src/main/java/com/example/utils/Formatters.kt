package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatRupiah(value: Double): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            formatter.maximumFractionDigits = 0
            formatter.format(value)
        } catch (e: Exception) {
            "Rp " + value.toLong().toString()
        }
    }

    fun formatRupiah(value: Long): String {
        return formatRupiah(value.toDouble())
    }

    fun formatDateIndo(dateStr: String): String {
        if (dateStr.isBlank()) return "-"
        return try {
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            )
            var parsedDate: Date? = null
            for (fmt in inputFormats) {
                try {
                    parsedDate = fmt.parse(dateStr)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }
            if (parsedDate != null) {
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
                outputFormat.format(parsedDate)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatDateTimeIndo(dateStr: String): String {
        if (dateStr.isBlank()) return "-"
        return try {
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            )
            var parsedDate: Date? = null
            for (fmt in inputFormats) {
                try {
                    parsedDate = fmt.parse(dateStr)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }
            if (parsedDate != null) {
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
                outputFormat.format(parsedDate)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun cleanPhoneNumber(phone: String): String {
        var cleaned = phone.replace(Regex("\\D"), "")
        if (cleaned.startsWith("0")) {
            cleaned = "62" + cleaned.substring(1)
        } else if (!cleaned.startsWith("62")) {
            cleaned = "62" + cleaned
        }
        return cleaned
    }

    fun createWhatsAppUrl(phone: String, message: String): String {
        val cleanPhone = cleanPhoneNumber(phone)
        val encodedMsg = java.net.URLEncoder.encode(message, "UTF-8")
        return "https://wa.me/$cleanPhone?text=$encodedMsg"
    }

    fun getDaysDifference(targetDateStr: String): Int {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val target = fmt.parse(targetDateStr) ?: return 999
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val targetCal = Calendar.getInstance().apply {
                time = target
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMs = targetCal.timeInMillis - todayCal.timeInMillis
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            999
        }
    }

    fun calculateDueDate(startDateStr: String, tenorCount: Int, tenorUnit: String): String {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val start = fmt.parse(startDateStr) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = start
                when (tenorUnit.uppercase(Locale.US)) {
                    "BULAN" -> add(Calendar.MONTH, tenorCount)
                    "MINGGU" -> add(Calendar.DAY_OF_YEAR, tenorCount * 7)
                    "HARI" -> add(Calendar.DAY_OF_YEAR, tenorCount)
                    else -> add(Calendar.MONTH, tenorCount)
                }
            }
            fmt.format(cal.time)
        } catch (e: Exception) {
            startDateStr
        }
    }

    fun calculateNextDueDate(startDateStr: String, tenorUnit: String, installmentIndex: Int): String {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val start = fmt.parse(startDateStr) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = start
                when (tenorUnit.uppercase(Locale.US)) {
                    "BULAN" -> add(Calendar.MONTH, installmentIndex)
                    "MINGGU" -> add(Calendar.DAY_OF_YEAR, installmentIndex * 7)
                    "HARI" -> add(Calendar.DAY_OF_YEAR, installmentIndex)
                    else -> add(Calendar.MONTH, installmentIndex)
                }
            }
            fmt.format(cal.time)
        } catch (e: Exception) {
            startDateStr
        }
    }

    fun getCurrentIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return sdf.format(Date())
    }

    fun getCurrentDateYmd(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
