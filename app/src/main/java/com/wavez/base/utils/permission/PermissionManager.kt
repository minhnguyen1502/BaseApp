package com.wavez.base.utils.permission

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.wavez.base.base.BaseActivity

interface PermissionManager {

    fun requestPermissions(
        activity: BaseActivity<*>, requestCode: Int, vararg permissions: String
    )

    fun hasPermissionArrayList(permissions: Array<String>): Boolean

    fun hasPermission(permission: String): Boolean

    fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>)

    fun hasNotificationPermission(): Boolean

    fun hasManageAllFilePermissions(): Boolean

    fun requestCameraPermissions(permissionLauncher: ActivityResultLauncher<String>)

    fun hasCameraPermission(): Boolean

    fun requestReadMediaPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>)

    fun requestFilesAccessPermissionIfNeed(activity: Activity)

    fun requestCalendarPermission(permissionLauncher: ActivityResultLauncher<String>)

    fun hasCalendarPermission(): Boolean

    fun hasReadAudioPermission(): Boolean

    fun hasReadMediaPermission(): Boolean

    fun requestReadAudioPermission(permissionLauncher: ActivityResultLauncher<String>)

    fun hasRecordAudioPermission(): Boolean

    fun requestRecordAudioPermission(permissionLauncher: ActivityResultLauncher<String>)

    fun requestBatteryOptimizationSettings(
        activity: BaseActivity<*>, resultLauncher: ActivityResultLauncher<Intent>
    )

    fun isIgnoringBatteryOptimizations(): Boolean

    fun isNetworkAvailable(): Boolean

    fun isGPSAvailable(): Boolean

    fun requestLocationPermission(permissionLauncher: ActivityResultLauncher<Array<String>>)

    fun hasLocationPermission(): Boolean

    fun canShowNotificationPermissionDialog(activity: Activity, hasAskedBefore: Boolean): Boolean
    fun shouldShowNotificationRationale(activity: Activity): Boolean

}