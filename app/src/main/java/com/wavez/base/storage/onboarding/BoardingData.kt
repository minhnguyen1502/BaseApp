package com.wavez.base.storage.onboarding

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class BoardingData(
    val index: Int,
    val imageID: Int,
    val labelID: Int,
    val desID: Int,
) : Parcelable
