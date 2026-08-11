package com.hayhak.turkcesozluk

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hayhak.turkcesozluk.data.repository.CoreRepository
import com.hayhak.turkcesozluk.ui.screens.DictionaryScreen
import com.hayhak.turkcesozluk.ui.screens.FavoritesScreen
import com.hayhak.turkcesozluk.ui.screens.HelpScreen
import com.hayhak.turkcesozluk.ui.screens.LoadingScreen
import com.hayhak.turkcesozluk.ui.screens.PrivacyPolicyScreen
import com.hayhak.turkcesozluk.ui.screens.QuizScreen
import com.hayhak.turkcesozluk.ui.screens.StatsScreen
import com.hayhak.turkcesozluk.ui.theme.TurkceSozlukTheme
import com.hayhak.turkcesozluk.util.findActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen(val route: String, val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dictionary : Screen("dictionary", R.string.tab_dictionary, Icons.Default.Search)
    object Quiz : Screen("quiz", R.string.tab_quiz, Icons.Default.PlayArrow)
    object Favorites : Screen("favorites", R.string.tab_favorites, Icons.Default.Favorite)
    object Profile : Screen("profile", R.string.tab_profile, Icons.Default.Person)
}

private const val ROUTE_HELP = "help"
private const val ROUTE_PRIVACY = "privacy_policy"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var coreRepository: CoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val isInitialized by coreRepository.isInitialized.collectAsState()
            val appTheme by com.hayhak.turkcesozluk.data.db.SettingsManager.themeState.collectAsState()
            
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            TurkceSozlukTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isInitialized) {
                        MainScreen()
                    } else {
                        LoadingScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    val items = listOf(
        Screen.Dictionary,
        Screen.Quiz,
        Screen.Favorites,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in items.map { it.route }

    BackHandler(enabled = navController.previousBackStackEntry == null) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Çıkmak istiyor musunuz?") },
            text = { Text("Türkçe Sözlük uygulamasından çıkmak üzeresiniz.") },
            confirmButton = {
                TextButton(onClick = { context.findActivity()?.finish() }) {
                    Text("Çık")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Vazgeç")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.titleRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dictionary.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.98f) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.98f) },
            popExitTransition = { fadeOut(tween(120)) }
        ) {
            composable(Screen.Dictionary.route) { DictionaryScreen() }
            composable(Screen.Quiz.route) { QuizScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Profile.route) {
                StatsScreen(
                    onNavigateToHelp = { navController.navigate(ROUTE_HELP) },
                    onNavigateToPrivacy = { navController.navigate(ROUTE_PRIVACY) }
                )
            }
            composable(ROUTE_HELP) {
                HelpScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_PRIVACY) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
