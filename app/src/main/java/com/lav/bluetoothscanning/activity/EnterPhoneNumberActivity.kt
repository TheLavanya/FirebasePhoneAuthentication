package com.lav.bluetoothscanning.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lav.bluetoothscanning.R
import kotlinx.android.synthetic.main.activity_enter_phone_number.*

class EnterPhoneNumberActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_phone_number)

        buttonContinue.setOnClickListener {
            val mobile = editTextMobile.text.toString()
            if (mobile.isEmpty() || mobile.length < 10) {
                editTextMobile.error = "Enter a valid mobile"
                editTextMobile.requestFocus();
            } else {
                val intent = Intent(this, VerifyPhoneNumberActivity::class.java)
                intent.putExtra("mobile", mobile)
                startActivity(intent)
            }
        }
    }
}