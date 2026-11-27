package com.wavez.base.ui.premium.dialogs

import android.content.Context
import com.wavez.base.base.BaseDialogFragment
import com.wavez.base.databinding.DialogPurchaseSuccessBinding

class PurchaseSuccessDialog : BaseDialogFragment<DialogPurchaseSuccessBinding>() {

    override fun bindingView(): DialogPurchaseSuccessBinding {
        return DialogPurchaseSuccessBinding.inflate(layoutInflater)
    }

    companion object {

        fun newInstance(): PurchaseSuccessDialog {
            return PurchaseSuccessDialog()
        }
    }

    private var listener: PurchaseSuccessListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PurchaseSuccessListener) {
            listener = context
        }
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
    }

    override fun initListener() {
        super.initListener()
        binding.btnGotIt.setOnClickListener {
            dismiss()
            listener?.onPurchaseSuccessGotIt()
        }
    }

    interface PurchaseSuccessListener {
        fun onPurchaseSuccessGotIt()
    }

}