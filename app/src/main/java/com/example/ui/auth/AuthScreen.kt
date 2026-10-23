package com.example.ui.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminUser
import com.example.model.Customer
import com.example.ui.components.AmarBankLogo
import com.example.ui.components.AmarBankTextFieldDefaults
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankEmerald
import com.example.ui.theme.AmarBankEmeraldLight
import com.example.ui.theme.AmarBankRose
import com.example.ui.theme.AmarBankRoseLight
import com.example.ui.theme.AmarBankRoyalBlue

enum class AuthTab {
    ADMIN,
    CUSTOMER
}

@Composable
fun AuthScreen(
    admin: AdminUser,
    customers: List<Customer>,
    isSyncingCloud: Boolean,
    onAdminLoginSuccess: () -> Unit,
    onCustomerLoginSuccess: (Customer) -> Unit,
    onSyncCustomersFromCloud: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AuthTab.ADMIN) }
    var adminPinInput by remember { mutableStateOf("") }
    var isAdminPinVisible by remember { mutableStateOf(false) }

    var customerIdentifierInput by remember { mutableStateOf("") }
    var customerPinInput by remember { mutableStateOf("") }
    var isCustomerPinVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A192F),
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Brand Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(top = 28.dp, bottom = 20.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AmarBankLogo(iconSize = 44.dp, showText = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sistem Pencatatan Kredit, Piutang & Angsuran",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dual Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selectedTab == AuthTab.ADMIN) AmarBankBlue else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedTab = AuthTab.ADMIN
                                    errorMessage = null
                                    successMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Portal Admin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == AuthTab.ADMIN) Color.White else Color(0xFF334155)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selectedTab == AuthTab.CUSTOMER) AmarBankBlue else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedTab = AuthTab.CUSTOMER
                                    errorMessage = null
                                    successMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Portal Pelanggan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == AuthTab.CUSTOMER) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }

                // Error / Success Feedback
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .background(AmarBankRoseLight, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmarBankRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMessage!!, fontSize = 11.sp, color = AmarBankRose, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .background(AmarBankEmeraldLight, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = successMessage!!, fontSize = 11.sp, color = AmarBankEmerald, fontWeight = FontWeight.Medium)
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    if (selectedTab == AuthTab.ADMIN) {
                        // PORTAL ADMIN LOGIN
                        Text(
                            text = "Masuk Sebagai Pengelola",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Masukkan 6-digit PIN keamanan Admin",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual 6-Box High-Contrast PIN Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 6) {
                                val digitChar = if (i < adminPinInput.length) {
                                    if (isAdminPinVisible) adminPinInput[i].toString() else "●"
                                } else {
                                    ""
                                }
                                val isFilled = i < adminPinInput.length
                                val isFocusedBox = i == adminPinInput.length.coerceAtMost(5)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(if (isFilled) Color(0xFFEFF6FF) else Color.White, RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isFocusedBox) 2.dp else 1.5.dp,
                                            color = if (isFocusedBox) AmarBankBlue else if (isFilled) AmarBankRoyalBlue else Color(0xFF64748B),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digitChar,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminPinInput,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) adminPinInput = it },
                            label = { Text("Ketik PIN Akses Admin", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                            placeholder = { Text("Contoh: 987654", color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmarBankBlue) },
                            trailingIcon = {
                                IconButton(onClick = { isAdminPinVisible = !isAdminPinVisible }) {
                                    Icon(
                                        imageVector = if (isAdminPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isAdminPinVisible) "Sembunyikan PIN" else "Lihat PIN",
                                        tint = Color(0xFF0F172A)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isAdminPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            textStyle = TextStyle(
                                color = Color(0xFF000000), // Pure Black text for absolute clarity
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 3.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = AmarBankTextFieldDefaults.colors()
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PIN default: 987654 (${admin.fullName})",
                                fontSize = 10.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isAdminPinVisible) "PIN Terlihat" else "PIN Tersembunyi",
                                fontSize = 10.sp,
                                color = AmarBankBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { isAdminPinVisible = !isAdminPinVisible }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                if (adminPinInput.trim() == admin.pin.trim()) {
                                    onAdminLoginSuccess()
                                } else {
                                    errorMessage = "PIN Admin tidak valid. Masukkan PIN yang benar."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Buka Akses Admin", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // PORTAL PELANGGAN / NASABAH LOGIN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Portal Mandiri Nasabah",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Akses aman untuk cek tagihan & kwitansi",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            // Update data dari Cloud button
                            OutlinedButton(
                                onClick = onSyncCustomersFromCloud,
                                enabled = !isSyncingCloud,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSyncingCloud) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmarBankBlue)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Update", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmarBankBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = customerIdentifierInput,
                            onValueChange = { customerIdentifierInput = it },
                            label = { Text("ID Nasabah / No. WhatsApp", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                            placeholder = { Text("Contoh: CUST-1001 atau 081234567890", color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AmarBankBlue) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF000000), // Pure Black
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = AmarBankTextFieldDefaults.colors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual 6-Box High-Contrast Customer PIN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 6) {
                                val digitChar = if (i < customerPinInput.length) {
                                    if (isCustomerPinVisible) customerPinInput[i].toString() else "●"
                                } else {
                                    ""
                                }
                                val isFilled = i < customerPinInput.length
                                val isFocusedBox = i == customerPinInput.length.coerceAtMost(5)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(if (isFilled) Color(0xFFEFF6FF) else Color.White, RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isFocusedBox) 2.dp else 1.5.dp,
                                            color = if (isFocusedBox) AmarBankBlue else if (isFilled) AmarBankRoyalBlue else Color(0xFF64748B),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digitChar,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customerPinInput,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) customerPinInput = it },
                            label = { Text("PIN Akses Nasabah (6 Digit)", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                            placeholder = { Text("Ketik 6 digit PIN...", color = Color(0xFF64748B)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmarBankBlue) },
                            trailingIcon = {
                                IconButton(onClick = { isCustomerPinVisible = !isCustomerPinVisible }) {
                                    Icon(
                                        imageVector = if (isCustomerPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isCustomerPinVisible) "Sembunyikan PIN" else "Lihat PIN",
                                        tint = Color(0xFF0F172A)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isCustomerPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            textStyle = TextStyle(
                                color = Color(0xFF000000), // Pure Black
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 3.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = AmarBankTextFieldDefaults.colors()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                val idQuery = customerIdentifierInput.trim().lowercase()
                                val cleanPhoneQuery = customerIdentifierInput.replace(Regex("\\D"), "")
                                val pinQuery = customerPinInput.trim()

                                val matchedCustomer = customers.find { c ->
                                    val matchId = c.id.lowercase() == idQuery
                                    val matchPhone = c.phone.replace(Regex("\\D"), "") == cleanPhoneQuery
                                    (matchId || matchPhone) && c.pin == pinQuery
                                }

                                if (matchedCustomer != null) {
                                    onCustomerLoginSuccess(matchedCustomer)
                                } else {
                                    errorMessage = "ID Nasabah / No. HP atau PIN tidak cocok. Silakan tanyakan PIN Anda ke Petugas Admin."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmarBankBlue)
                        ) {
                            Text(text = "Masuk ke Akun Saya", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Contoh login nasabah awal: ID CUST-1001 (Budi Santoso, PIN: 112233) atau CUST-1002 (Siti Aminah, PIN: 223344)",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // Bottom Footer: The Update
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "THE UPDATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
