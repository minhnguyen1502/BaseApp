package com.wavez.base.di

import com.wavez.base.ui.onboard_language.repository.LanguageRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppModuleEntry {

    fun getLanguageRepository(): LanguageRepository

}