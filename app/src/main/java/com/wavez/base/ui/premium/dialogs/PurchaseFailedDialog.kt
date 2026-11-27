package com.wavez.base.ui.premium.dialogs

import android.content.Context
import com.wavez.base.base.BaseDialogFragment
import com.wavez.base.databinding.DialogPurchaseFailedBinding

class PurchaseFailedDialog : BaseDialogFragment<DialogPurchaseFailedBinding>() {

    override fun bindingView(): DialogPurchaseFailedBinding {
        return DialogPurchaseFailedBinding.inflate(layoutInflater)
    }

    companion object {

        fun newInstance(): PurchaseFailedDialog {
            return PurchaseFailedDialog()
        }
    }

    private var listener: PurchaseFailedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PurchaseFailedListener) {
            listener = context
        }
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }

    override fun initListener() {
        super.initListener()
        binding.btnTryAgain.setOnClickListener {
            dismiss()
            listener?.onTryAgain()
        }
    }

    interface PurchaseFailedListener {
        fun onTryAgain()
    }

}