package com.lav.bluetoothscanning.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.lav.bluetoothscanning.R
import kotlinx.android.synthetic.main.activity_verify_phone_number.*
import java.util.concurrent.TimeUnit

class VerifyPhoneNumberActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mVerificationId: String
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone_number)
        mAuth = FirebaseAuth.getInstance()

        val mobile = getIntent().getStringExtra("mobile")
        sendVerificationCode(mobile)
    }

    //the method is sending verification code
    private fun sendVerificationCode(mobile: String) {
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
                editTextCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            val msg = p0.localizedMessage
            // Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            Log.i(TAG, msg)
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            mVerificationId = p0;
            enableButton()
        }
    }

    private fun enableButton() {
        buttonSignIn.setOnClickListener(this)
    }

    private fun verifyVerificationCode(otp: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(mVerificationId, otp)

        //signing the user
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        //verification successful we will start the profile activity
                        val intent =
                            Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {

                        //verification unsuccessful.. display an error message
                        var message =
                            "Somthing is wrong, we will fix it soon..."
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered..."
                        }
                        val snackbar: Snackbar = Snackbar.make(
                            findViewById(R.id.parent),
                            message,
                            Snackbar.LENGTH_LONG
                        )
                        /*snackbar.setAction("Dismiss", object : OnClickListener() {
                            fun onClick(v: View?) {}
                        })*/
                        snackbar.show()
                    }
                })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonSignIn -> {
                val code = editTextCode.text.toString().trim()
                if (code.isEmpty() || code.length < 6) {
                    editTextCode.error = "Enter valid code"
                    editTextCode.requestFocus()
                    return
                }

                //verifying the code entered manually
                verifyVerificationCode(code)
            }
        }
    }
}