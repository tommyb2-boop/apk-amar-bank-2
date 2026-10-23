package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Badge Status Kelancaran Nasabah / Kredit:
 * - LANCAR: Hijau (Semua pembayaran tepat waktu / tidak ada tunggakan jatuh tempo)
 * - MACET: Merah (Ada angsuran yang belum lunas dan melewati tanggal jatuh tempo)
 */
@Composable
fun NasabahStatusBadge(
    status: String, // "LANCAR" atau "MACET"
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 9.5.sp
) {
    val isMacet = status.equals("MACET", ignoreCase = true)
    Box(
        modifier = modifier
            .testTag("status_badge_${if (isMacet) "macet" else "lancar"}")
            .background(
                if (isMacet) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (isMacet) Color(0xFFFECACA) else Color(0xFF86EFAC),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isMacet) Color(0xFFDC2626) else Color(0xFF16A34A), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isMacet) "MACET" else "LANCAR",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isMacet) Color(0xFF991B1B) else Color(0xFF166534),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = fontSize,
                    lineHeight = (fontSize.value + 2f).sp
                )
            )
        }
    }
}
