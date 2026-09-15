package com.exitguard.app.ui.config

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exitguard.app.data.AppRepository
import com.exitguard.app.data.RuleRepository
import com.exitguard.app.model.AppRule
import com.exitguard.app.model.CheckMode
import com.exitguard.app.model.CheckResult
import com.exitguard.app.model.ExitInfo
import com.exitguard.app.model.RuleChecker
import com.exitguard.app.network.ExitDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfigUiState(
    val packageName: String = "",
    val appName: String = "",
    val icon: Drawable? = null,
    val mode: CheckMode = CheckMode.IP_STRICT,
    val allowedIps: Set<String> = emptySet(),
    val allowedCountries: Set<String> = emptySet(),
    val initialMode: CheckMode = CheckMode.IP_STRICT,
    val initialAllowedIps: Set<String> = emptySet(),
    val initialAllowedCountries: Set<String> = emptySet(),
    val currentExitInfo: ExitInfo? = null,
    val isDetectingExit: Boolean = false,
    val isSavedMessageVisible: Boolean = false,
    val inputError: String? = null
) {
    val hasUnsavedChanges: Boolean
        get() = mode != initialMode || allowedIps != initialAllowedIps || allowedCountries != initialAllowedCountries

    val currentAppRule: AppRule
        get() = AppRule(
            packageName = packageName,
            appName = appName,
            mode = mode,
            allowedIps = allowedIps,
            allowedCountries = allowedCountries
        )

    val currentMatchEvaluation: CheckResult?
        get() = currentExitInfo?.let { RuleChecker.evaluate(currentAppRule, it) }
}

class ConfigViewModel(
    private val packageName: String,
    private val ruleRepository: RuleRepository,
    private val appRepository: AppRepository,
    private val exitDetectionService: ExitDetectionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState(packageName = packageName))
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        loadAppAndRule()
        refreshExitInfo()
    }

    private fun loadAppAndRule() {
        val appName = appRepository.getAppName(packageName)
        val icon = appRepository.getAppIcon(packageName)

        viewModelScope.launch {
            val existingRule = ruleRepository.getRule(packageName)
            if (existingRule != null) {
                _uiState.update {
                    it.copy(
                        appName = existingRule.appName.ifEmpty { appName },
                        icon = icon,
                        mode = existingRule.mode,
                        allowedIps = existingRule.allowedIps,
                        allowedCountries = existingRule.allowedCountries,
                        initialMode = existingRule.mode,
                        initialAllowedIps = existingRule.allowedIps,
                        initialAllowedCountries = existingRule.allowedCountries
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        appName = appName,
                        icon = icon,
                        mode = CheckMode.IP_STRICT,
                        initialMode = CheckMode.IP_STRICT,
                        initialAllowedIps = emptySet(),
                        initialAllowedCountries = emptySet()
                    )
                }
            }
        }
    }

    fun refreshExitInfo() {
        if (_uiState.value.isDetectingExit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingExit = true) }
            val result = exitDetectionService.detectExit()
            result.fold(
                onSuccess = { info ->
                    _uiState.update {
                        it.copy(
                            currentExitInfo = info,
                            isDetectingExit = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isDetectingExit = false) }
                }
            )
        }
    }

    fun setMode(mode: CheckMode) {
        _uiState.update { it.copy(mode = mode, inputError = null) }
    }

    fun addIp(ip: String) {
        val trimmed = ip.trim()
        if (trimmed.isEmpty()) return

        // Basic IPv4 or IPv6 validation
        val isValidIp = trimmed.matches(Regex("^[0-9a-fA-F.:]+$"))
        if (!isValidIp) {
            _uiState.update { it.copy(inputError = "请输入合法的 IP 地址") }
            return
        }

        _uiState.update {
            it.copy(
                allowedIps = it.allowedIps + trimmed,
                inputError = null
            )
        }
    }

    fun removeIp(ip: String) {
        _uiState.update {
            it.copy(allowedIps = it.allowedIps - ip)
        }
    }

    fun addCurrentExitIp() {
        val currentIp = _uiState.value.currentExitInfo?.ip?.trim() ?: return
        if (currentIp.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    allowedIps = it.allowedIps + currentIp,
                    inputError = null
                )
            }
        }
    }

    fun addCountry(countryCode: String) {
        val trimmed = countryCode.trim().uppercase()
        if (trimmed.isEmpty()) return

        if (!trimmed.matches(Regex("^[A-Z]{2}$"))) {
            _uiState.update { it.copy(inputError = "请输入 2 位地区代码，例如 US、JP、SG") }
            return
        }

        _uiState.update {
            it.copy(
                allowedCountries = it.allowedCountries + trimmed,
                inputError = null
            )
        }
    }

    fun removeCountry(countryCode: String) {
        val trimmed = countryCode.trim().uppercase()
        _uiState.update {
            it.copy(allowedCountries = it.allowedCountries - trimmed)
        }
    }

    fun addCurrentExitCountry() {
        val currentCountry = _uiState.value.currentExitInfo?.countryCode?.trim()?.uppercase() ?: return
        if (currentCountry.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    allowedCountries = it.allowedCountries + currentCountry,
                    inputError = null
                )
            }
        }
    }

    fun clearInputError() {
        _uiState.update { it.copy(inputError = null) }
    }

    fun saveRule(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val state = _uiState.value
            val rule = state.currentAppRule
            ruleRepository.saveRule(rule)
            _uiState.update {
                it.copy(
                    initialMode = rule.mode,
                    initialAllowedIps = rule.allowedIps,
                    initialAllowedCountries = rule.allowedCountries,
                    isSavedMessageVisible = true
                )
            }
            onSaved()
        }
    }

    fun hideSavedMessage() {
        _uiState.update { it.copy(isSavedMessageVisible = false) }
    }

    class Factory(
        private val packageName: String,
        private val ruleRepository: RuleRepository,
        private val appRepository: AppRepository,
        private val exitDetectionService: ExitDetectionService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfigViewModel(packageName, ruleRepository, appRepository, exitDetectionService) as T
        }
    }
}
