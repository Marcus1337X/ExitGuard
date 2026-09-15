package com.exitguard.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exitguard.app.data.SettingsRepository
import com.exitguard.app.model.ExitInfo
import com.exitguard.app.network.ExitDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeApiUrl: String = SettingsRepository.DEFAULT_PRIMARY_API,
    val allApis: List<String> = SettingsRepository.PRESET_APIS,
    val isTesting: Boolean = false,
    val testUrl: String? = null,
    val testResult: Result<ExitInfo>? = null,
    val inputError: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exitDetectionService: ExitDetectionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.primaryApiFlow,
                settingsRepository.allApisFlow
            ) { primary, all ->
                primary to all
            }.collect { (primary, all) ->
                _uiState.update {
                    it.copy(
                        activeApiUrl = primary,
                        allApis = all
                    )
                }
            }
        }
    }

    fun selectApi(url: String) {
        viewModelScope.launch {
            settingsRepository.setPrimaryApi(url)
        }
    }

    fun addCustomApi(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return

        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            _uiState.update { it.copy(inputError = "URL 必须以 http:// 或 https:// 开头") }
            return
        }

        viewModelScope.launch {
            settingsRepository.addCustomApi(trimmed)
            settingsRepository.setPrimaryApi(trimmed)
            _uiState.update { it.copy(inputError = null) }
        }
    }

    fun removeCustomApi(url: String) {
        viewModelScope.launch {
            settingsRepository.removeCustomApi(url)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            settingsRepository.resetToDefault()
        }
    }

    fun testApi(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testUrl = url, testResult = null) }
            val result = exitDetectionService.testApiUrl(url)
            _uiState.update { it.copy(isTesting = false, testResult = result) }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null, testUrl = null) }
    }

    fun clearInputError() {
        _uiState.update { it.copy(inputError = null) }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val exitDetectionService: ExitDetectionService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, exitDetectionService) as T
        }
    }
}
