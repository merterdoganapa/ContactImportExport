package com.mea.contact_import_export.ui.screens.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mea.contact_import_export.data.PremiumManager
import com.mea.contact_import_export.data.PurchaseResult
import com.mea.contact_import_export.data.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PremiumUiState(
    val isProUser: Boolean = false,
    val isPurchaseLoading: Boolean = false,
    val isRestoreLoading: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            premiumManager.isProUser.collect { isPro ->
                _uiState.update { it.copy(isProUser = isPro) }
            }
        }
    }

    fun buyPro(activity: Activity, onResult: (PurchaseResult) -> Unit) {
        if (_uiState.value.isPurchaseLoading) return
        _uiState.update { it.copy(isPurchaseLoading = true) }
        viewModelScope.launch {
            val result = try {
                if (_uiState.value.isProUser) {
                    PurchaseResult.AlreadyPro
                } else {
                    val isActive = premiumManager.purchaseRemoveAds(activity)
                    if (isActive) PurchaseResult.Success else PurchaseResult.NotActive
                }
            } catch (e: Exception) {
                PurchaseResult.Error(e.message)
            }
            _uiState.update { it.copy(isPurchaseLoading = false) }
            onResult(result)
        }
    }

    fun restorePurchases(onResult: (RestoreResult) -> Unit) {
        if (_uiState.value.isRestoreLoading) return
        _uiState.update { it.copy(isRestoreLoading = true) }
        viewModelScope.launch {
            val result = premiumManager.restorePurchases()
            _uiState.update { it.copy(isRestoreLoading = false) }
            onResult(result)
        }
    }
}
