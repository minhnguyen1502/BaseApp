package com.wavez.base.ui.home.dialog

import android.content.DialogInterface
import android.view.ViewGroup
import com.wavez.base.base.BaseDialogFragment
import com.wavez.base.databinding.DialogLoadingScrinBinding

class DialogLoading : BaseDialogFragment<DialogLoadingScrinBinding>() {
    var onDismissed: (() -> Unit)? = null
    override val fullScreen = true
    override fun bindingView(): DialogLoadingScrinBinding {
        return DialogLoadingScrinBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent() = DialogLoading()
    }

    override fun initListener() {
        super.initListener()
        binding.root.setOnClickListener {
            val cb = onDismissed
            onDismissed = null
            cb?.invoke()
            dismissAllowingStateLoss()
        }
        binding.linearLayout6.setOnClickListener { }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissed?.invoke()
    }
}