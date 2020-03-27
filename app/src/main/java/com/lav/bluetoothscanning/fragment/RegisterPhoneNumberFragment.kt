package com.lav.bluetoothscanning.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.lav.bluetoothscanning.R
import com.lav.bluetoothscanning.activity.OnboardingActivity
import com.lav.bluetoothscanning.viewmodel.OnboardingViewModel
import kotlinx.android.synthetic.main.fragment_register_phone_number.*

class RegisterPhoneNumberFragment : Fragment() {

    private var viewModel: OnboardingViewModel? = null

    private var activity: OnboardingActivity? = null

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
        id_button_proceed.setOnClickListener {
            val mobile = id_editText_mobile.text.toString()
            if (mobile.isEmpty() || mobile.length < 10) {
                id_editText_mobile.error = "Enter a valid mobile"
                id_editText_mobile.requestFocus();
            } else {
                viewModel?.mobile = mobile
                viewModel?.redirectToVerify()
            }
        }
    }
}