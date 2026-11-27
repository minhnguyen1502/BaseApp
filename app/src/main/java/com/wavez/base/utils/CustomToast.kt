package com.wavez.base.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wavez.base.R
import com.wavez.base.extension.gone

class CustomToast(context: Context) : Toast(context) {

    companion object {

        @SuppressLint("InflateParams")
        fun makeText(
            context: Context,
            @SuppressLint("SupportAnnotationUsage") @StringRes contentId: Int,
            @DrawableRes bgDrawable: Int,
            @DrawableRes iconId: Int? = null,
            gravity: Int = Gravity.TOP,
            duration: Int = LENGTH_SHORT,
        ): Toast {
            val toast = Toast(context)
            toast.duration = duration

            val layout: View =
                LayoutInflater.from(context).inflate(R.layout.layout_toast_lock, null, false)

            val bgRoot = layout.findViewById<View>(R.id.bgRoot) as ConstraintLayout
            val icon = layout.findViewById<View>(R.id.imgIcon) as AppCompatImageView
            val text = layout.findViewById<View>(R.id.tvContent) as TextView

            bgRoot.setBackgroundResource(bgDrawable)
            if (iconId != null) {
                icon.setImageResource(iconId)
            } else {
                icon.gone()
            }
            text.text = context.getString(contentId)

            try {
                toast.view = layout
                toast.setGravity(gravity, 0, 0)
            } catch (_: Exception) {
                toast.setText(context.getString(contentId))
            }
            toast.duration = duration
            return toast
        }
    }
}
