package com.example.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseSyncService
import com.example.model.AdminUser
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.LoanStatus
import com.example.model.LoanType
import com.example.model.NotificationItem
import com.example.model.Payment
import com.example.model.SystemSettings
import com.example.ui.components.AmarBankLogo
import com.example.ui.components.AmarBankTextFieldDefaults
import com.example.ui.components.LoanReceiptDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.AmarBankAmber
import com.example.ui.theme.AmarBankAmberLight
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankEmeraldLight
import com.example.ui.theme.AmarBankRose
import com.example.ui.theme.AmarBankRoseLight
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.ExcelExporter
import com.example.utils.Formatters
import com.example.utils.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    admin: AdminUser,
    systemSettings: SystemSettings,
    customers: List<Customer>,
    loans: List<Loan>,
    payments: List<Payment>,
    notifications: List<NotificationItem>,
    firebaseService: FirebaseSyncService,
    onSaveCustomer: (Customer) -> Unit,
    onSaveLoan: (Loan) -> Unit,
    onRecordPayment: (Payment, Loan) -> Unit,
    onSaveAdmin: (AdminUser) -> Unit,
    onSaveSettings: (SystemSettings) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onClearAllNotifications: () -> Unit,
    onDataUpdatedFromCloud: (List<Customer>, List<Loan>, List<Payment>, SystemSettings?) -> Unit,
    onResetAllData: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showAdminMenuSheet by remember { mutableStateOf(false) }

    // Dialog state controllers
    var showCustomerRegistrationDialog by remember { mutableStateOf(false) }
    var showNewLoanDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showGoogleDriveBackupDialog by remember { mutableStateOf(false) }

    // Detail Customer Modal State
    var selectedCustomerForDetail by remember { mutableStateOf<Customer?>(null) }
    // LIST Piutang Modal State
    var selectedCustomerForListReceivables by remember { mutableStateOf<Customer?>(null) }

    // Active receipt preview
    var activeReceiptPayment by remember { mutableStateOf<Payment?>(null) }
    var activeLoanReceipt by remember { mutableStateOf<Loan?>(null) }

    // Direct pay shortcut from customer detail
    var loanToPayDirectly by remember { mutableStateOf<Loan?>(null) }

    // Search query for customers on main screen
    var customerSearchQuery by remember { mutableStateOf("") }

    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }

    // Financial Statistics
    val totalPlafon = remember(loans) { loans.sumOf { it.totalBill } }
    val totalTerkumpul = remember(payments) { payments.sumOf { it.amount } }
    val totalSisa = remember(loans) { loans.sumOf { it.remainingBalance } }
    val activeLoansCount = remember(loans) { loans.count { it.status != LoanStatus.LUNAS } }

    // Filter customers by search
    val filteredCustomers = remember(customers, customerSearchQuery) {
        if (customerSearchQuery.isBlank()) customers
        else {
            val q = customerSearchQuery.lowercase()
            customers.filter {
                it.fullName.lowercase().contains(q) ||
                it.id.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.address.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            // "cukup tombol menu admin saja yang ada di bagian pojok kanan bawah aplikasi"
            ExtendedFloatingActionButton(
                onClick = { showAdminMenuSheet = true },
                containerColor = AmarBankBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Admin",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = "Menu Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
        ) {
            // =========================================================================
            // 1. HEADER:
            // "lebih di perkecil dan di pendekan lagi jarak antara logo dengan tulisan Halo,
            // dan tidak perlu ada tulisan admin panel, + tombol notifikasi"
            // =========================================================================
            // 1. HEADER:
            // "lebih di perkecil dan di pendekan lagi jarak antara logo dengan tulisan Halo,
            // dan tidak perlu ada tulisan admin panel, + tombol notifikasi"
            // =========================================================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo and Halo very close together, compact
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        AmarBankLogo(iconSize = 24.dp, showText = false)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Halo, ${admin.fullName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF0F172A),
                                letterSpacing = (-0.2).sp,
                                maxLines = 1
                            )
                            Text(
                                text = systemSettings.cityName,
                                fontSize = 8.5.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    // Top Right: Cloud Badge & Tombol Notifikasi
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AmarBankEmerald, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Cloud",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmarBankEmerald
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Tombol Notifikasi with unread badge
                        Box {
                            IconButton(
                                onClick = { showNotificationDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = AmarBankBlue,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            if (unreadNotifCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .align(Alignment.TopEnd)
                                        .background(AmarBankRose, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadNotifCount > 99) "99+" else unreadNotifCount.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. TOTAL PLAFON ; ANGSURAN ; SISA PIUTANG AKTIF ; TOTAL NASABAH
            // "(lebih dipersempit lagi jaraknya )"
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Card 1: TOTAL PLAFON
                CompactMetricCard(
                    title = "TOTAL PLAFON",
                    value = Formatters.formatRupiah(totalPlafon),
                    countLabel = "${loans.size} Akad",
                    textColor = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )

                // Card 2: ANGSURAN
                CompactMetricCard(
                    title = "ANGSURAN",
                    value = Formatters.formatRupiah(totalTerkumpul),
                    countLabel = "${payments.size} Bayar",
                    textColor = AmarBankEmerald,
                    modifier = Modifier.weight(1f)
                )

                // Card 3: SISA PIUTANG AKTIF
                CompactMetricCard(
                    title = "SISA PIUTANG AKTIF",
                    value = Formatters.formatRupiah(totalSisa),
                    countLabel = "$activeLoansCount Aktif",
                    textColor = AmarBankRoyalBlue,
                    modifier = Modifier.weight(1f)
                )

                // Card 4: TOTAL NASABAH
                CompactMetricCard(
                    title = "TOTAL NASABAH",
                    value = "${customers.size} Org",
                    countLabel = "Debitur",
                    textColor = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar Nasabah Compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customerSearchQuery,
                    onValueChange = { customerSearchQuery = it },
                    placeholder = { Text("Cari nama nasabah, ID (CUST-...), no HP...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF475569)) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    colors = AmarBankTextFieldDefaults.colors(),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // =========================================================================
            // 3. SEMUA DATA PELANGGAN DI HALAMAN UTAMA (FONT DIPERBESAR 2X, JARAK MEPET,
            // KARTU NASABAH DENGAN JENIS AKAD, NOMINAL RUPIAH, TOMBOL LIST)
            // =========================================================================
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (customerSearchQuery.isNotBlank()) "Tidak ada nasabah yang cocok dengan pencarian." else "Belum ada data nasabah terdaftar.",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // Jarak baris mepet
                ) {
                    items(filteredCustomers) { cust ->
                        val custLoans = loans.filter { it.customerId == cust.id }
                        val custTotalPlafon = custLoans.sumOf { it.totalBill }
                        val custTotalPaid = custLoans.sumOf { it.totalPaid }
                        val custRemaining = custLoans.sumOf { it.remainingBalance }

                        val isAnyOverdue = custLoans.any { it.status == LoanStatus.MENUNGGAK }
                        val isAnyAttention = custLoans.any { it.status == LoanStatus.PERHATIAN }
                        val isAllLunas = custLoans.isNotEmpty() && custLoans.all { it.status == LoanStatus.LUNAS }

                        CustomerPortfolioCard(
                            customer = cust,
                            loans = custLoans,
                            totalPlafon = custTotalPlafon,
                            totalPaid = custTotalPaid,
                            remaining = custRemaining,
                            isAnyOverdue = isAnyOverdue,
                            isAnyAttention = isAnyAttention,
                            isAllLunas = isAllLunas,
                            onDetailClick = { selectedCustomerForDetail = cust },
                            onListClick = { selectedCustomerForListReceivables = cust }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(75.dp)) // Extra space for FAB
                    }
                }
            }
        }
    }

    // =========================================================================
    // 4. ISI DI TOMBOL MENU ADMIN (BOTTOM SHEET)
    // - pengaturan profil & kota pdf
    // - pendaftaran nasabah baru
    // - akad kredit baru
    // - laporan rekapitulasi PDF (pilihan rentang tanggal rekap dulu)
    // - backup database agar dikirimkan ke email dicoba.ngetes@gmail.com
    // - sinkronisasi firebase cloud
    // - factory reset
    // - logout
    // =========================================================================
    if (showAdminMenuSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAdminMenuSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                // Header Sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Menu Admin Amar Bank",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Akses modul manajemen piutang & operasional",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = { showAdminMenuSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 1. Pengaturan Profil & Kota PDF
                    AdminMenuRow(
                        title = "Pengaturan Profil & Kota PDF",
                        subtitle = "Ubah profil admin, PIN login, & kota penandatanganan PDF",
                        icon = Icons.Default.Settings,
                        iconTint = AmarBankBlue,
                        bgColor = Color(0xFFEFF6FF),
                        onClick = {
                            showAdminMenuSheet = false
                            showSettingsDialog = true
                        }
                    )

                    // 2. Pendaftaran Nasabah Baru
                    AdminMenuRow(
                        title = "Pendaftaran Nasabah Baru",
                        subtitle = "Tambah debitur, buat ID unik CUST-... & PIN akses",
                        icon = Icons.Default.PersonAdd,
                        iconTint = AmarBankRoyalBlue,
                        bgColor = Color(0xFFEEF2FF),
                        onClick = {
                            showAdminMenuSheet = false
                            showCustomerRegistrationDialog = true
                        }
                    )

                    // 3. Akad Kredit Baru
                    AdminMenuRow(
                        title = "Akad Kredit Baru",
                        subtitle = "Pencatatan kredit barang / tunai, margin %, tenor",
                        icon = Icons.Default.AddCard,
                        iconTint = Color(0xFF4338CA),
                        bgColor = Color(0xFFEEF2FF),
                        onClick = {
                            showAdminMenuSheet = false
                            showNewLoanDialog = true
                        }
                    )

                    // 4. Laporan Rekapitulasi PDF (Pilihan Rentang Tanggal)
                    AdminMenuRow(
                        title = "Laporan Rekapitulasi PDF",
                        subtitle = "Pilih rentang tanggal rekapitulasi, cetak & simpan PDF",
                        icon = Icons.Default.Description,
                        iconTint = Color(0xFF0D9488),
                        bgColor = Color(0xFFF0FDFA),
                        onClick = {
                            showAdminMenuSheet = false
                            showReportDialog = true
                        }
                    )

                    // 5. Backup File XLS ke Lokal
                    AdminMenuRow(
                        title = "Backup File XLS ke Lokal",
                        subtitle = "Ekspor & unduh spreadsheet riwayat nasabah & piutang (.xls)",
                        icon = Icons.Default.TableChart,
                        iconTint = AmarBankEmerald,
                        bgColor = AmarBankEmeraldLight,
                        onClick = {
                            showAdminMenuSheet = false
                            val xlsFile = ExcelExporter.generateExcelBackup(
                                context, customers, loans, payments, systemSettings
                            )
                            if (xlsFile != null) {
                                Toast.makeText(context, "Backup XLS Berhasil Disimpan:\n${xlsFile.name}", Toast.LENGTH_LONG).show()
                                ExcelExporter.shareExcelFile(context, xlsFile)
                            } else {
                                Toast.makeText(context, "Gagal membuat cadangan XLS", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // 6. Backup ke Google Drive
                    AdminMenuRow(
                        title = "Backup ke Google Drive",
                        subtitle = "Unggah & amankan cadangan database otomatis ke Google Drive",
                        icon = Icons.Default.CloudUpload,
                        iconTint = Color(0xFF1A73E8),
                        bgColor = Color(0xFFE8F0FE),
                        onClick = {
                            showAdminMenuSheet = false
                            showGoogleDriveBackupDialog = true
                        }
                    )

                    // 7. Restore dari Google Drive
                    AdminMenuRow(
                        title = "Restore dari Google Drive",
                        subtitle = "Pulihkan snapshot basis data kredit dari Google Drive Cloud",
                        icon = Icons.Default.CloudDownload,
                        iconTint = Color(0xFFEA4335),
                        bgColor = Color(0xFFFCE8E6),
                        onClick = {
                            showAdminMenuSheet = false
                            showGoogleDriveBackupDialog = true
                        }
                    )

                    // 6. Sinkronisasi Firebase Cloud
                    AdminMenuRow(
                        title = "Sinkronisasi Firebase Cloud",
                        subtitle = "Upload ke Cloud & Update data dari Firestore resmi",
                        icon = Icons.Default.Sync,
                        iconTint = AmarBankEmerald,
                        bgColor = AmarBankEmeraldLight,
                        onClick = {
                            showAdminMenuSheet = false
                            showSyncDialog = true
                        }
                    )

                    // 7. Factory Reset
                    AdminMenuRow(
                        title = "Factory Reset (Reset Semua Data)",
                        subtitle = "Bersihkan seluruh data debitur & piutang (Kode OTP email)",
                        icon = Icons.Default.DeleteSweep,
                        iconTint = AmarBankRose,
                        bgColor = AmarBankRoseLight,
                        onClick = {
                            showAdminMenuSheet = false
                            showResetDialog = true
                        }
                    )

                    // 8. Logout
                    AdminMenuRow(
                        title = "Logout / Keluar",
                        subtitle = "Kunci sesi pengelola & kembali ke portal login",
                        icon = Icons.Default.ExitToApp,
                        iconTint = Color(0xFF64748B),
                        bgColor = Color(0xFFF1F5F9),
                        onClick = {
                            showAdminMenuSheet = false
                            onLogout()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // =========================================================================
    // MODAL DIALOGS
    // =========================================================================

    // Detail Portofolio Nasabah Modal (Opened from "Detail" button)
    if (selectedCustomerForDetail != null) {
        CustomerDetailDialog(
            customer = selectedCustomerForDetail!!,
            loans = loans,
            payments = payments,
            systemSettings = systemSettings,
            onViewLoanReceipt = { loan ->
                activeLoanReceipt = loan
            },
            onViewPaymentReceipt = { pay ->
                activeReceiptPayment = pay
            },
            onDismiss = { selectedCustomerForDetail = null }
        )
    }

    // LIST Piutang Jatuh Tempo & Belum Tempo Nasabah (Opened from "LIST" button)
    if (selectedCustomerForListReceivables != null) {
        CustomerReceivablesListDialog(
            customer = selectedCustomerForListReceivables!!,
            loans = loans,
            systemSettings = systemSettings,
            onRecordPayment = { pay, updatedLoan ->
                onRecordPayment(pay, updatedLoan)
                // Refresh active receipt if needed
            },
            onDismiss = { selectedCustomerForListReceivables = null }
        )
    }

    // Google Drive Cloud Backup & Restore Dialog
    if (showGoogleDriveBackupDialog) {
        GoogleDriveBackupRestoreDialog(
            userEmail = admin.email,
            customers = customers,
            loans = loans,
            payments = payments,
            systemSettings = systemSettings,
            firebaseService = firebaseService,
            onRestoreConfirmed = {
                showGoogleDriveBackupDialog = false
                showSyncDialog = true
            },
            onDismiss = { showGoogleDriveBackupDialog = false }
        )
    }

    // Direct Payment Dialog
    if (loanToPayDirectly != null) {
        PaymentManagementDialog(
            payments = payments,
            loans = loans,
            onRecordPayment = { pay, updatedLoan ->
                onRecordPayment(pay, updatedLoan)
                loanToPayDirectly = null
                activeReceiptPayment = pay
            },
            onViewReceipt = { pay -> activeReceiptPayment = pay },
            onDismiss = { loanToPayDirectly = null }
        )
    }

    // Pendaftaran Nasabah Baru Dialog
    if (showCustomerRegistrationDialog) {
        CustomerManagementDialog(
            customers = customers,
            loans = loans,
            onSaveCustomer = { newCust ->
                onSaveCustomer(newCust)
                showCustomerRegistrationDialog = false
            },
            onDismiss = { showCustomerRegistrationDialog = false }
        )
    }

    // Akad Kredit Baru Dialog
    if (showNewLoanDialog) {
        LoanManagementDialog(
            loans = loans,
            customers = customers,
            onSaveLoan = { newLoan ->
                onSaveLoan(newLoan)
                showNewLoanDialog = false
                activeLoanReceipt = newLoan
            },
            onViewLoanReceipt = { activeLoanReceipt = it },
            onDismiss = { showNewLoanDialog = false }
        )
    }

    // Laporan Rekapitulasi PDF (with Date Range Picker)
    if (showReportDialog) {
        ReportDialog(
            customers = customers,
            loans = loans,
            payments = payments,
            systemSettings = systemSettings,
            onDismiss = { showReportDialog = false }
        )
    }

    // Pengaturan Profil & Kota PDF
    if (showSettingsDialog) {
        SettingsDialog(
            currentAdmin = admin,
            systemSettings = systemSettings,
            onSaveAdmin = onSaveAdmin,
            onSaveSettings = onSaveSettings,
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Pusat Notifikasi
    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            loans = loans,
            onMarkAllRead = onMarkAllNotificationsRead,
            onClearAll = onClearAllNotifications,
            onDismiss = { showNotificationDialog = false }
        )
    }

    // Sinkronisasi Firebase Cloud
    if (showSyncDialog) {
        SyncDialog(
            firebaseService = firebaseService,
            customers = customers,
            loans = loans,
            payments = payments,
            systemSettings = systemSettings,
            admin = admin,
            onDataUpdatedFromCloud = onDataUpdatedFromCloud,
            onDismiss = { showSyncDialog = false }
        )
    }

    // Factory Reset Dialog
    if (showResetDialog) {
        ResetDataDialog(
            adminEmail = admin.email,
            onConfirmReset = {
                onResetAllData()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Kwitansi E-Receipt Preview Dialog
    if (activeReceiptPayment != null) {
        ReceiptDialog(
            payment = activeReceiptPayment!!,
            onDismiss = { activeReceiptPayment = null }
        )
    }

    // Kwitansi Akad Baru Preview Dialog
    if (activeLoanReceipt != null) {
        LoanReceiptDialog(
            loan = activeLoanReceipt!!,
            onDismiss = { activeLoanReceipt = null }
        )
    }
}

// -----------------------------------------------------------------------------
// COMPACT METRIC CARD: Super tight spacing for Header Metrics (Font 2x Lebih Besar)
// -----------------------------------------------------------------------------
@Composable
private fun CompactMetricCard(
    title: String,
    value: String,
    countLabel: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp, // Diperbesar 2x
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.1.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                fontSize = 15.sp, // Diperbesar 2x
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = countLabel,
                fontSize = 12.sp, // Diperbesar 2x
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

// -----------------------------------------------------------------------------
// CUSTOMER PORTFOLIO CARD ON MAIN PAGE:
// - Font diperbesar 2x, jarak baris mepet.
// - Di tiap layer nasabah: jenis akad, nama kredit barang / pinjaman tunai
//   (nominal rupiah), lalu sebelah kanan ada tombol LIST.
// - Footer kirim whatsapp dan simpan pdf ke lokal telah dihapus.
// -----------------------------------------------------------------------------
@Composable
private fun CustomerPortfolioCard(
    customer: Customer,
    loans: List<Loan>,
    totalPlafon: Double,
    totalPaid: Double,
    remaining: Double,
    isAnyOverdue: Boolean,
    isAnyAttention: Boolean,
    isAllLunas: Boolean,
    onDetailClick: () -> Unit,
    onListClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // 1. Header Kartu Nasabah: Nama, ID, Status & Tombol DETAIL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = customer.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, // Font 2x lebih besar
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = customer.id,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = AmarBankRoyalBlue
                            )
                        }
                    }
                    Text(
                        text = "WA: ${customer.phone} ${if (customer.address.isNotBlank()) "• ${customer.address.take(24)}" else ""}",
                        fontSize = 10.5.sp,
                        color = Color.Gray
                    )
                }

                // Tombol Detail & Status Badge
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                when {
                                    isAllLunas -> AmarBankEmeraldLight
                                    isAnyOverdue -> AmarBankRoseLight
                                    isAnyAttention -> AmarBankAmberLight
                                    else -> Color(0xFFF1F5F9)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when {
                                isAllLunas -> "LUNAS"
                                isAnyOverdue -> "MACET"
                                else -> "LANCAR"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isAllLunas -> AmarBankEmerald
                                isAnyOverdue -> AmarBankRose
                                else -> AmarBankEmerald
                            }
                        )
                    }

                    // Tombol DETAIL
                    Button(
                        onClick = onDetailClick,
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Detail", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. KOP KEUANGAN NASABAH: Total Piutang ; Terbayar ; Sisa Piutang (Font diperbesar, spasi mepet)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Piutang (Plafon)
                Column {
                    Text(text = "Total Piutang", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(text = Formatters.formatRupiah(totalPlafon), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }

                Box(modifier = Modifier.width(1.dp).height(22.dp).background(Color(0xFFE2E8F0)))

                // Terbayar
                Column {
                    Text(text = "Terbayar", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(text = Formatters.formatRupiah(totalPaid), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = AmarBankEmerald)
                }

                Box(modifier = Modifier.width(1.dp).height(22.dp).background(Color(0xFFE2E8F0)))

                // Sisa Piutang
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Sisa Piutang", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(
                        text = Formatters.formatRupiah(remaining),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (remaining > 0) AmarBankRoyalBlue else AmarBankEmerald
                    )
                }
            }

            // 3. LAYER SEMUA AKAD NASABAH:
            // Jenis akad, nama kredit barang / pinjaman tunai (nominal rupiah), dan sebelah kanan tombol LIST
            if (loans.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    loans.forEach { loan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Jenis Akad (Barang / Tunai)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (loan.type == LoanType.BARANG) Color(0xFFE0F2FE) else Color(0xFFFEF3C7),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (loan.type == LoanType.BARANG) "Kredit Barang" else "Pinjaman Tunai",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (loan.type == LoanType.BARANG) Color(0xFF0284C7) else Color(0xFFD97706)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Status
                                    Text(
                                        text = loan.status.name,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (loan.status) {
                                            LoanStatus.LUNAS -> AmarBankEmerald
                                            LoanStatus.MENUNGGAK -> AmarBankRose
                                            LoanStatus.PERHATIAN -> AmarBankAmber
                                            else -> AmarBankRoyalBlue
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                // Nama kredit barang ataupun pinjaman tunai (nominal rupiah)
                                Text(
                                    text = "${loan.loanTitle} (${Formatters.formatRupiah(loan.totalBill)})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Tagihan: ${Formatters.formatRupiah(loan.installmentPerPeriod)} • Sisa: ${Formatters.formatRupiah(loan.remainingBalance)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Tombol LIST di sebelah kanan
                            Button(
                                onClick = onListClick,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatListBulleted,
                                    contentDescription = "List Piutang",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIST",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Belum ada akad aktif",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Button(
                        onClick = onListClick,
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankEmerald)
                    ) {
                        Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "LIST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ADMIN MENU BOTTOM SHEET ROW ITEM
// -----------------------------------------------------------------------------
@Composable
private fun AdminMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                Text(text = subtitle, fontSize = 9.5.sp, color = Color(0xFF64748B), maxLines = 1)
            }
        }
    }
}
