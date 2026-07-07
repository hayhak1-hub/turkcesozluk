package com.hayhak.esanlamli

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hayhak.esanlamli.data.repository.CoreRepository
import com.hayhak.esanlamli.ui.screens.DictionaryScreen
import com.hayhak.esanlamli.ui.screens.FavoritesScreen
import com.hayhak.esanlamli.ui.screens.LoadingScreen
import com.hayhak.esanlamli.ui.screens.QuizScreen
import com.hayhak.esanlamli.ui.screens.StatsScreen
import com.hayhak.esanlamli.ui.theme.EsAnlamliTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen(val route: String, val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dictionary : Screen("dictionary", R.string.tab_dictionary, Icons.Default.Search)
    object Quiz : Screen("quiz", R.string.tab_quiz, Icons.Default.PlayArrow)
    object Favorites : Screen("favorites", R.string.tab_favorites, Icons.Default.Favorite)
    object Profile : Screen("profile", R.string.tab_profile, Icons.Default.Person)
    object Learning : Screen("learning", R.string.tab_learning, Icons.AutoMirrored.Filled.MenuBook)
}

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
            val appTheme by com.hayhak.esanlamli.data.db.SettingsManager.themeState.collectAsState()
            
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            EsAnlamliTheme(appTheme = appTheme) {
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
    val items = listOf(
        Screen.Dictionary,
        Screen.Quiz,
        Screen.Favorites,
        Screen.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dictionary.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dictionary.route) { 
                DictionaryScreen(onNavigateToLearning = { navController.navigate(Screen.Learning.route) }) 
            }
            composable(Screen.Quiz.route) { QuizScreen() }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Profile.route) { StatsScreen() }
            composable(Screen.Learning.route) { 
                com.hayhak.esanlamli.ui.screens.LearningScreen(
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(),
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
