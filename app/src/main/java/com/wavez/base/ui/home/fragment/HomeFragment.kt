package com.wavez.base.ui.home.fragment

import com.wavez.base.base.BaseFragment
import com.wavez.base.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/** Màn Home mẫu — thay bằng nội dung chính của app mới. */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindingView(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }
}
