package com.example.ui.admin

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Customer
import com.example.model.Loan
import com.example.ui.components.AmarBankTextFieldDefaults
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankRoyalBlue
import com.example.utils.Formatters

@Composable
fun CustomerManagementDialog(
    customers: List<Customer>,
    loans: List<Loan>,
    onSaveCustomer: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Auto-generate next customer ID
    val nextCustomerId = remember(customers) {
        val nums = customers.mapNotNull {
            val match = Regex("\\d+").find(it.id)
            match?.value?.toIntOrNull()
        }
        val maxNum = if (nums.isNotEmpty()) nums.maxOrNull() ?: 1000 else 1000
        "CUST-${maxNum + 1}"
    }

    // Form inputs for new customer profile worksheet
    var formFullName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }
    var formNotes by remember { mutableStateOf("") }
    var formPin by remember { mutableStateOf("123456") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Lembar Kerja Profil Baru Nasabah
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
                                .size(34.dp)
                                .background(AmarBankBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lembar Kerja Profil Baru Nasabah",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Pendaftaran debitur baru & penerbitan PIN akses",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                    }
                }

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ID Nasabah Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ID Debitur Otomatis", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                                Text(
                                    text = nextCustomerId,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = AmarBankRoyalBlue
                                )
                            }
                            Text("Dibuat Otomatis", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Nama Lengkap
                    OutlinedTextField(
                        value = formFullName,
                        onValueChange = { formFullName = it },
                        label = { Text("Nama Lengkap Nasabah *", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Contoh: Budi Prasetyo") },
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Nomor WhatsApp
                    OutlinedTextField(
                        value = formPhone,
                        onValueChange = { formPhone = it },
                        label = { Text("Nomor WhatsApp / HP Aktif *", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Contoh: 081234567890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // PIN Akses Nasabah
                    OutlinedTextField(
                        value = formPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) formPin = it },
                        label = { Text("PIN Login Mandiri Nasabah (6 Digit) *", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Contoh: 123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Alamat Lengkap
                    OutlinedTextField(
                        value = formAddress,
                        onValueChange = { formAddress = it },
                        label = { Text("Alamat Tempat Tinggal *", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Jalan, RT/RW, Kelurahan, Kecamatan, Kota") },
                        minLines = 2,
                        maxLines = 3,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Catatan Tambahan
                    OutlinedTextField(
                        value = formNotes,
                        onValueChange = { formNotes = it },
                        label = { Text("Catatan / Keterangan Pekerjaan (Opsional)", color = Color(0xFF0F172A)) },
                        placeholder = { Text("Pekerjaan, kontak darurat, atau catatan kredit") },
                        minLines = 2,
                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 13.sp),
                        colors = AmarBankTextFieldDefaults.colors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Bottom Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Text("Batal", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (formFullName.isBlank()) {
                                Toast.makeText(context, "Nama lengkap harus diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (formPhone.isBlank()) {
                                Toast.makeText(context, "Nomor WhatsApp harus diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newCust = Customer(
                                id = nextCustomerId,
                                fullName = formFullName.trim(),
                                phone = formPhone.trim(),
                                address = formAddress.trim(),
                                notes = formNotes.trim().ifBlank { null },
                                pin = formPin.ifBlank { "123456" },
                                createdAt = Formatters.getCurrentDateYmd()
                            )

                            onSaveCustomer(newCust)
                            Toast.makeText(context, "Nasabah ${newCust.fullName} Berhasil Didaftarkan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.5f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Nasabah", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
