package com.wavez.base.base

sealed class ResponseState<T> {

    class Loading<T> : ResponseState<T>()

    class Success<T>(val data: T) : ResponseState<T>()

    class Error<T>(val message: String? = null) : ResponseState<T>()

}