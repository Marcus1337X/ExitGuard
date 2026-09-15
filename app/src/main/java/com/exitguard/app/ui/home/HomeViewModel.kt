package com.exitguard.app.ui.home

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exitguard.app.data.AppRepository
import com.exitguard.app.data.RuleRepository
import com.exitguard.app.model.AppRule
import com.exitguard.app.model.CheckResult
import com.exitguard.app.model.ExitInfo
import com.exitguard.app.model.RuleChecker
import com.exitguard.app.network.ExitDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppRuleItem(
    val rule: AppRule,
    val icon: Drawable? = null
)

sealed interface LaunchDialogState {
    data object Idle : LaunchDialogState
    data class Checking(val appName: String) : LaunchDialogState
    data class Blocked(
        val appName: String,
        val reason: String,
        val exitInfo: ExitInfo? = null,
        val rule: AppRule? = null
    ) : LaunchDialogState
}

data class HomeUiState(
    val appItems: List<AppRuleItem> = emptyList(),
    val currentExitInfo: ExitInfo? = null,
    val isDetectingExit: Boolean = false,
    val exitDetectionError: String? = null,
    val launchDialogState: LaunchDialogState = LaunchDialogState.Idle
)

class HomeViewModel(
    private val ruleRepository: RuleRepository,
    private val appRepository: AppRepository,
    private val exitDetectionService: ExitDetectionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeRules()
        refreshExitInfo()
    }

    private fun observeRules() {
        viewModelScope.launch {
            ruleRepository.rulesFlow.collect { rules ->
                val items = rules.map { rule ->
                    AppRuleItem(
                        rule = rule,
                        icon = appRepository.getAppIcon(rule.packageName)
                    )
                }
                _uiState.update { it.copy(appItems = items) }
            }
        }
    }

    fun refreshExitInfo() {
        if (_uiState.value.isDetectingExit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingExit = true, exitDetectionError = null) }
            val result = exitDetectionService.detectExit()
            result.fold(
                onSuccess = { info ->
                    _uiState.update {
                        it.copy(
                            currentExitInfo = info,
                            isDetectingExit = false,
                            exitDetectionError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDetectingExit = false,
                            exitDetectionError = error.message ?: "检测失败"
                        )
                    }
                }
            )
        }
    }

    fun onAppClicked(item: AppRuleItem, launchActivity: (Intent) -> Unit) {
        val rule = item.rule

        // Fast check for empty rules to prevent unnecessary network calls
        when (rule.mode) {
            com.exitguard.app.model.CheckMode.IP_STRICT -> {
                if (rule.allowedIps.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            launchDialogState = LaunchDialogState.Blocked(
                                appName = rule.appName,
                                reason = "该应用尚未配置任何允许的出口 IP 规则，安全起见已禁止启动。",
                                exitInfo = null,
                                rule = rule
                            )
                        )
                    }
                    return
                }
            }
            com.exitguard.app.model.CheckMode.COUNTRY -> {
                if (rule.allowedCountries.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            launchDialogState = LaunchDialogState.Blocked(
                                appName = rule.appName,
                                reason = "该应用尚未配置任何允许的国家规则，安全起见已禁止启动。",
                                exitInfo = null,
                                rule = rule
                            )
                        )
                    }
                    return
                }
            }
        }

        // Perform real-time detection & security check
        viewModelScope.launch {
            _uiState.update {
                it.copy(launchDialogState = LaunchDialogState.Checking(appName = rule.appName))
            }

            val detectionResult = exitDetectionService.detectExit()

            detectionResult.fold(
                onSuccess = { exitInfo ->
                    // Update latest cached exit info as well
                    _uiState.update { it.copy(currentExitInfo = exitInfo) }

                    when (val checkResult = RuleChecker.evaluate(rule, exitInfo)) {
                        is CheckResult.Allowed -> {
                            _uiState.update { it.copy(launchDialogState = LaunchDialogState.Idle) }
                            val intent = appRepository.getLaunchIntent(rule.packageName)
                            if (intent != null) {
                                launchActivity(intent)
                            } else {
                                _uiState.update {
                                    it.copy(
                                        launchDialogState = LaunchDialogState.Blocked(
                                            appName = rule.appName,
                                            reason = "无法创建启动意向，目标应用可能已被卸载。",
                                            exitInfo = exitInfo,
                                            rule = rule
                                        )
                                    )
                                }
                            }
                        }

                        is CheckResult.Denied -> {
                            _uiState.update {
                                it.copy(
                                    launchDialogState = LaunchDialogState.Blocked(
                                        appName = rule.appName,
                                        reason = checkResult.reason,
                                        exitInfo = exitInfo,
                                        rule = rule
                                    )
                                )
                            }
                        }

                        is CheckResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    launchDialogState = LaunchDialogState.Blocked(
                                        appName = rule.appName,
                                        reason = checkResult.message,
                                        exitInfo = exitInfo,
                                        rule = rule
                                    )
                                )
                            }
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            launchDialogState = LaunchDialogState.Blocked(
                                appName = rule.appName,
                                reason = "出口检测异常（超时或无网络）：${error.localizedMessage ?: "连接失败"}。根据安全策略已阻止启动。",
                                exitInfo = null,
                                rule = rule
                            )
                        )
                    }
                }
            )
        }
    }

    fun deleteApp(packageName: String) {
        viewModelScope.launch {
            ruleRepository.deleteRule(packageName)
        }
    }

    fun dismissLaunchDialog() {
        _uiState.update { it.copy(launchDialogState = LaunchDialogState.Idle) }
    }

    class Factory(
        private val ruleRepository: RuleRepository,
        private val appRepository: AppRepository,
        private val exitDetectionService: ExitDetectionService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(ruleRepository, appRepository, exitDetectionService) as T
        }
    }
}
