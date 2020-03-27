package com.lav.bluetoothscanning.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashViewModel : ViewModel() {

    fun queryUserTable() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val fireStore = Firebase.firestore
        fireStore.collection("users")
            .get()
    }
}