package com.hayhak.turkcesozluk.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.db.AppLocale
import com.hayhak.turkcesozluk.ui.components.RateUsDialog
import com.hayhak.turkcesozluk.ui.components.UpdateAvailableDialog
import com.hayhak.turkcesozluk.util.PlayStoreHelper
import com.hayhak.turkcesozluk.util.PlayUpdateChecker
import com.hayhak.turkcesozluk.util.PlayUpdateInfo
import com.hayhak.turkcesozluk.util.UpdateCheckStatus
import com.hayhak.turkcesozluk.util.findActivity
import com.hayhak.turkcesozluk.viewmodel.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateToHelp: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val favorites by viewModel.favorites.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val userWords by viewModel.userWords.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val previousWeekTotal by viewModel.previousWeekTotal.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRateDialog by remember { mutableStateOf(false) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var manualUpdateInfo by remember { mutableStateOf<PlayUpdateInfo?>(null) }
    var languageExpanded by remember { mutableStateOf(false) }

    val collapsedLocales = remember(currentLanguage) {
        buildList {
            add(currentLanguage)
            for (fallback in listOf(AppLocale.SYSTEM, AppLocale.TURKISH, AppLocale.ENGLISH)) {
                if (size >= 2) break
                if (fallback !in this) add(fallback)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStats()
    }

    if (showRateDialog) {
        RateUsDialog(onDismiss = { showRateDialog = false })
    }

    manualUpdateInfo?.let { info ->
        UpdateAvailableDialog(
            updateInfo = info,
            onDismiss = { manualUpdateInfo = null },
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            WeeklyActivityCard(weeklyStats = weeklyStats, previousWeekTotal = previousWeekTotal)
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
                Spacer(modifier = Modifier.width(16.dp))
                StatCard(
                    title = stringResource(R.string.stat_searches),
                    value = recentSearches.size.toString(),
                    icon = Icons.Rounded.Search,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { languageExpanded = !languageExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_language),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (currentLanguage == AppLocale.SYSTEM) {
                                    stringResource(R.string.language_system)
                                } else {
                                    currentLanguage.nativeName
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (languageExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (languageExpanded) {
                                stringResource(R.string.language_show_less)
                            } else {
                                stringResource(R.string.language_show_all)
                            },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val localesToShow = if (languageExpanded) AppLocale.sortedForPicker else collapsedLocales
                        localesToShow.forEach { locale ->
                            FilterChip(
                                selected = currentLanguage == locale,
                                onClick = {
                                    if (currentLanguage != locale) {
                                        viewModel.setLanguage(locale)
                                        context.findActivity()?.recreate()
                                    }
                                },
                                label = {
                                    Text(
                                        if (locale == AppLocale.SYSTEM) {
                                            stringResource(R.string.language_system)
                                        } else {
                                            locale.nativeName
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    AnimatedVisibility(visible = !languageExpanded && AppLocale.sortedForPicker.size > collapsedLocales.size) {
                        Text(
                            text = stringResource(R.string.language_show_all),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable { languageExpanded = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            val packageInfo = remember {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            stringResource(
                                R.string.settings_version_label,
                                packageInfo.versionName ?: "",
                                versionCode
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider()
                    AboutLinkRow(
                        icon = Icons.Default.SystemUpdate,
                        title = stringResource(R.string.settings_update),
                        subtitle = if (checkingUpdate) {
                            stringResource(R.string.update_check_checking)
                        } else {
                            stringResource(R.string.settings_update_desc)
                        },
                        onClick = {
                            if (checkingUpdate) return@AboutLinkRow
                            scope.launch {
                                checkingUpdate = true
                                val result = withContext(Dispatchers.IO) {
                                    PlayUpdateChecker.checkDetailed(context)
                                }
                                checkingUpdate = false
                                when (result.status) {
                                    UpdateCheckStatus.AVAILABLE -> {
                                        manualUpdateInfo = result.info
                                    }
                                    UpdateCheckStatus.UP_TO_DATE -> {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.update_check_up_to_date),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    UpdateCheckStatus.UNAVAILABLE -> {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.update_check_unavailable),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                    AboutLinkRow(
                        icon = Icons.Default.HelpOutline,
                        title = stringResource(R.string.settings_help),
                        subtitle = stringResource(R.string.settings_help_desc),
                        onClick = onNavigateToHelp
                    )
                    HorizontalDivider()
                    AboutLinkRow(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.settings_rate_us),
                        subtitle = stringResource(R.string.settings_rate_us_desc),
                        onClick = { showRateDialog = true }
                    )
                    HorizontalDivider()
                    AboutLinkRow(
                        icon = Icons.Default.Share,
                        title = stringResource(R.string.settings_share),
                        subtitle = stringResource(R.string.settings_share_desc),
                        onClick = { PlayStoreHelper.shareApp(context) }
                    )
                    HorizontalDivider()
                    AboutLinkRow(
                        icon = Icons.Default.Policy,
                        title = stringResource(R.string.settings_privacy_policy),
                        subtitle = stringResource(R.string.settings_privacy_policy_desc),
                        onClick = onNavigateToPrivacy
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AboutLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_privacy_policy), fontWeight = FontWeight.Bold) },
        text = {
            androidx.compose.foundation.rememberScrollState().let { scrollState ->
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Türkçe Sözlük (com.hayhak.turkcesozluk) — Geliştirici / veri sorumlusu: ThunderCraft.\n\n" +
                                "1. Hesap / kimlik: Uygulama hesap istemez; ad, e-posta, konum vb. kişisel kimlik bilgisi toplamayız.\n\n" +
                                "2. Yerel saklama: Favoriler, arama geçmişi, eklenen kelimeler ve istatistikler yalnızca cihazınızda tutulur.\n\n" +
                                "3. İnternet: Yerelde bulunamayan kelimeler için aranan kelime metni TDK (sozluk.gov.tr) sitesine gönderilebilir.\n\n" +
                                "4. Bildirim ve sesli arama isteğe bağlıdır. Reklam veya analitik SDK kullanmayız.\n\n" +
                                "İletişim: hayhak1@gmail.com",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cd_close))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun WeeklyActivityCard(weeklyStats: List<Int>, previousWeekTotal: Int) {
    val days = listOf(
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat),
        stringResource(R.string.day_sun),
    )
    val fullDays = listOf(
        stringResource(R.string.day_mon_full),
        stringResource(R.string.day_tue_full),
        stringResource(R.string.day_wed_full),
        stringResource(R.string.day_thu_full),
        stringResource(R.string.day_fri_full),
        stringResource(R.string.day_sat_full),
        stringResource(R.string.day_sun_full),
    )

    val currentDayIndex = remember {
        java.util.Calendar.getInstance().let {
            val day = it.get(java.util.Calendar.DAY_OF_WEEK)
            if (day == java.util.Calendar.SUNDAY) 6 else day - 2
        }
    }

    var selectedIndex by remember { mutableIntStateOf(currentDayIndex) }
    val total = weeklyStats.sum()
    val average = if (weeklyStats.isNotEmpty()) total / 7f else 0f
    val deltaPercent = if (previousWeekTotal > 0) {
        (((total - previousWeekTotal).toFloat() / previousWeekTotal) * 100).roundToInt()
    } else null
    val peakIndex = remember(weeklyStats) {
        weeklyStats.withIndex().maxByOrNull { it.value }?.index ?: currentDayIndex
    }
    val selectedValue = weeklyStats.getOrElse(selectedIndex) { 0 }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            primary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = primary.copy(alpha = 0.18f),
                spotColor = primary.copy(alpha = 0.22f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(primary.copy(alpha = 0.25f), secondary.copy(alpha = 0.08f))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(primary, secondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            stringResource(R.string.weekly_activity),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.weekly_last_7_days),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (deltaPercent != null) {
                    val isUp = deltaPercent >= 0
                    val deltaColor = if (isUp) Color(0xFF10B981) else Color(0xFFEF4444)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(deltaColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = deltaColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "%${kotlin.math.abs(deltaPercent)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = deltaColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeeklyKpiChip(
                    value = total.toString(),
                    label = stringResource(R.string.weekly_total),
                    modifier = Modifier.weight(1f)
                )
                WeeklyKpiChip(
                    value = String.format("%.1f", average),
                    label = stringResource(R.string.weekly_average),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (total == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.weekly_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val maxVal = (weeklyStats.maxOrNull() ?: 1).coerceAtLeast(1)
                val avgFraction = (average / maxVal).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                        .padding(horizontal = 10.dp, vertical = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .fillMaxHeight(avgFraction.coerceAtLeast(0.02f))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            primary.copy(alpha = 0.35f),
                                            secondary.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyStats.forEachIndexed { index, value ->
                            val targetFraction =
                                if (value > 0) (value.toFloat() / maxVal).coerceAtLeast(0.10f) else 0.04f
                            val animatedFraction by animateFloatAsState(
                                targetValue = targetFraction,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "barHeight_$index"
                            )
                            val isSelected = index == selectedIndex
                            val isToday = index == currentDayIndex
                            val isPeak = index == peakIndex && value == maxVal && value > 0
                            val barWidth by animateDpAsState(
                                targetValue = if (isSelected) 26.dp else 18.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "barWidth_$index"
                            )
                            val barColor by animateColorAsState(
                                targetValue = when {
                                    isSelected -> primary
                                    isToday -> primary.copy(alpha = 0.72f)
                                    else -> primary.copy(alpha = 0.32f)
                                },
                                animationSpec = tween(220),
                                label = "barColor_$index"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedIndex = index },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(18.dp)
                                        .padding(bottom = 4.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    if (isSelected) {
                                        Text(
                                            value.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primary
                                        )
                                    } else if (isPeak) {
                                        Icon(
                                            Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = secondary.copy(alpha = 0.85f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .width(barWidth)
                                        .weight(1f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(50))
                                            .background(primary.copy(alpha = 0.08f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animatedFraction)
                                            .clip(RoundedCornerShape(50))
                                            .then(
                                                if (isSelected) {
                                                    Modifier.shadow(
                                                        elevation = 8.dp,
                                                        shape = RoundedCornerShape(50),
                                                        ambientColor = primary.copy(alpha = 0.45f),
                                                        spotColor = primary.copy(alpha = 0.55f)
                                                    )
                                                } else Modifier
                                            )
                                            .background(
                                                if (isSelected) {
                                                    Brush.verticalGradient(listOf(secondary, primary))
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(barColor.copy(alpha = 0.85f), barColor)
                                                    )
                                                }
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = days[index],
                                    fontSize = 10.sp,
                                    color = when {
                                        isSelected -> primary
                                        isToday -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) primary else Color.Transparent)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(primary.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.weekly_day_detail, fullDays[selectedIndex], selectedValue),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyKpiChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
            Text(title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
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
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

