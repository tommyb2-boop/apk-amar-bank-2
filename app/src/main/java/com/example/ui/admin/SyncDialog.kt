package com.example.ui.admin

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.FirebaseSyncService
import com.example.model.AdminUser
import com.example.model.Customer
import com.example.model.Loan
import com.example.model.Payment
import com.example.model.SystemSettings
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun SyncDialog(
    firebaseService: FirebaseSyncService,
    customers: List<Customer>,
    loans: List<Loan>,
    payments: List<Payment>,
    systemSettings: SystemSettings,
    admin: AdminUser,
    onDataUpdatedFromCloud: (List<Customer>, List<Loan>, List<Payment>, SystemSettings?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

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
                                .background(AmarBankBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Sinkronisasi Firebase & Cadangan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text(text = "Proyek: ${FirebaseSyncService.PROJECT_ID}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status banner
                    if (syncMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSuccess) Color(0xFFECFDF5) else Color(0xFFFFF1F2), RoundedCornerShape(14.dp))
                                .border(1.dp, if (isSuccess) Color(0xFFA7F3D0) else Color(0xFFFECDD3), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = syncMessage!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSuccess) AmarBankEmerald else Color(0xFFE11D48)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Section 1: Firebase Cloud
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(18.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AmarBankEmerald, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FIREBASE FIRESTORE CLOUD RESMI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AmarBankBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aplikasi ini terhubung langsung ke Firebase Firestore proyek gen-lang-client-0985161510. Data tersinkronisasi real-time antar perangkat dan aplikasi web.",
                                fontSize = 10.sp,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Detail Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Project ID:", fontSize = 9.sp, color = Color.Gray)
                                    Text(text = "gen-lang-client...", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Koleksi Firestore:", fontSize = 9.sp, color = Color.Gray)
                                    Text(text = "amar_bank_cloud", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Dokumen:", fontSize = 9.sp, color = Color.Gray)
                                    Text(text = "database", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        isLoading = true
                                        syncMessage = null
                                        coroutineScope.launch {
                                            val res = firebaseService.uploadToFirebase(customers, loans, payments, systemSettings, admin)
                                            isLoading = false
                                            isSuccess = res.success
                                            syncMessage = res.message
                                        }
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload ke Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        isLoading = true
                                        syncMessage = null
                                        coroutineScope.launch {
                                            val res = firebaseService.updateFromFirebase()
                                            isLoading = false
                                            isSuccess = res.success
                                            syncMessage = res.message
                                            if (res.success && res.customers != null && res.loans != null && res.payments != null) {
                                                onDataUpdatedFromCloud(res.customers, res.loans, res.payments, res.settings)
                                            }
                                        }
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmarBankRoyalBlue)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Update dari Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 2: Local JSON Backup & Share
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(18.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "CADANGAN DATABASE LOKAL (.JSON)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AmarBankBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bagikan atau simpan berkas JSON cadangan seluruh data debitur, akad kredit, dan mutasi angsuran ke aplikasi lain atau email.",
                                fontSize = 10.sp,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val backupJson = JSONObject().apply {
                                        put("version", "2.0-android")
                                        put("exportedAt", Formatters.getCurrentIsoDate())
                                        put("totalCustomers", customers.size)
                                        put("totalLoans", loans.size)
                                        put("totalPayments", payments.size)

                                        val custArr = JSONArray()
                                        customers.forEach { c ->
                                            custArr.put(JSONObject().apply {
                                                put("id", c.id)
                                                put("fullName", c.fullName)
                                                put("phone", c.phone)
                                                put("address", c.address)
                                                put("notes", c.notes ?: "")
                                                put("pin", c.pin)
                                            })
                                        }
                                        put("customers", custArr)

                                        val loanArr = JSONArray()
                                        loans.forEach { l ->
                                            loanArr.put(JSONObject().apply {
                                                put("id", l.id)
                                                put("customerId", l.customerId)
                                                put("customerName", l.customerName)
                                                put("loanTitle", l.loanTitle)
                                                put("totalBill", l.totalBill)
                                                put("remainingBalance", l.remainingBalance)
                                                put("status", l.status.name)
                                                put("nextDueDate", l.nextDueDate)
                                            })
                                        }
                                        put("loans", loanArr)
                                    }

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, backupJson.toString(2))
                                        type = "application/json"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Simpan / Bagikan Cadangan JSON Amar Bank"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ekspor & Bagikan File Cadangan JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
