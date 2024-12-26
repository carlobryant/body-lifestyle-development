package com.example.bold_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import com.example.bold_app.model.GoalModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivityViewModel : ViewModel() {
    private val _selectedFragmentTag = MutableLiveData<String>("FitnessFragment")
    val selectedFragmentTag: LiveData<String> get() = _selectedFragmentTag

    private val _fitnessView = MutableLiveData<Boolean>(true)
    private val _goalView = MutableLiveData<Boolean>(true)
    val fitnessView: LiveData<Boolean> get() = _fitnessView
    val goalView: LiveData<Boolean> get() = _goalView

    private val _latestGoalDataId = MutableLiveData<String?>()
    val latestGoalDataId: LiveData<String?> get() = _latestGoalDataId

    private val _latestBMI = MutableLiveData<Double?>()
    val latestBMI: LiveData<Double?> get() = _latestBMI

    private val _checklistUpdate = MutableLiveData<Pair<Int, Boolean>>()
    val checklistUpdate: LiveData<Pair<Int, Boolean>> get() = _checklistUpdate

    private val firebaseRef: DatabaseReference

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            firebaseRef = FirebaseDatabase.getInstance().getReference("users/$uid/goal")
            fetchLatestGoalDataId()
        } else {
            throw IllegalStateException("User is not logged in")
        }
    }

    fun setSelectedFragmentTag(tag: String) {
        _selectedFragmentTag.value = tag
    }

    fun setFitnessView(isFitnessView: Boolean) {
        _fitnessView.value = isFitnessView
    }

    fun setGoalView(isGoalView: Boolean) {
        _goalView.value = isGoalView
    }

    fun updateChecklistItem(itemId: Int, isDone: Boolean) {
        _checklistUpdate.value = Pair(itemId, isDone)
    }

    fun updateLatestBMI(bmi: Double) {
            _latestBMI.value = bmi
    }

    private fun fetchLatestGoalDataId() {
        firebaseRef.orderByKey().limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        _latestGoalDataId.value = child.key
                    }
                    _goalView.value = false
                } else {
                    _latestGoalDataId.value = null
                    _goalView.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivityViewModel", "Failed to read latest goal data: $error")
            }
        })
    }
}
