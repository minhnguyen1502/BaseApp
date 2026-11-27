package com.wavez.base.ui.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import android.util.Log
import com.bumptech.glide.Glide
import com.wavez.base.R
import com.wavez.base.base.BaseActivity
import com.wavez.base.databinding.ActivityLanguageBinding
import com.wavez.base.extension.gone
import com.wavez.base.extension.visible
import com.wavez.base.ui.home.HomeActivity
import com.wavez.base.ui.onboard_language.adapter.LanguageAdapter
import com.wavez.base.ui.onboard_language.model.DataResult
import com.wavez.base.ui.onboard_language.viewmodel.LanguageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    override fun bindingView() = ActivityLanguageBinding.inflate(layoutInflater)

    companion object {
        fun newIntent(context: Context) = Intent(context, LanguageActivity::class.java)
    }

    private val viewModel by viewModels<LanguageViewModel>()

    lateinit var adapter: LanguageAdapter

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        initAdapter()
        binding.toolbar.btnSave.isSelected = true
        viewModel.getUiLanguages()
        binding.viewNative.gone()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.selectedUiLanguage.observe(this) {
            adapter.onLanguageSelected(it.code)
        }

        viewModel.uiLanguageState.observe(this) {
            binding.progressBar.gone()
            when (it) {
                is DataResult.Loading -> {
                    binding.progressBar.visible()
                }

                is DataResult.Success -> {
                    binding.progressBar.gone()
                    viewModel.setLanguageDefault(it.data)
                    adapter.setData(it.data)

                }

                else -> {}
            }
        }
    }

    override fun initListener() {
        super.initListener()
        adapter.onLanguageSelected(viewModel.getConfig())

        binding.toolbar.btnBack.setOnClickListener {
            finish()
        }
        binding.toolbar.btnSave.setOnClickListener {
            viewModel.saveConfig()
            startActivity(HomeActivity.newIntent(this, true))
            finishAffinity()
        }
    }

    private fun initAdapter() {
        viewModel.getUiLanguages()
        adapter = LanguageAdapter().apply {
            onSelected = {
                viewModel.onLanguageChanged(it)
            }
        }
        binding.rvLanguage.adapter = adapter
    }
}