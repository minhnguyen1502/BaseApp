package com.wavez.base.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.wavez.base.BuildConfig
import com.wavez.base.R
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivityFeedbackBinding
import com.wavez.base.extension.gone
import com.wavez.base.utils.Constant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {

    override fun bindingView(): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }

    companion object {
        const val FROM_RATE = "from_rate"
        fun newIntent(context: Context, fromRate: Boolean = false) =
            Intent(context, FeedbackActivity::class.java).apply {
                putExtra(FROM_RATE, fromRate)
            }
    }

    private var fromRate: Boolean = false

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        fromRate = intent.getBooleanExtra(FROM_RATE, false)
        binding.toolbar.btnSave.gone()
        binding.toolbar.tvTitle.text = getString(R.string.feedback)
        binding.edtFeedback.addTextChangedListener {
            updateSendButtonState()
        }

        binding.edtFeedback.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }

    }


    override fun initListener() {
        super.initListener()

        binding.toolbar.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSendFeedback.setOnClickListener {
            val feedback = binding.edtFeedback.text.toString().trim()

            if (feedback.length < 6) {
                showToast(R.string.at_least_6_characters, Gravity.TOP)
                return@setOnClickListener
            }

            val appVersion = "App Version: ${BuildConfig.VERSION_NAME}"
            val phoneModel = "Phone Model: ${Build.MODEL}"
            val osVersion = "OS Version: ${Build.VERSION.RELEASE}"
            val sdkVersion = "SDK Version: ${Build.VERSION.SDK_INT}"

            val userFeedback = binding.edtFeedback.text.toString().trim()

            val emailContent = buildString {

                if (userFeedback.isNotEmpty()) {
                    append("User Feedback:\n$userFeedback\n\n")
                }

                append("Device Info:\n")
                append("$appVersion\n")
                append("$phoneModel\n")
                append("$osVersion\n")
                append("$sdkVersion\n")
            }

            sendToEmail(emailContent)
        }
    }

    private fun updateSendButtonState() {
        val feedbackText = binding.edtFeedback.text.toString().trim()
        val hasValidFeedback = feedbackText.length >= 6

        binding.btnSendFeedback.apply {
            isSelected = hasValidFeedback
        }
    }

    private fun sendToEmail(content: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(Constant.EMAIL_FEEDBACK))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback))
            putExtra(Intent.EXTRA_TEXT, content)
        }
        emailIntent.setPackage("com.google.android.gm")
        try {
            if (fromRate) {
                sharedPref.hasUserRated = true
            }
            startActivity(Intent.createChooser(emailIntent, getString(R.string.app_name)))
            finish()
        } catch (e: Exception) {
            Log.e("awwawaw", "Exception: $e")
            showToast(R.string.no_email_app_found, Gravity.TOP)
        }
    }
}