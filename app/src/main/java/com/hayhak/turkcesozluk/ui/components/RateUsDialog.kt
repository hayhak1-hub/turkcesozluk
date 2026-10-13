package com.hayhak.turkcesozluk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.util.PlayStoreHelper
import com.hayhak.turkcesozluk.util.RatePromptStore
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun RateUsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107)
                    )
                }
            }
        },
        title = { Text(stringResource(R.string.settings_rate_us)) },
        text = { Text(stringResource(R.string.settings_rate_us_desc)) },
        confirmButton = {
            TextButton(
                onClick = {
                    // Güncelleme penceresi de aynı sayfayı açtığı için kayıt PlayStoreHelper'da
                    // değil burada tutulur: yalnızca değerlendirme için gidenler sayılır.
                    RatePromptStore(context).markVisitedPlay()
                    PlayStoreHelper.openListing(context)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.rate_dialog_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.rate_dialog_later))
            }
        }
    )
}

/**
 * [trigger] true olduğunda (örn. quiz/oyun turu bitti) ve süre dolmuşsa değerlendirme penceresini
 * gösterir. Sonucun görülebilmesi için kısa bir gecikme verir.
 */
@Composable
fun RatePromptEffect(trigger: Boolean) {
    val context = LocalContext.current
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(trigger) {
        if (!trigger) return@LaunchedEffect
        val store = RatePromptStore(context)
        if (!store.canShowNow()) return@LaunchedEffect
        delay(1500)
        store.markShown()
        show = true
    }
    if (show) RateUsDialog(onDismiss = { show = false })
}
