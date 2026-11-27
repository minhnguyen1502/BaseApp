package com.wavez.base.ui.onboarding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.wavez.base.base.BaseRecyclerViewAdapterVer2
import com.wavez.base.databinding.ItemHabitsBinding
import com.wavez.base.extension.startMarquee
import com.wavez.base.storage.onboarding.Habits

class HabitsAdapter : BaseRecyclerViewAdapterVer2<Habits, ItemHabitsBinding>() {

    private var habitsSelects = mutableListOf<Habits>()

    var onItemSelected: (() -> Unit)? = null

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemHabitsBinding = ItemHabitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindData(
        binding: ItemHabitsBinding,
        data: Habits,
        position: Int,
        context: Context,
    ) {
        Glide.with(context).load(data.image).into(binding.imageHabit)
        binding.tvHabitsName.text = context.getString(data.name)
        binding.tvHabitsName.startMarquee()
        binding.root.setOnClickListener {
            if (habitsSelects.contains(data)) {
                habitsSelects.remove(data)
                binding.root.isSelected = false
            } else {
                habitsSelects.add(data)
                binding.root.isSelected = true
            }
            onItemSelected?.invoke()
        }
    }

    fun getSelectedItems(): List<Habits> = habitsSelects
}
