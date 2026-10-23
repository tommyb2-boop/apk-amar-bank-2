package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.model.Loan
import com.example.model.LoanStatus
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.utils.Formatters

object DueNotificationHelper {

    private const val CHANNEL_ID = "amar_bank_due_channel"
    private const val CHANNEL_NAME = "Amar Bank - Pengingat Jatuh Tempo Piutang"
    private const val CHANNEL_DESC = "Notifikasi real-time untuk angsuran dan piutang jatuh tempo"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks all loans and generates real-time notifications for loans due in <= 3 days or overdue.
     * Returns list of new NotificationItems generated.
     */
    fun checkAndNotifyDueLoans(
        context: Context,
        loans: List<Loan>,
        existingNotifications: List<NotificationItem>,
        notifiedLoanIds: MutableSet<String>
    ): List<NotificationItem> {
        val newNotifs = mutableListOf<NotificationItem>()
        createNotificationChannel(context)

        for (loan in loans) {
            if (loan.remainingBalance <= 0 || loan.status == LoanStatus.LUNAS) continue

            val daysDiff = Formatters.getDaysDifference(loan.nextDueDate)

            if (daysDiff < 0) {
                // OVERDUE
                val notifKey = "OVERDUE_${loan.id}_${loan.nextDueDate}"
                if (!notifiedLoanIds.contains(notifKey)) {
                    notifiedLoanIds.add(notifKey)
                    val title = "Peringatan Piutang Menunggak!"
                    val message = "Akad ${loan.customerName} (${loan.loanTitle} - ${Formatters.formatRupiah(loan.installmentPerPeriod)}) telah menunggak sejak ${Formatters.formatDateIndo(loan.nextDueDate)}."
                    
                    val item = NotificationItem(
                        id = "NOTIF_${System.currentTimeMillis()}_${loan.id}",
                        title = title,
                        message = message,
                        type = NotificationType.OVERDUE,
                        loanId = loan.id,
                        customerId = loan.customerId,
                        date = Formatters.getCurrentIsoDate(),
                        isRead = false,
                        daysRemaining = daysDiff
                    )
                    newNotifs.add(item)
                    sendSystemNotification(context, loan.id.hashCode() + 100, title, message)
                }
            } else if (daysDiff in 0..3) {
                // DUE SOON (H-0 to H-3)
                val notifKey = "DUE_SOON_${loan.id}_${loan.nextDueDate}_H$daysDiff"
                if (!notifiedLoanIds.contains(notifKey)) {
                    notifiedLoanIds.add(notifKey)
                    val daysLabel = if (daysDiff == 0) "Hari Ini" else "H-$daysDiff"
                    val title = "Jatuh Tempo Mendekat ($daysLabel)"
                    val message = "Angsuran ${loan.customerName} (${loan.loanTitle} - ${Formatters.formatRupiah(loan.installmentPerPeriod)}) jatuh tempo pada ${Formatters.formatDateIndo(loan.nextDueDate)}."
                    
                    val item = NotificationItem(
                        id = "NOTIF_${System.currentTimeMillis()}_${loan.id}",
                        title = title,
                        message = message,
                        type = NotificationType.DUE_SOON,
                        loanId = loan.id,
                        customerId = loan.customerId,
                        date = Formatters.getCurrentIsoDate(),
                        isRead = false,
                        daysRemaining = daysDiff
                    )
                    newNotifs.add(item)
                    sendSystemNotification(context, loan.id.hashCode() + 200, title, message)
                }
            }
        }

        return newNotifs
    }

    private fun sendSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Android 13+ permission not yet granted or simulator limitation
        } catch (_: Exception) {
            // Safely continue
        }
    }
}
