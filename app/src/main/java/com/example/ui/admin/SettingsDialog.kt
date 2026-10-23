package com.example.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AdminUser
import com.example.model.SystemSettings
import com.example.ui.theme.AmarBankBlue

@Composable
fun SettingsDialog(
    currentAdmin: AdminUser,
    systemSettings: SystemSettings,
    onSaveAdmin: (AdminUser) -> Unit,
    onSaveSettings: (SystemSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("ADMIN") }

    // Admin fields
    var adminName by remember { mutableStateOf(currentAdmin.fullName) }
    var adminEmail by remember { mutableStateOf(currentAdmin.email) }
    var adminPhone by remember { mutableStateOf(currentAdmin.phone) }
    var adminPin by remember { mutableStateOf(currentAdmin.pin) }

    // System fields
    var cityName by remember { mutableStateOf(systemSettings.cityName) }
    var branchName by remember { mutableStateOf(systemSettings.branchName) }
    var divisionName by remember { mutableStateOf(systemSettings.divisionName) }
    var branchAddress by remember { mutableStateOf(systemSettings.branchAddress) }
    var branchPhone by remember { mutableStateOf(systemSettings.branchPhone) }
    var officialWhatsApp by remember { mutableStateOf(systemSettings.officialWhatsApp) }
    var responsibleName by remember { mutableStateOf(systemSettings.responsibleName) }
    var responsibleTitle by remember { mutableStateOf(systemSettings.responsibleTitle) }

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
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Pengaturan Profil & Sistem", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text(text = "Profil admin, PIN, & identitas kota PDF", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (activeTab == "ADMIN") AmarBankBlue else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .clickable { activeTab = "ADMIN" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Profil & PIN Admin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "ADMIN") Color.White else Color(0xFF475569)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (activeTab == "SYSTEM") AmarBankBlue else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .clickable { activeTab = "SYSTEM" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Kota PDF & Instansi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "SYSTEM") Color.White else Color(0xFF475569)
                        )
                    }
                }

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeTab == "ADMIN") {
                        OutlinedTextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text("Nama Lengkap Admin") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Email Pemulihan") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminPhone,
                            onValueChange = { adminPhone = it },
                            label = { Text("Nomor WhatsApp Admin") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminPin,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) adminPin = it },
                            label = { Text("PIN Login Admin (6 Digit)") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = cityName,
                            onValueChange = { cityName = it },
                            label = { Text("Nama Kota Tertera di PDF *") },
                            placeholder = { Text("Contoh: Surabaya, Jakarta, Medan") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = branchName,
                            onValueChange = { branchName = it },
                            label = { Text("Nama Lembaga / Instansi") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = divisionName,
                            onValueChange = { divisionName = it },
                            label = { Text("Nama Divisi") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = branchAddress,
                            onValueChange = { branchAddress = it },
                            label = { Text("Alamat Kantor") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = responsibleName,
                            onValueChange = { responsibleName = it },
                            label = { Text("Nama Pejabat Penanggung Jawab (Tanda Tangan)") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = responsibleTitle,
                            onValueChange = { responsibleTitle = it },
                            label = { Text("Jabatan Resmi") },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp
                            ),
                            colors = com.example.ui.components.AmarBankTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (activeTab == "ADMIN") {
                                val updated = currentAdmin.copy(
                                    fullName = adminName.trim(),
                                    email = adminEmail.trim(),
                                    phone = adminPhone.trim(),
                                    pin = adminPin.trim()
                                )
                                onSaveAdmin(updated)
                                Toast.makeText(context, "Profil Admin berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            } else {
                                val updated = systemSettings.copy(
                                    cityName = cityName.trim(),
                                    branchName = branchName.trim(),
                                    divisionName = divisionName.trim(),
                                    branchAddress = branchAddress.trim(),
                                    responsibleName = responsibleName.trim(),
                                    responsibleTitle = responsibleTitle.trim()
                                )
                                onSaveSettings(updated)
                                Toast.makeText(context, "Pengaturan sistem & kota PDF berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
