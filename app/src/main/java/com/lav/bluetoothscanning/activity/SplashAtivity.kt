package com.lav.bluetoothscanning.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashAtivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldStartSignIn()) {
            redirectToLoginScreen(false)
        } else {
            redirectToMainScreen()
        }
    }

    private fun redirectToLoginScreen(onlyConsentPending: Boolean) {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.putExtra("consentRequired", true)
        startActivity(intent)
        finish()
    }

    private fun redirectToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun shouldStartSignIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser == null
    }
}