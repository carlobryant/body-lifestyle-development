package com.example.bold_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogInViewModel : ViewModel() {
    private var _isSignedIn = MutableLiveData<Boolean>()
    val isSignedIn: LiveData<Boolean> get() = _isSignedIn

    fun updateSignInStatus(signedIn: Boolean) {
        _isSignedIn.value = signedIn
    }
}