package com.wavez.base.utils.permission


import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import com.wavez.base.base.BaseActivity


class PermissionManagerImpl(private val context: Context) : PermissionManager,
    DefaultLifecycleObserver {

    private fun requestPermission(
        requestStorageLauncher: ActivityResultLauncher<String>, permission: String
    ) {
        requestStorageLauncher.launch(permission)
    }

    private fun requestListPermission(
        launcher: ActivityResultLauncher<Array<String>>, permissions: Array<String>
    ) {
        launcher.launch(permissions)
    }

    override fun hasManageAllFilePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }


    override fun requestPermissions(
        activity: BaseActivity<*>, requestCode: Int, vararg permissions: String
    ) {
        val need = permissions.filterNot { hasPermission(it) }
        if (need.isEmpty()) return
        activity.requestPermissions(need.toTypedArray(), requestCode)
    }


    override fun hasPermissionArrayList(permissions: Array<String>): Boolean =
        permissions.all { hasPermission(it) }


    override fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>) {
        requestPermission(permissionLauncher, Manifest.permission.POST_NOTIFICATIONS)
    }


    override fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }


    override fun requestCameraPermissions(permissionLauncher: ActivityResultLauncher<String>) {
        val permission = Manifest.permission.CAMERA
        if (!hasPermission(permission)) requestPermission(permissionLauncher, permission)
    }

    override fun requestCalendarPermission(permissionLauncher: ActivityResultLauncher<String>) {
        val permission = Manifest.permission.READ_CALENDAR
        if (!hasPermission(permission)) requestPermission(permissionLauncher, permission)
    }

    override fun hasCameraPermission(): Boolean = hasPermission(Manifest.permission.CAMERA)

    override fun hasCalendarPermission(): Boolean = hasPermission(Manifest.permission.READ_CALENDAR)

    override fun hasReadMediaPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
        return hasPermissionArrayList(permission)
    }

    override fun hasReadAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            READ_EXTERNAL_STORAGE
        }
        return hasPermission(permission)
    }

    override fun requestReadMediaPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
        if (!hasPermissionArrayList(permission)) requestListPermission(
            permissionLauncher, permission
        )
    }


    override fun requestFilesAccessPermissionIfNeed(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = try {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
            } catch (_: Exception) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        }
    }

    override fun requestReadAudioPermission(permissionLauncher: ActivityResultLauncher<String>) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            READ_EXTERNAL_STORAGE
        }
        if (!hasPermission(permission)) requestPermission(permissionLauncher, permission)
    }

    override fun hasRecordAudioPermission(): Boolean =
        hasPermission(Manifest.permission.RECORD_AUDIO)

    override fun requestRecordAudioPermission(permissionLauncher: ActivityResultLauncher<String>) {
        val permission = Manifest.permission.RECORD_AUDIO
        if (!hasPermission(permission)) requestPermission(permissionLauncher, permission)
    }

    @SuppressLint("BatteryLife")
    override fun requestBatteryOptimizationSettings(
        activity: BaseActivity<*>, resultLauncher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${activity.packageName}")
        resultLauncher.launch(intent)
    }


    override fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

    override fun isGPSAvailable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    override fun hasLocationPermission(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override fun requestLocationPermission(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun canShowNotificationPermissionDialog(
        activity: Activity, hasAskedBefore: Boolean
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (!hasAskedBefore) return true
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun shouldShowNotificationRationale(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    }
}