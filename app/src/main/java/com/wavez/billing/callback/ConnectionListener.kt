package com.wavez.billing.callback

interface ConnectionListener {

    fun onConnectionSuccess()

    fun onDisconnected()

    fun onConnectionFailure()

}