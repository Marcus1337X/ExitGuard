package com.exitguard.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exitguard.app.ExitGuardApplication
import com.exitguard.app.ui.add.AddAppScreen
import com.exitguard.app.ui.add.AddAppViewModel
import com.exitguard.app.ui.config.ConfigScreen
import com.exitguard.app.ui.config.ConfigViewModel
import com.exitguard.app.ui.home.HomeScreen
import com.exitguard.app.ui.home.HomeViewModel
import com.exitguard.app.ui.theme.ExitGuardTheme

sealed interface Screen {
    data object Home : Screen
    data object AddApp : Screen
    data class Config(val packageName: String) : Screen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ExitGuardApplication
        val ruleRepository = app.ruleRepository
        val appRepository = app.appRepository
        val exitDetectionService = app.exitDetectionService

        setContent {
            ExitGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            val homeViewModel: HomeViewModel = viewModel(
                                factory = HomeViewModel.Factory(
                                    ruleRepository = ruleRepository,
                                    appRepository = appRepository,
                                    exitDetectionService = exitDetectionService
                                )
                            )

                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToAdd = { currentScreen = Screen.AddApp },
                                onNavigateToConfig = { pkg -> currentScreen = Screen.Config(pkg) },
                                onLaunchIntent = { intent ->
                                    try {
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this,
                                            "启动失败: ${e.localizedMessage ?: "未知错误"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }

                        is Screen.AddApp -> {
                            BackHandler {
                                currentScreen = Screen.Home
                            }

                            val addAppViewModel: AddAppViewModel = viewModel(
                                factory = AddAppViewModel.Factory(
                                    appRepository = appRepository,
                                    ruleRepository = ruleRepository
                                )
                            )

                            AddAppScreen(
                                viewModel = addAppViewModel,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }

                        is Screen.Config -> {
                            BackHandler {
                                currentScreen = Screen.Home
                            }

                            val configViewModel: ConfigViewModel = viewModel(
                                key = screen.packageName,
                                factory = ConfigViewModel.Factory(
                                    packageName = screen.packageName,
                                    ruleRepository = ruleRepository,
                                    appRepository = appRepository,
                                    exitDetectionService = exitDetectionService
                                )
                            )

                            ConfigScreen(
                                viewModel = configViewModel,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
