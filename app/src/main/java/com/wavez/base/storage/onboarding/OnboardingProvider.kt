package com.wavez.base.storage.onboarding

import android.os.Bundle
import com.wavez.base.App
import com.wavez.base.BuildConfig
import com.wavez.base.R
import com.google.firebase.analytics.FirebaseAnalytics

object OnboardingProvider {

    val ONBOARD_02 = BoardingData(
        index = 2,
        imageID = R.drawable.img_ob_2,
        labelID = R.string.title_onb_1,
        desID = R.string.content_onb_1,
    )

    val ONBOARD_03 = BoardingData(
        index = 3,
        imageID = R.drawable.img_ob_3,
        labelID = R.string.title_onb_2,
        desID = R.string.content_onb_2,
    )

    private val habits = listOf<Habits>(
        Habits(1, R.string.habit_1, R.drawable.img_habit_1, "Live TV"),
        Habits(2, R.string.habit_2, R.drawable.img_habit_2, "Movies"),
        Habits(3, R.string.habit_3, R.drawable.img_habit_3, "Sport"),
        Habits(4, R.string.habit_4, R.drawable.img_habit_4, "News"),
        Habits(4, R.string.habit_5, R.drawable.img_habit_5, "Family"),
        Habits(4, R.string.habit_6, R.drawable.img_habit_6, "Entertainment"),
    )

    fun getHabits(): List<Habits> = habits

    fun pushEvent(data: List<Habits>) {
        if (data.isNotEmpty()) {
            data.forEach {
                logEventHabit(it.event)
            }
        }
    }

    private fun logEventHabit(name: String) {
        if (BuildConfig.DEBUG) {
            return
        }
        val bundle = Bundle().apply {
            putString("hobby_name", name)
        }
        FirebaseAnalytics.getInstance(App.appContext()).logEvent("onb1_hobby", bundle)
    }
}
