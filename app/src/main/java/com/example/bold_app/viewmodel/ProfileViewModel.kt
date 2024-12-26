package com.example.bold_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    private val _username = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _profilePictureUri = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val email: LiveData<String> get() = _email
    val profilePictureUri: LiveData<String> get() = _profilePictureUri

    fun setProfileData(username: String, email: String, profilePictureUri: String) {
        _username.value = username
        _email.value = email
        _profilePictureUri.value = profilePictureUri
    }
}