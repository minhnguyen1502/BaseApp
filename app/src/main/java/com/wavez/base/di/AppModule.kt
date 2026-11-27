package com.wavez.base.di

import android.content.Context
import com.wavez.billing.BillingLibrary
import com.wavez.billing.GooglePlayBillingLibrary
import com.wavez.base.storage.SharedPref
import com.wavez.base.storage.SharedPreferencesApp
import com.wavez.base.ui.onboard_language.provider.LanguageProvider
import com.wavez.base.ui.onboard_language.repository.LanguageRepository
import com.wavez.base.ui.onboard_language.repository.LanguageRepositoryImpl
import com.wavez.base.ui.premium.repository.PremiumRepository
import com.wavez.base.utils.network.NetworkHelper
import com.wavez.base.utils.permission.PermissionManager
import com.wavez.base.utils.permission.PermissionManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideSharedPref(
        @ApplicationContext context: Context,
    ): SharedPref = SharedPreferencesApp(context)

    @Provides
    fun provideLanguageRepository(
        sharedPref: SharedPref, providerLanguage: LanguageProvider
    ): LanguageRepository {
        return LanguageRepositoryImpl(sharedPref, providerLanguage)
    }

    @Singleton
    @Provides
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManagerImpl(context)
    }

    @Provides
    @Singleton
    fun providerBillingManager(
        @ApplicationContext context: Context,
    ): BillingLibrary {
        return GooglePlayBillingLibrary(context)
    }

    @Provides
    @Singleton
    fun providerPremiumRepository(
        billingLibrary: BillingLibrary, sharedPref: SharedPref
    ): PremiumRepository {
        return PremiumRepository(billingLibrary, sharedPref)
    }

    @Provides
    @Singleton
    fun provideNetWorkManager(@ApplicationContext context: Context): NetworkHelper =
        NetworkHelper(context)
}
