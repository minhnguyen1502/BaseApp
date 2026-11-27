package com.wavez.base.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.wavez.base.utils.permission.PermissionManager
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

abstract class BaseDialogFragment<T : ViewBinding> : DialogFragment() {
    open val binding by lazy { bindingView() }

    open val registerEventBus = false

    open val fullScreen = false

    open val isTransparentBackground: Boolean = true

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (registerEventBus) EventBus.getDefault().register(this)
        initConfig(view, savedInstanceState)
        initObserver()
        initListener()
        initTask()
    }

    override fun onStart() {
        super.onStart()

        /*dialog?.window?.setDimAmount(0.7f)*/

        if (isTransparentBackground) {
            dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }

        if (fullScreen) {
            dialog?.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
        } else {
            dialog?.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        if (registerEventBus) EventBus.getDefault().unregister(this)
        release()
        super.onDestroyView()
    }

    open fun initObserver() {}

    open fun initConfig(view: View, savedInstanceState: Bundle?) {}

    open fun initListener() {}

    open fun initTask() {}

    open fun release() {}

    abstract fun bindingView(): T

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            manager.findFragmentByTag(tag).let { fragment ->
                fragment ?: let {
                    super.show(manager, tag)
                }
            }
        } catch (_: Exception) {
        }
    }
}