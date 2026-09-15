package com.exitguard.app.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.exitguard.app.data.AppRepository
import com.exitguard.app.data.InstalledApp
import com.exitguard.app.data.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddAppUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val allApps: List<InstalledApp> = emptyList(),
    val existingPackages: Set<String> = emptySet(),
    val selectedPackages: Set<String> = emptySet()
) {
    val filteredApps: List<InstalledApp>
        get() {
            if (searchQuery.isBlank()) return allApps
            val query = searchQuery.trim().lowercase()
            return allApps.filter {
                it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }
}

class AddAppViewModel(
    private val appRepository: AppRepository,
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAppUiState())
    val uiState: StateFlow<AddAppUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val existing = ruleRepository.getRules().map { it.packageName }.toSet()
            val installed = appRepository.getInstalledLaunchableApps()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allApps = installed,
                    existingPackages = existing,
                    selectedPackages = emptySet()
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSelection(packageName: String) {
        _uiState.update { state ->
            if (state.existingPackages.contains(packageName)) {
                state
            } else {
                val updated = if (state.selectedPackages.contains(packageName)) {
                    state.selectedPackages - packageName
                } else {
                    state.selectedPackages + packageName
                }
                state.copy(selectedPackages = updated)
            }
        }
    }

    fun selectAllFiltered() {
        _uiState.update { state ->
            val selectable = state.filteredApps
                .filter { it.packageName !in state.existingPackages }
                .map { it.packageName }
            state.copy(selectedPackages = state.selectedPackages + selectable)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedPackages = emptySet()) }
    }

    fun addSelectedApps(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val toAdd = state.allApps.filter { it.packageName in state.selectedPackages }
            if (toAdd.isNotEmpty()) {
                ruleRepository.addApps(toAdd)
            }
            onSuccess()
        }
    }

    class Factory(
        private val appRepository: AppRepository,
        private val ruleRepository: RuleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddAppViewModel(appRepository, ruleRepository) as T
        }
    }
}
