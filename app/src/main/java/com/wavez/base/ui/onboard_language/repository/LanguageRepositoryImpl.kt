package com.wavez.base.ui.onboard_language.repository

import android.content.res.Resources
import com.wavez.base.storage.SharedPref
import com.wavez.base.ui.onboard_language.model.Language
import com.wavez.base.ui.onboard_language.provider.LanguageProvider

class LanguageRepositoryImpl(
    private val sharedPref: SharedPref, private val providerLanguage: LanguageProvider
) : LanguageRepository {

    override fun getLanguages(): List<Language> {
        val primaryLocale = Resources.getSystem().configuration.locales[0].language
        return providerLanguage.getLanguages(primaryLocale).languages
    }

    override fun saveLanguage(language: Language) {
        sharedPref.configLanguage = language.code
    }

    override fun getLanguageConfig(): Language {
        return providerLanguage.getLanguageByCode(sharedPref.configLanguage)
    }

    override fun getConfigLanguageCode(): String {
        return sharedPref.configLanguage
    }

}