package com.wavez.base.utils

object AppState {
    var isAppInForeground = false

    /**
     * Set right before returning to Main from a successful playlist add or a watched channel.
     * Consumed once by the very next [com.wavez.base.ui.home.HomeActivity.onResume] —
     * in-memory only, so it can't linger stale across process restarts or fire off an
     * unrelated Home resume (e.g. an ad activity closing, screen lock/unlock).
     */
    var pendingNotificationPermissionPrompt = false

    /**
     * Set once the OS notification-permission dialog has been asked (allow or deny) during the
     * app's very first-ever launch. In-memory only, so it resets on the next process start —
     * this deliberately limits the "don't ask again" behavior to the first session, not forever.
     */
    var hasHandledFirstLaunchNotificationPrompt = false
}