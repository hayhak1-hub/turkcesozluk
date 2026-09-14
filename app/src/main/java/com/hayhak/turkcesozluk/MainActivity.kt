package com.hayhak.turkcesozluk



import android.Manifest

import android.content.Context

import android.os.Build

import android.os.Bundle

import androidx.activity.compose.BackHandler

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.compose.setContent

import androidx.activity.enableEdgeToEdge

import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity

import androidx.compose.animation.*

import androidx.compose.animation.core.tween

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width

import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.Menu

import androidx.compose.material.icons.filled.Person

import androidx.compose.material.icons.filled.PlayArrow

import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable

import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.compose.rememberNavController

import com.hayhak.turkcesozluk.R
import com.hayhak.turkcesozluk.data.db.AppTheme
import com.hayhak.turkcesozluk.data.db.SettingsManager
import com.hayhak.turkcesozluk.data.repository.CoreRepository

import com.hayhak.turkcesozluk.ui.components.UpdateAvailableDialog

import com.hayhak.turkcesozluk.ui.screens.DictionaryScreen

import com.hayhak.turkcesozluk.ui.screens.FavoritesScreen

import com.hayhak.turkcesozluk.ui.screens.HelpScreen

import com.hayhak.turkcesozluk.ui.screens.LoadingScreen

import com.hayhak.turkcesozluk.ui.screens.PrivacyPolicyScreen

import com.hayhak.turkcesozluk.ui.screens.QuizScreen

import com.hayhak.turkcesozluk.ui.screens.StatsScreen

import com.hayhak.turkcesozluk.ui.theme.TurkceSozlukTheme

import com.hayhak.turkcesozluk.util.LocaleHelper

import com.hayhak.turkcesozluk.util.PlayUpdateChecker

import com.hayhak.turkcesozluk.util.PlayUpdateInfo

import com.hayhak.turkcesozluk.util.findActivity

import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext

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

class MainActivity : AppCompatActivity() {



    @Inject

    lateinit var coreRepository: CoreRepository



    override fun attachBaseContext(newBase: Context) {

        super.attachBaseContext(LocaleHelper.wrap(newBase, LocaleHelper.savedTag(newBase)))

    }



    override fun onCreate(savedInstanceState: Bundle?) {

        LocaleHelper.applyAppLocale(LocaleHelper.savedTag(this))

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



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun MainScreen() {

    val navController = rememberNavController()

    val context = LocalContext.current

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }

    var startupUpdateInfo by remember { mutableStateOf<PlayUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = withContext(Dispatchers.IO) {
            PlayUpdateChecker.checkForStartupPrompt(context)
        }
        if (info != null) {
            startupUpdateInfo = info
        }
    }

    startupUpdateInfo?.let { info ->
        UpdateAvailableDialog(
            updateInfo = info,
            onDismiss = { startupUpdateInfo = null },
        )
    }

    val items = listOf(

        Screen.Dictionary,

        Screen.Quiz,

        Screen.Favorites,

        Screen.Profile

    )



    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    val showDrawer = currentRoute in items.map { it.route }

    val currentScreen = items.find { it.route == currentRoute }



    BackHandler(enabled = drawerState.isOpen) {

        scope.launch { drawerState.close() }

    }



    BackHandler(enabled = !drawerState.isOpen && navController.previousBackStackEntry == null) {

        showExitDialog = true

    }



    if (showExitDialog) {

        AlertDialog(

            onDismissRequest = { showExitDialog = false },

            title = { Text(stringResource(R.string.exit_dialog_title)) },

            text = { Text(stringResource(R.string.exit_dialog_message)) },

            confirmButton = {

                TextButton(onClick = { context.findActivity()?.finish() }) {

                    Text(stringResource(R.string.exit_dialog_confirm))

                }

            },

            dismissButton = {

                TextButton(onClick = { showExitDialog = false }) {

                    Text(stringResource(R.string.exit_dialog_cancel))

                }

            },

            shape = RoundedCornerShape(24.dp)

        )

    }



    ModalNavigationDrawer(

        drawerState = drawerState,

        gesturesEnabled = showDrawer,

        drawerContent = {

            ModalDrawerSheet(

                modifier = Modifier
                    .width(280.dp)
                    .navigationBarsPadding()

            ) {

                Spacer(Modifier.statusBarsPadding())

                Text(

                    text = stringResource(R.string.app_name),

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    color = MaterialTheme.colorScheme.primary,

                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)

                )

                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                items.forEach { screen ->

                    NavigationDrawerItem(

                        icon = { Icon(screen.icon, contentDescription = null) },

                        label = {
                            Text(
                                text = stringResource(screen.titleRes),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },

                        selected = currentRoute == screen.route,

                        onClick = {

                            scope.launch { drawerState.close() }

                            if (currentRoute != screen.route) {

                                navController.navigate(screen.route) {

                                    popUpTo(navController.graph.startDestinationId)

                                    launchSingleTop = true

                                }

                            }

                        },

                        modifier = Modifier.padding(horizontal = 12.dp)

                    )

                }

            }

        }

    ) {

        Scaffold(

            modifier = Modifier.fillMaxSize(),

            topBar = {

                if (showDrawer && currentScreen != null) {

                    TopAppBar(

                        title = {
                            Text(
                                text = stringResource(currentScreen.titleRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },

                        navigationIcon = {

                            IconButton(onClick = { scope.launch { drawerState.open() } }) {

                                Icon(

                                    Icons.Default.Menu,

                                    contentDescription = stringResource(R.string.cd_open_menu)

                                )

                            }

                        },

                        actions = {
                            AppThemeToggleButton()
                        },

                        colors = TopAppBarDefaults.topAppBarColors(

                            containerColor = MaterialTheme.colorScheme.surface,

                            titleContentColor = MaterialTheme.colorScheme.primary

                        )

                    )

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

}

@Composable
private fun AppThemeToggleButton() {
    val context = LocalContext.current
    val appTheme by SettingsManager.themeState.collectAsState()
    val isDark = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    IconButton(
        onClick = {
            SettingsManager.setTheme(
                context,
                if (isDark) AppTheme.LIGHT else AppTheme.DARK
            )
        }
    ) {
        Icon(
            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = stringResource(
                if (isDark) R.string.theme_light else R.string.theme_dark
            )
        )
    }
}

