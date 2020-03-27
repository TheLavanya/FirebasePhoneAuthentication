package com.lav.bluetoothscanning.model

import java.lang.Exception

data class VerificationStatus(
    val status: Boolean,
    val code: String?,
    val error: Exception?
)