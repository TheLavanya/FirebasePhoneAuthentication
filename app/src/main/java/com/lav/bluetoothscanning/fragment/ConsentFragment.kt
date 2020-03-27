package com.lav.bluetoothscanning.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.lav.bluetoothscanning.R
import com.lav.bluetoothscanning.activity.OnboardingActivity
import com.lav.bluetoothscanning.viewmodel.OnboardingViewModel
import kotlinx.android.synthetic.main.fragment_consent.*

class ConsentFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_consent, container, false)
    }

    private fun initViewModel() {
        viewModel =
            activity?.let { ViewModelProviders.of(it).get(OnboardingViewModel::class.java) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id_button_agree.setOnClickListener {
            viewModel?.setConsentAgreement()
        }
    }
}