package com.lav.bluetoothscanning.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lav.bluetoothscanning.activity.TAG
import java.util.concurrent.TimeUnit

class OnboardingViewModel : ViewModel() {

    private lateinit var mVerificationId: String

    var mobile: String? = null
    private val consentAgreedLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    private val redirectToVerifyLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val loginSuccessLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val phoneVerificationCodeLiveData: MutableLiveData<Pair<Boolean, String>> by lazy {
        MutableLiveData<Pair<Boolean, String>>()
    }

    fun isConsentAgreedData(): LiveData<Boolean> = consentAgreedLiveData
    fun getRedirectToVerifyData(): LiveData<Boolean> = redirectToVerifyLiveData
    fun getLoginSuccessData(): LiveData<Boolean> = loginSuccessLiveData
    fun getPhoneVerificationCodeData(): LiveData<Pair<Boolean, String>> =
        phoneVerificationCodeLiveData

    fun getVerificationId(): String {
        return mVerificationId
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("OnboardingViewModel", "OnboardingViewModel destroyed!")
    }

    fun redirectToVerify() {
        redirectToVerifyLiveData.postValue(true)
    }

    fun setConsentAgreement() {
        consentAgreedLiveData.postValue(true)
    }

    //the method is sending verification code
    fun sendVerificationCode(mobile: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91$mobile",
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallbacks
        )
    }

    //the callback to detect the verification status
    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            //Getting the code sent by SMS
            val code = phoneAuthCredential.smsCode

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                phoneVerificationCodeLiveData.postValue(Pair(true, code))
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            val msg = p0.localizedMessage
            // Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            Log.i(TAG, msg)
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            mVerificationId = p0
        }
    }

    fun addDetailInUserTable() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val user = hashMapOf(
                "mobile" to currentUser.phoneNumber,
                "userId" to currentUser.uid
            )
            val fireStore = Firebase.firestore
            fireStore.collection("users")
                .document(currentUser.uid)
                .set(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "OnboardingViewModel",
                        "DocumentSnapshot added with ID: ${documentReference}"
                    )
                    loginSuccessLiveData.postValue(true)
                }
                .addOnFailureListener { e ->
                    Log.w("OnboardingViewModel", "Error adding document", e)

                    //TODO Logout from firebase auth also

                    loginSuccessLiveData.postValue(false)
                }
        }
    }
}