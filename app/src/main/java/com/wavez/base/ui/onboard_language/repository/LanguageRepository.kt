package com.wavez.base.ui.onboard_language.repository

import com.wavez.base.ui.onboard_language.model.Language


interface LanguageRepository {

    fun getLanguages(): List<Language>

    fun saveLanguage(language: Language)

    fun getLanguageConfig(): Language

    fun getConfigLanguageCode(): String

}