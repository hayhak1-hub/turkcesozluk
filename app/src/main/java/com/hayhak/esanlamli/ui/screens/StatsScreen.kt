package com.hayhak.esanlamli.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.esanlamli.viewmodel.StatsViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.hayhak.esanlamli.R
import com.hayhak.esanlamli.util.findActivity

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val favorites by viewModel.favorites.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val userWords by viewModel.userWords.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val premiumPriceText by viewModel.premiumPriceText.collectAsState()
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshStats()
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(32.dp))
        }

        item {
            PremiumCard(
                isPremium = isPremium,
                priceText = premiumPriceText,
                onPurchaseClick = {
                    context.findActivity()?.let { activity -> viewModel.purchasePremium(activity) }
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.weekly_activity), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                        val maxVal = (weeklyStats.maxOrNull() ?: 1).coerceAtLeast(1)
                        
                        // Haftanın gününü 0-6 arası al (Pzt:0 ... Paz:6)
                        val currentDayIndex = java.util.Calendar.getInstance().let {
                            val day = it.get(java.util.Calendar.DAY_OF_WEEK)
                            if (day == java.util.Calendar.SUNDAY) 6 else day - 2
                        }
                        
                        weeklyStats.forEachIndexed { index, value ->
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(if (value > 0) (value.toFloat() / maxVal).coerceAtLeast(0.1f) else 0f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            if (index == currentDayIndex) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = days[index], 
                                    fontSize = 10.sp, 
                                    color = if (index == currentDayIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    fontWeight = if (index == currentDayIndex) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = stringResource(R.string.stat_favorites), 
                    value = favorites.size.toString(), 
                    icon = Icons.Rounded.Favorite, 
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                StatCard(
                    title = stringResource(R.string.stat_searches), 
                    value = recentSearches.size.toString(), 
                    icon = Icons.Rounded.Search, 
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = stringResource(R.string.stat_my_words), 
                    value = userWords.size.toString(), 
                    icon = Icons.Rounded.Add, 
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.stat_dictionary), 
                    value = viewModel.synonymsCount.toString(), 
                    icon = Icons.AutoMirrored.Rounded.MenuBook, 
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            
            AchievementItem(
                title = stringResource(R.string.achievement_first_step_title), 
                desc = stringResource(R.string.achievement_first_step_desc), 
                isUnlocked = recentSearches.isNotEmpty()
            )
            AchievementItem(
                title = stringResource(R.string.achievement_hunter_title), 
                desc = stringResource(R.string.achievement_hunter_desc), 
                isUnlocked = favorites.size >= 10
            )
            AchievementItem(
                title = stringResource(R.string.achievement_writer_title), 
                desc = stringResource(R.string.achievement_writer_desc),
                isUnlocked = userWords.isNotEmpty()
            )

            Spacer(Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.appearance_mode), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        com.hayhak.esanlamli.data.db.AppTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                label = { 
                                    Text(when(theme) {
                                        com.hayhak.esanlamli.data.db.AppTheme.LIGHT -> stringResource(R.string.theme_light)
                                        com.hayhak.esanlamli.data.db.AppTheme.DARK -> stringResource(R.string.theme_dark)
                                        com.hayhak.esanlamli.data.db.AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                                    })
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.5.dp)
            ) {
                Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gizlilik Politikası", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gizlilik Politikası", fontWeight = FontWeight.Bold) },
        text = {
            androidx.compose.foundation.rememberScrollState().let { scrollState ->
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Türkçe Sözlük uygulaması olarak gizliliğinize önem veriyoruz. " +
                                "Uygulamamız kullanıcı verilerini toplamaz, saklamaz veya üçüncü taraflarla paylaşmaz. " +
                                "\n\n1. Veri Toplama: Herhangi bir kişisel veri (isim, e-posta, konum vb.) toplamıyoruz. " +
                                "\n\n2. Yerel Saklama: Favori kelimeleriniz ve istatistikleriniz sadece cihazınızda saklanır. " +
                                "\n\n3. İzinler: Uygulama sadece temel işlevler için gerekli izinleri (bildirim vb.) ister. " +
                                "\n\nBu politika uygulama içindeki tüm özellikler için geçerlidir.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun PremiumCard(isPremium: Boolean, priceText: String?, onPurchaseClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFFFDF0D5) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(if (isPremium) R.string.premium_thanks_title else R.string.premium_support_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(if (isPremium) R.string.premium_thanks_desc else R.string.premium_support_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (!isPremium) {
                Spacer(Modifier.width(12.dp))
                Button(onClick = onPurchaseClick, shape = RoundedCornerShape(14.dp)) {
                    Text(priceText ?: stringResource(R.string.premium_support_button))
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
            Text(title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun AchievementItem(title: String, desc: String, isUnlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (isUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
