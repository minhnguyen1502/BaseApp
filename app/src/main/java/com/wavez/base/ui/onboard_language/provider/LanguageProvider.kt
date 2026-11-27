package com.wavez.base.ui.onboard_language.provider

import android.content.Context
import com.wavez.base.R
import com.wavez.base.ui.onboard_language.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LanguageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {

        // Danh sách 70+ ngôn ngữ
        val localeTranslateDefaults = listOf(
            "af",
            "am",
            "az",
            "be",
            "bn",
            "bn-rBD",
            "ca",
            "ceb",
            "cs",
            "et",
            "fa",
            "gl",
            "gu",
            "ha",
            "ga",
            "hr",
            "hu",
            "ig",
            "in",
            "jv",
            "kk",
            "km",
            "kn",
            "ml",
            "mr",
            "ms",
            "my",
            "ne",
            "nl",
            "pa",
            "ps",
            "ro",
            "sd",
            "si",
            "sk",
            "sl",
            "sr",
            "su",
            "ta",
            "te",
            "tg",
            "uk",
            "ur",
            "uz",
            "yo",
            "ar",
            "da",
            "de",
            "el",
            "es",
            "es-rES",
            "es-rUS",
            "ru",
            "it",
            "ja",
            "fi",
            "tl",
            "fr",
            "ko",
            "fr-rCA",
            "hi",
            "no",
            "vi",
            "pt",
            "pt-rBR",
            "pl",
            "sv",
            "th",
            "tr",
            "zh",
            "zh-rCN",
            "zh-rHK",
            "zh-rTW",
            "en"
        )

        // Danh sách ngôn ngữ riêng (có thẻ riêng)
        val supportedLanguages = listOf(
            Language("en", R.string.lan_english_na, R.string.lan_english_en),
            Language("es", R.string.lan_spanish_na, R.string.lan_spanish_en),
            Language("de", R.string.lan_german_na, R.string.lan_german_en),
            Language("pt", R.string.lan_portuguese_na, R.string.lan_portuguese_en),
            Language("hi", R.string.lan_hindi_na, R.string.lan_hindi_en),
            Language("vi", R.string.lan_vietnamese_na, R.string.lan_vietnamese_en),
            Language("ar", R.string.lan_arabic_na, R.string.lan_arabic_en),
            Language("fr", R.string.lan_french_na, R.string.lan_french_en),
            Language("in", R.string.lan_indonesian_na, R.string.lan_indonesian_en),
            Language("fil", R.string.lan_filipino_na, R.string.lan_filipino_en),
            Language("th", R.string.lan_thai_na, R.string.lan_thai_en),
            Language("zh", R.string.lan_chinese_na, R.string.lan_chinese_en),
            Language("ja", R.string.lan_japanese_na, R.string.lan_japanese_en),
            Language("ko", R.string.lan_korean_na, R.string.lan_korean_en),
            Language("it", R.string.lan_italian_na, R.string.lan_italian_en),
            Language("nl", R.string.lan_dutch_na, R.string.lan_dutch_en),
            Language("tr", R.string.lan_turkish_na, R.string.lan_turkish_en)
        )
    }

    data class LanguageListResult(
        val languages: List<Language>,
    )

    private fun createDeviceDefaultLanguage(deviceLang: String): Language {
        return Language(
            code = deviceLang, nativeName = -1, englishName = R.string.lan_defaults
        )
    }

    fun getLanguages(deviceLanguage: String): LanguageListResult {
        val deviceLangLower = deviceLanguage.lowercase()

        val isIn70List = localeTranslateDefaults.contains(deviceLangLower)
        val deviceSupported = supportedLanguages.find { it.code == deviceLangLower }
        val english = supportedLanguages.find { it.code == "en" }

        return when {

            // ⭐ RULE 1 — Device ∈ 70 list && not supported → show Device Default
            isIn70List && deviceSupported == null -> {
                val deviceDefault = createDeviceDefaultLanguage(deviceLangLower)
                LanguageListResult(
                    listOf(deviceDefault) + supportedLanguages
                )
            }

            // ⭐ RULE 2 — Device ∈ supportedLanguages → device first, english second
            deviceSupported != null -> {
                val others = supportedLanguages.filter {
                    it.code != deviceLangLower && it.code != "en"
                }

                val finalList = buildList {
                    add(deviceSupported)   // thêm device language
                    if (english != null && deviceSupported.code != "en") {
                        add(english)       // chỉ thêm English nếu device không phải EN
                    }
                    addAll(others)
                }


                LanguageListResult(finalList)
            }

            // ⭐ RULE 3 — Not in 70 && not supported → English first (no Device Default)
            else -> {
                val others = supportedLanguages.filter { it.code != "en" }

                val finalList = if (english != null) listOf(english) + others
                else supportedLanguages

                LanguageListResult(finalList)
            }
        }
    }

    fun getLanguageByCode(code: String): Language {
        return supportedLanguages.find { it.code == code } ?: Language(
            code,
            -1,
            R.string.lan_defaults
        )
    }
}
