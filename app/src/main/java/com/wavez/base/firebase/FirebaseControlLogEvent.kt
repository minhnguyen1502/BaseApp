package com.wavez.base.firebase

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

object FirebaseControlLogEvent {
    fun logEvent(event: AppEvent, params: Bundle? = null) {
        Firebase.analytics.logEvent(event.value, params)
    }
}

enum class AppEvent(val value: String) {
    SPLASH_VIEW("splash_view"), LFO_VIEW("language_screen_view"), LFO_CLICK_LANGUAGE("click_language"), LFO_CLICK_NEXT(
        "click_btn_next"
    ),
    ONBOARDING_1_VIEW("onboarding_1_view"), ONBOARDING_1_CLICK_NEXT("onboarding_1_click_next"), ONBOARDING_2_VIEW(
        "onboarding_2_view"
    ),
    ONBOARDING_2_CLICK_NEXT("onboarding_2_click_next"), ONBOARDING_3_VIEW("onboarding_3_view"), ONBOARDING_3_CLICK_NEXT(
        "onboarding_3_click_next"
    ),
    WELCOME_VIEW("welcome_view"), WELCOME_CLICK("welcome_click"), HOME_VIEW("home_view"),
    SPLASH_VIEW_RE("splash_view_re"),
    HOME_VIEW_RE("home_view_re"),

    ADD_PLAYLIST_CLICK("add_playlist_click"),
    IMPORT_URL_CLICK("import_url_click"),
    UPLOAD_M3U_CLICK("upload_m3u_click"),
    LOAD_DEVICE_CLICK("load_device_click"),
    SINGLE_STREAM_CLICK("single_stream_click"),
    XSTREAM_CLICK("xstream_click"),
    SAVE_CLICK("save_click"),

}

