package com.mea.contact_import_export.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.mea.contact_import_export.BuildConfig
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.awaitGetProducts
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()
    private val _appUserId = MutableStateFlow("")
    val appUserId: StateFlow<String> = _appUserId.asStateFlow()

    init {
        configureIfNeeded()
        refreshCustomerInfo()
        if (Purchases.isConfigured) {
            _appUserId.value = Purchases.sharedInstance.appUserID.orEmpty()
        }
    }

    suspend fun purchaseRemoveAds(activity: Activity): Boolean {
        if (!isPurchasesReady()) return false
        val products = Purchases.sharedInstance.awaitGetProducts(listOf(BuildConfig.REVENUECAT_PRO_PRODUCT_ID))
        val targetProduct = products.firstOrNull() ?: return false
        val purchaseResult = Purchases.sharedInstance.awaitPurchase(
            PurchaseParams.Builder(
                activity = activity,
                storeProduct = targetProduct,
            ).build()
        )
        val isActive = purchaseResult.customerInfo.entitlements[BuildConfig.REVENUECAT_ENTITLEMENT_PRO_ID]?.isActive == true
        _isProUser.value = isActive
        return isActive
    }

    suspend fun restorePurchases(): RestoreResult {
        if (!isPurchasesReady()) {
            return RestoreResult.Error("RevenueCat is not configured.")
        }
        val customerInfo = suspendCancellableCoroutine<CustomerInfo?> { continuation ->
            Purchases.sharedInstance.restorePurchases(
                object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        if (continuation.isActive) continuation.resume(customerInfo)
                    }

                    override fun onError(error: PurchasesError) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            )
        } ?: return RestoreResult.NotFound

        updateProState(customerInfo)
        return if (_isProUser.value) RestoreResult.Success else RestoreResult.NotFound
    }

    fun refreshCustomerInfo() {
        if (!isPurchasesReady()) return
        Purchases.sharedInstance.getCustomerInfo(
            object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    updateProState(customerInfo)
                    _appUserId.value = customerInfo.originalAppUserId
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Failed to refresh customer info: ${error.message}")
                }
            }
        )
    }

    private fun configureIfNeeded() {
        if (Purchases.isConfigured) return

        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank()) {
            Log.w(TAG, "RevenueCat API key is empty. Premium purchases are disabled.")
            return
        }

        Purchases.configure(
            PurchasesConfiguration.Builder(context, apiKey).build()
        )
    }

    private fun isPurchasesReady(): Boolean {
        if (!Purchases.isConfigured) {
            configureIfNeeded()
        }
        return Purchases.isConfigured
    }

    private fun updateProState(customerInfo: CustomerInfo) {
        val entitlementId = BuildConfig.REVENUECAT_ENTITLEMENT_PRO_ID
        val entitlement = customerInfo.entitlements[entitlementId]
        _isProUser.value = entitlement?.isActive == true
        _appUserId.value = customerInfo.originalAppUserId
    }

    companion object {
        private const val TAG = "PremiumManager"
    }
}

sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object AlreadyPro : PurchaseResult
    data object NotActive : PurchaseResult
    data class Error(val message: String? = null) : PurchaseResult
}

sealed interface RestoreResult {
    data object Success : RestoreResult
    data object NotFound : RestoreResult
    data class Error(val message: String) : RestoreResult
}
