package com.wavez.base.utils.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetWorkStateHolder {

    private val _isConnected by lazy { MutableLiveData<Boolean>() }
    val isConnected: LiveData<Boolean>
        get() = _isConnected

    fun updateNetworkState(state: Boolean) = _isConnected.postValue(state)
}
