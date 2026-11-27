package com.wavez.base.ads

import android.util.Log
import com.wavez.base.storage.SharedPref
import com.google.android.gms.ads.nativead.NativeAd
import java.util.Locale

/**
 * Detects devices registered as test devices in the AdMob console. Such devices request real ad
 * unit ids but AdMob serves them test creatives, whose native headline is labelled "Test Ad"
 * (or "Test mode"). Only native ads expose their content, so detection runs on native loads.
 */
object AdTestDeviceDetector {

    private const val TAG = "AdTestDeviceDetector"
    private const val ADMOB_ADAPTER = "com.google.ads.mediation.admob.AdMobAdapter"

    /**
     * AdMob localizes the test creative label to the device language ("Test Ad : <headline>",
     * "Quảng cáo thử nghiệm : <headline>", ...). Stored normalized: lowercase, no whitespace.
     */
    private val TEST_LABELS = listOf(
        "Test Ad", "Test mode", "Anuncio de prueba", "Annonce test", "테스트 광고",
        "Annuncio di testo", "Testanzeige", "Tes Iklan", "Quảng cáo thử nghiệm",
        "Anúncio de teste", "পরীক্ষামূলক বিজ্ঞাপন", "जाँच विज्ञापन",
        "إعلان تجريبي", "Тестовое объявление", "测试广告", "測試廣告", "テスト広告",
        "โฆษณาทดสอบ", "Test Reklamı", "Testadvertentie", "Reklama testowa",
    ).map(::normalize).toSet()

    private fun normalize(text: String): String =
        text.filterNot(Char::isWhitespace).lowercase(Locale.ROOT)

    /**
     * Compares the label before ':' exactly, so a real ad such as "Test Advanced..." does not
     * match the way a plain contains("test ad") would.
     */
    fun isTestAd(ad: NativeAd): Boolean {
        val texts = listOfNotNull(ad.headline, ad.body, ad.advertiser)
        return texts.any { normalize(it.substringBefore(':')) in TEST_LABELS }
    }

    /**
     * Marks the device as an AdMob test device when [ad] is a test ad. The flag is cleared only
     * by a real ad served by AdMob itself (a mediated network ad says nothing about the AdMob
     * test-device list), so it follows the device being removed from the console.
     */
    fun update(ad: NativeAd, sharedPref: SharedPref) {
        Log.d(TAG, "DEBUG headline=${ad.headline} | body=${ad.body} | advertiser=${ad.advertiser} | cta=${ad.callToAction} | store=${ad.store} | price=${ad.price}")
        Log.d(TAG, "DEBUG extras=${ad.extras} | responseExtras=${ad.responseInfo?.responseExtras}")
        Log.d(TAG, "DEBUG responseInfo=${ad.responseInfo}")
        val isTest = isTestAd(ad)
        val servedByAdmob = ad.responseInfo?.loadedAdapterResponseInfo?.adapterClassName == ADMOB_ADAPTER
        val newValue = when {
            isTest -> true
            servedByAdmob -> false
            else -> return
        }
        if (sharedPref.isAdmobTestDevice != newValue) {
            Log.d(TAG, "isAdmobTestDevice: ${sharedPref.isAdmobTestDevice} -> $newValue (headline=${ad.headline})")
            sharedPref.isAdmobTestDevice = newValue
        }
    }
}
