package com.example.whatsappclone

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.set
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks //it is the interface that is used to handle different states of verification process
    var phoneNumber: String? = null
    lateinit var verifyTv: TextView
    lateinit var waitingTv: TextView
    lateinit var counterTv: TextView
    lateinit var resendBtn: MaterialButton
    lateinit var sentcodeEt: EditText
    lateinit var verificationButton: MaterialButton
    private lateinit var progressDialog: ProgressDialog
    private var mCounterDown: CountDownTimer? = null
    var mVerificationId: String? = null
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        verifyTv = findViewById(R.id.verifyTv)
        waitingTv = findViewById(R.id.waitingTv)
        counterTv = findViewById(R.id.counterTv)
        resendBtn = findViewById(R.id.resendBtn)
        sentcodeEt = findViewById(R.id.sentcodeEt)
        verificationButton = findViewById(R.id.verificationBtn)
        initViews()
        startVerify()

    }

    private fun startVerify() {
        startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
        progressDialog = createProgressDialog("Sending a Verification code", false)
        progressDialog.show()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,            // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isEnabled = false
        mCounterDown = object : CountDownTimer(milliSecInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {// this will be called in every seconds
                counterTv.isVisible = true
                counterTv.text = getString(R.string.seconds_remaining, millisUntilFinished / 1000)
            }

            override fun onFinish() {// this will be called when the seconds will be over
                resendBtn.isEnabled = true
                counterTv.isVisible = false
            }

        }.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()
        verificationButton.setOnClickListener(this)
        resendBtn.setOnClickListener(this)
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                val smsCode = credential.smsCode
                if (!smsCode.isNullOrBlank()) sentcodeEt.setText(smsCode)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.


                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.


                // Save verification ID and resending token so we can use them later
                progressDialog.dismiss()
                counterTv.isVisible = false
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                startActivity(Intent(this,SignUpActivity::class.java))
                finish()
            } else {
                notifyUserAndRetry("Your phone number verification failed.Try Again!!")
            }
        }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text, phoneNumber))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                //send back
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(
            Intent(
                this, LoginActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        //backstrack handle krte h yeh flags
    }

    override fun onBackPressed() {

    }

    override fun onClick(p0: View?) {
        when (p0) {
            verificationButton -> {
                val code = sentcodeEt.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
                    progressDialog = createProgressDialog("Please wait", false)
                    progressDialog.show()
                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn -> {
                if (mResendToken != null) {
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a Verification code", false)
                    progressDialog.show()
                }
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber!!, 60, TimeUnit.SECONDS, this, callbacks, mResendToken
                )
            }
        }
    }
}

fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}