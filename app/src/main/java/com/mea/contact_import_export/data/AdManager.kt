package com.mea.contact_import_export.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mea.contact_import_export.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val premiumManager: PremiumManager
) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        scope.launch {
            premiumManager.isProUser.collect { isPro ->
                if (isPro) {
                    rewardedAd = null
                    isLoading = false
                } else {
                    MobileAds.initialize(context)
                    loadRewardedAd()
                }
            }
        }
    }

    private fun loadRewardedAd() {
        if (isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        val adUnitId = BuildConfig.REWARDED_AD_UNIT_ID

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Ad failed to load: $adError")
                    rewardedAd = null
                    isLoading = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad
                    isLoading = false
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdClosed: () -> Unit,
        onAdFailedToShow: () -> Unit
    ) {
        if (premiumManager.isProUser.value) {
            onRewarded()
            onAdClosed()
            return
        }

        val rewardedAd = this.rewardedAd

        if (rewardedAd == null) {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            onAdFailedToShow()
            loadRewardedAd() // Try to load a new ad
            return
        }

        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                this@AdManager.rewardedAd = null
                loadRewardedAd() // Load the next rewarded ad
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show: $adError")
                this@AdManager.rewardedAd = null
                onAdFailedToShow()
                loadRewardedAd() // Try to load a new ad
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

        rewardedAd.show(
            activity,
            OnUserEarnedRewardListener { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
                onRewarded()
            }
        )
    }

    fun isRewardedAdAvailable(): Boolean {
        if (premiumManager.isProUser.value) return true
        return rewardedAd != null
    }

    companion object {
        private const val TAG = "AdManager"
    }
}