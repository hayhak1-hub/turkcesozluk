package com.hayhak.esanlamli.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * "premium_lifetime" tek seferlik, hiçbir özelliği kilitlemeyen bir destek/bağış satın alımıdır.
 * Bu ürünün Google Play Console'da aynı ID ile bir yönetilen ürün (managed product) olarak
 * tanımlanması gerekir, aksi halde queryProductDetails boş döner.
 */
const val PREMIUM_PRODUCT_ID = "premium_lifetime"

private const val TAG = "PremiumManager"

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _priceText = MutableStateFlow<String?>(null)
    val priceText = _priceText.asStateFlow()

    private var cachedProductDetails: ProductDetails? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { handlePurchases(purchases) }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun start() {
        scope.launch {
            if (ensureConnected()) {
                loadProductDetails()
                syncOwnedPurchases()
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        scope.launch {
            if (cachedProductDetails == null && ensureConnected()) {
                loadProductDetails()
            }
            val details = cachedProductDetails ?: return@launch
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build()
                    )
                )
                .build()
            withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, params)
            }
        }
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing service disconnected")
                }
            })
        }
    }

    private suspend fun loadProductDetails() {
        try {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()
            val result = billingClient.queryProductDetails(params)
            val details = result.productDetailsList?.firstOrNull()
            cachedProductDetails = details
            _priceText.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
        } catch (e: Exception) {
            Log.e(TAG, "Error loading product details", e)
        }
    }

    private suspend fun syncOwnedPurchases() {
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val result = billingClient.queryPurchasesAsync(params)
            handlePurchases(result.purchasesList)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying owned purchases", e)
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        _isPremium.value = purchases.any { purchase ->
            purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        purchases.forEach { purchase ->
            val isOwnedPremium = purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            if (isOwnedPremium && !purchase.isAcknowledged) {
                try {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams)
                } catch (e: Exception) {
                    Log.e(TAG, "Error acknowledging purchase", e)
                }
            }
        }
    }
}
