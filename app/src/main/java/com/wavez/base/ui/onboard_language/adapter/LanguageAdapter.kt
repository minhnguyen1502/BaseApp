package com.wavez.base.ui.onboard_language.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wavez.base.R
import com.wavez.base.base.BaseRecyclerViewAdapter
import com.wavez.base.databinding.ItemLanguageBinding
import com.wavez.base.ui.onboard_language.model.Language

class LanguageAdapter : BaseRecyclerViewAdapter<Language>() {

    companion object {

        private const val TYPE_ITEM = 1

    }

    var selectPosition = -1

    var selectedLanguageCode: String? = null

    var onSelected: ((Language) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun onLanguageSelected(code: String) {
        selectedLanguageCode = code
        notifyDataSetChanged()
    }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: Language, position: Int) {
        if (holder is LanguageViewHolder) {
            holder.bind(item, position)
        } else {
            throw IllegalArgumentException("Invalid view holder type")
        }
    }

    override fun getItemLayout(viewType: Int): Int {
        if (viewType == TYPE_ITEM) {
            return R.layout.item_language
        } else {
            throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun createViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            val binding = ItemLanguageBinding.bind(view)
            return LanguageViewHolder(binding)
        } else {
            throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemType(item: Language): Int {
        return TYPE_ITEM
    }

    inner class LanguageViewHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Language, position: Int) {
            val context = binding.root.context
            binding.tvLanguageName.text = context.getString(data.englishName)
            if (data.nativeName == -1) {
                binding.tvLanguage.visibility = View.INVISIBLE
            } else {
                binding.tvLanguage.visibility = View.VISIBLE
                binding.tvLanguage.text = context.getString(data.nativeName)
            }
            binding.root.isSelected = data.code == selectedLanguageCode
            if (selectPosition == position) {
                binding.tvLanguageName.isSelected = true
                binding.tvLanguageName.setTypeface(binding.tvLanguageName.typeface, Typeface.BOLD)
                binding.root.isSelected = true
            }
            binding.root.setOnClickListener {
                if (selectPosition != position) {
                    notifyItemChanged(selectPosition)
                    selectPosition = position
                    notifyItemChanged(position)
                }
                onSelected?.invoke(data)
            }
        }
    }

}