package com.lav.bluetoothscanning.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.lav.bluetoothscanning.R
import com.lav.bluetoothscanning.activity.OnboardingActivity
import com.lav.bluetoothscanning.viewmodel.OnboardingViewModel
import kotlinx.android.synthetic.main.fragment_register_phone_number.*
import java.util.concurrent.TimeUnit

class RegisterPhoneNumberFragment : Fragment(), View.OnClickListener {

    private var viewModel: OnboardingViewModel? = null

    private var activity: OnboardingActivity? = null

    private lateinit var mAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnboardingActivity)
            activity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewModel()
        return inflater.inflate(R.layout.fragment_register_phone_number, container, false)
    }

    private fun initViewModel() {
        viewModel =
            activity?.let { ViewModelProviders.of(it).get(OnboardingViewModel::class.java) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        setClickListeners()
    }

    private fun setClickListeners() {
        id_button_proceed.setOnClickListener(this)
        id_button_resend.setOnClickListener(this)
        id_button_verify.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)
        if (viewModel?.verificationInProgress == true && validatePhoneNumber()) {
            startPhoneNumberVerification(id_editText_mobile.text.toString())
        }
    }

    override fun onClick(v: View?) {
        when (view?.id) {
            R.id.id_button_proceed -> {
                if (!validatePhoneNumber()) {
                    return
                }
                startPhoneNumberVerification(id_editText_mobile.text.toString())
            }
            R.id.id_button_resend -> {
                resendVerificationCode(id_editText_mobile.text.toString(), viewModel?.resendToken)
            }
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = id_editText_mobile.text.toString()
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length < 10) {
            id_editText_mobile.error = "Invalid phone number."
            id_editText_mobile.requestFocus()
            return false
        }
        return true
    }

    //the method is sending verification code
    private fun startPhoneNumberVerification(mobile: String) {
        if (activity != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91$mobile",
                60,
                TimeUnit.SECONDS,
                activity!!,
                mCallbacks
            )
            viewModel?.verificationInProgress = true
        }
    }

    //the callback to detect the verification status
    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            Log.i("OnboardingViewModel", "onVerificationCompleted:$phoneAuthCredential")
            viewModel?.verificationInProgress = false
            //Getting the code sent by SMS
            val code = phoneAuthCredential.smsCode

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, phoneAuthCredential)
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            Log.w("OnboardingViewModel", "onVerificationFailed", exception)
            viewModel?.verificationInProgress = false
            if (exception is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                id_editText_mobile.error = "Invalid phone number."
            } else if (exception is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Snackbar.make(
                    parent, "Quota exceeded.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            // Show a message and update the UI
            updateUI(STATE_VERIFY_FAILED)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            Log.d("OnboardingViewModel", "onCodeSent:$verificationId")
            // Save verification ID and resending token so we can use them later
            viewModel?.mVerificationId = verificationId
            viewModel?.resendToken = token

            // Update UI
            updateUI(STATE_CODE_SENT)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                activity!!,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        //verification successful we will create user table on Firestore
                        //viewModel?.setLoginSuccessData(true)
                        Log.d("RegisterPhone", "signInWithCredential:success")
                        val user = task.result?.user
                        // [START_EXCLUDE]
                        updateUI(STATE_SIGNIN_SUCCESS, user)
                        viewModel?.addDetailInUserTable(user)
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w("RegisterPhone", "signInWithCredential:failure", task.exception)
                        var message =
                            "Somthing is wrong, we will fix it soon..."
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered..."
                        }
                        val snackbar: Snackbar = Snackbar.make(
                            parent,
                            message,
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.show()
                        updateUI(STATE_SIGNIN_FAILED)
                    }
                })
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        if (activity != null)
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                activity!!, // Activity (for callback binding)
                mCallbacks, // OnVerificationStateChangedCallbacks
                token
            ) // ForceResendingToken from callbacks
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = mAuth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
                enableViews(id_editText_mobile, id_button_proceed)
                disableViews(id_editTextCode, id_button_resend, id_button_verify)
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                enableViews(id_button_verify, id_button_resend, id_editText_mobile, id_editTextCode)
                disableViews(id_button_proceed)
                showToast(getString(R.string.status_code_sent))
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                enableViews(
                    id_button_proceed, id_button_verify, id_button_resend, id_editText_mobile,
                    id_editTextCode
                )
                showToast(getString(R.string.status_verification_failed))
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                disableViews(
                    id_button_proceed, id_button_verify, id_button_resend, id_editText_mobile,
                    id_editTextCode
                )
                showToast(getString(R.string.status_verification_succeeded))

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        id_editTextCode.setText(cred.smsCode)
                    } else {
                        id_editTextCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                // No-op, handled by sign-in check
                showToast(getString(R.string.status_sign_in_failed))
            STATE_SIGNIN_SUCCESS -> {
            }
        }
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    private fun showToast(msg: CharSequence) {
        activity?.let {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}