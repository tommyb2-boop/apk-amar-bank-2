package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmarBankRoyalBlue

@Composable
fun AmarBankLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 38.dp,
    showText: Boolean = true,
    textColor: Color = Color(0xFF0F172A),
    subtitleColor: Color = Color(0xFF64748B)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .background(AmarBankRoyalBlue, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "Amar Bank Logo",
                tint = Color.White,
                modifier = Modifier.size(iconSize * 0.6f)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AMAR ",
                        fontWeight = FontWeight.Black,
                        fontSize = (iconSize.value * 0.42f).sp,
                        color = textColor,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "BANK",
                        fontWeight = FontWeight.Black,
                        fontSize = (iconSize.value * 0.42f).sp,
                        color = AmarBankRoyalBlue,
                        letterSpacing = (-0.5).sp
                    )
                }
                Text(
                    text = "KREDIT & PIUTANG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = subtitleColor,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
