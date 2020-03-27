package com.lav.bluetoothscanning.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.lav.bluetoothscanning.R
import com.lav.bluetoothscanning.activity.OnboardingActivity
import com.lav.bluetoothscanning.viewmodel.OnboardingViewModel
import kotlinx.android.synthetic.main.fragment_verify_phone_number.*

class VerifyPhoneNumberFragment : Fragment(), View.OnClickListener {

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

        return inflater.inflate(R.layout.fragment_verify_phone_number, container, false)
    }

    private fun initViewModel() {
        viewModel =
            activity?.let { ViewModelProviders.of(it).get(OnboardingViewModel::class.java) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        initObserver()

        viewModel?.mobile?.let {
            viewModel?.sendVerificationCode(it)
        }
        id_button_sign_in.setOnClickListener(this)
    }

    private fun initObserver() {
        viewModel?.getPhoneVerificationCodeData()?.observe(this, getVerificationCodeObserver())
    }

    private fun getVerificationCodeObserver(): Observer<Pair<Boolean, String>> {
        return Observer {
            if (it.first) {
                val code = it.second
                verifyCode(code)
            }
        }
    }

    private fun verifyCode(code: String) {
        editTextCode.setText(code);
        //verifying the code
        verifyVerificationCode(code);
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.id_button_sign_in -> {
                val code = editTextCode.text.toString().trim()
                if (code.isEmpty() || code.length < 6) {
                    editTextCode.error = getString(R.string.enter_valid_code_error)
                    editTextCode.requestFocus()
                    return
                }
                //verifying the code entered manually
                verifyVerificationCode(code)
            }
        }
    }

    private fun verifyVerificationCode(otp: String) {
        //creating the credential
        viewModel?.getVerificationId()?.let {
            val credential = PhoneAuthProvider.getCredential(it, otp)
            //signing the user
            signInWithPhoneAuthCredential(credential)
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
                        viewModel?.addDetailInUserTable()
                    } else {
                        //verification unsuccessful.. display an error message
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
                    }
                })
    }
}