package com.wavez.base.ui.home.dialog

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import com.wavez.base.R
import com.wavez.base.base.BaseDialogFragment
import com.wavez.base.databinding.DialogNotiPermissionVer2Binding

class NotiPerDialogVer2 : BaseDialogFragment<DialogNotiPermissionVer2Binding>() {
    override fun bindingView(): DialogNotiPermissionVer2Binding {
        return DialogNotiPermissionVer2Binding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "NotiPerDialogVer2"
        fun newInstance() = NotiPerDialogVer2()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.8f)
        }
    }

    override fun initListener() {
        super.initListener()
        binding.tvTitle.text = getString(R.string.noti_enable_title_ver_2, getString(R.string.app_name))

        binding.btnDeny.setOnClickListener {
            dismiss()
        }

        binding.btnAllow.setOnClickListener {
            dismiss()
            openNotificationSettings()
        }
    }

    private fun openNotificationSettings() {
        val ctx = context ?: return
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", ctx.packageName, null))
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", ctx.packageName, null))
                )
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
}
