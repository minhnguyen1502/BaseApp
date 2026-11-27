package com.wavez.base.ui.home.dialog

import android.content.Context
import android.view.ViewGroup
import com.wavez.base.base.BaseDialogFragment
import com.wavez.base.databinding.DialogInternetBinding

class ReconnectNetworkDialog : BaseDialogFragment<DialogInternetBinding>() {
    override fun bindingView(): DialogInternetBinding = DialogInternetBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "ReconnectNetworkDialog"
        fun newInstance() = ReconnectNetworkDialog()
    }

    override val fullScreen: Boolean
        get() = true

    private var listener: ReconnectNetworkListener? = null

    override fun onStart() {
        super.onStart()
        isCancelable = false

        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener =
            context as? ReconnectNetworkListener ?: parentFragment as? ReconnectNetworkListener
    }

    override fun initListener() {
        super.initListener()

        binding.btnTryAgain.setOnClickListener {
            listener?.onReconnectNetwork()
        }
    }
    interface ReconnectNetworkListener {
        fun onReconnectNetwork()
    }
}


