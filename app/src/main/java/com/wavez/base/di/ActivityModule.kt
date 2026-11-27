package com.wavez.base.di

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.wavez.admob.native_ads.NativeHelper
import com.wavez.base.storage.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {

    @Provides
    fun provideNativeHelper(
        activity: Activity, sharePref: SharedPref
    ): NativeHelper {
        return NativeHelper(
            activity as AppCompatActivity, sharePref
        )
    }

}