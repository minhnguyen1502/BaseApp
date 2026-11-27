package com.wavez.base.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    private val saved = SavedStateHandle()

    val isLoading = MutableLiveData<Boolean>()

    val message = MutableLiveData<String>()

    val error = MutableLiveData<String>()

    fun showLoading() {
        isLoading.postValue(true)
    }

    fun dismissLoading() {
        isLoading.postValue(false)
    }

    fun showMessage(message: String) {
        this.message.value = message
    }

    fun showError(message: String) {
        this.error.value = message
    }

    fun <T> saveStateData(key: String, data: T) = saved.set(key, data)

    fun <T> getDataSave(key: String) = saved.getLiveData<T>(key)

}