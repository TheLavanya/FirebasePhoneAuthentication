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
import com.lav.bluetoothscanning.model.VerificationStatus
import java.lang.Exception
import java.util.concurrent.TimeUnit

class OnboardingViewModel : ViewModel() {

    var verificationInProgress = false
    lateinit var mVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    var mobile: String? = null

    private val consentAgreedLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    /* private val redirectToVerifyLiveData: MutableLiveData<Boolean> by lazy {
         MutableLiveData<Boolean>()
     }*/

    private val loginSuccessLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    /*private val phoneVerificationCodeLiveData: MutableLiveData<Pair<Boolean, String>> by lazy {
        MutableLiveData<Pair<Boolean, String>>()
    }*/

    private val verificationStatusLiveData: MutableLiveData<VerificationStatus> by lazy {
        MutableLiveData<VerificationStatus>()
    }

    fun isConsentAgreedData(): LiveData<Boolean> = consentAgreedLiveData

    //fun getRedirectToVerifyData(): LiveData<Boolean> = redirectToVerifyLiveData
    fun getLoginSuccessData(): LiveData<Boolean> = loginSuccessLiveData
    fun getPhoneVerificationStatusData(): LiveData<VerificationStatus> =
        verificationStatusLiveData

    fun getVerificationId(): String {
        return mVerificationId
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("OnboardingViewModel", "OnboardingViewModel destroyed!")
    }

    /*fun redirectToVerify() {
        redirectToVerifyLiveData.postValue(true)
    }*/

    fun setConsentAgreement() {
        consentAgreedLiveData.postValue(true)
    }


    fun addDetailInUserTable(currentUser: FirebaseUser?) {
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

    private fun getVerificationStatusModel(
        status: Boolean,
        code: String?, error: Exception?
    ): VerificationStatus {
        return VerificationStatus(status, code, error)
    }
}