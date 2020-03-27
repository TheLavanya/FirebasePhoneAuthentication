package com.lav.bluetoothscanning.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lav.bluetoothscanning.R
import com.lav.bluetoothscanning.fragment.ConsentFragment
import com.lav.bluetoothscanning.fragment.RegisterPhoneNumberFragment
import com.lav.bluetoothscanning.viewmodel.OnboardingViewModel

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        initViewModel()
        initObserver()
        initRegistrationFlow()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(OnboardingViewModel::class.java)
    }

    private fun initRegistrationFlow() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_container, ConsentFragment(), null).commit()
    }

    private fun initObserver() {
        viewModel.isConsentAgreedData().observe(this, getConsentAgreedObserver())

        //viewModel.getRedirectToVerifyData().observe(this, getRedirectToVerifyNumberScreen())

        viewModel.getLoginSuccessData().observe(this, getLoginSuccessObserver())
    }

    private fun getConsentAgreedObserver(): Observer<Boolean> {
        return Observer {
            redirectToRegisterPhoneScreen()
        }
    }

    private fun redirectToRegisterPhoneScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_container, RegisterPhoneNumberFragment(), null)
            .commit()
    }

    private fun getLoginSuccessObserver(): Observer<Boolean> {
        return Observer {
            if (it) redirectToDashboard()
        }
    }

    private fun redirectToDashboard() {
        val intent =
            Intent(this, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    /*private fun getRedirectToVerifyNumberScreen(): Observer<Boolean> {
        return Observer {
            if (it) {
                redirectToVerifyNumberScreen()
            }
        }
    }*/

    /*private fun redirectToVerifyNumberScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.id_container, VerifyPhoneNumberFragment(), null)
            .commit()
    }*/
}