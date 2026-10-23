package com.example.ui.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmarBankBlue
import com.example.ui.theme.AmarBankRoyalBlue

object AmarBankTextFieldDefaults {
    @Composable
    fun colors() = OutlinedTextFieldDefaults.colors(
        // High contrast: Pure Black text on Pure White background
        focusedTextColor = Color(0xFF000000),
        unfocusedTextColor = Color(0xFF000000),
        disabledTextColor = Color(0xFF334155),
        focusedContainerColor = Color(0xFFFFFFFF),
        unfocusedContainerColor = Color(0xFFFFFFFF),
        disabledContainerColor = Color(0xFFF1F5F9),
        cursorColor = AmarBankBlue,
        errorCursorColor = Color(0xFFDC2626),
        focusedBorderColor = AmarBankBlue,
        unfocusedBorderColor = Color(0xFF475569), // Crisp and clearly visible border
        disabledBorderColor = Color(0xFFCBD5E1),
        errorBorderColor = Color(0xFFDC2626),
        focusedLabelColor = AmarBankBlue,
        unfocusedLabelColor = Color(0xFF0F172A), // Clear, readable label
        disabledLabelColor = Color(0xFF64748B),
        errorLabelColor = Color(0xFFDC2626),
        focusedPlaceholderColor = Color(0xFF64748B),
        unfocusedPlaceholderColor = Color(0xFF64748B),
        disabledPlaceholderColor = Color(0xFF94A3B8),
        focusedLeadingIconColor = AmarBankBlue,
        unfocusedLeadingIconColor = Color(0xFF1E293B),
        disabledLeadingIconColor = Color(0xFF94A3B8),
        focusedTrailingIconColor = AmarBankBlue,
        unfocusedTrailingIconColor = Color(0xFF1E293B),
        disabledTrailingIconColor = Color(0xFF94A3B8)
    )
}
