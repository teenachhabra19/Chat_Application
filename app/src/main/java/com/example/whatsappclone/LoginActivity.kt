package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hbb20.CountryCodePicker

class LoginActivity : AppCompatActivity() {
    private lateinit var phoneNumber:String
    private lateinit var countryCode:String
    lateinit var phoneNumberEt:EditText
    lateinit var nextButton: Button
    lateinit var ccp:CountryCodePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        phoneNumberEt=findViewById(R.id.phoneNumberEt)
        nextButton=findViewById(R.id.nextBtn)
        ccp=findViewById(R.id.ccp)
        phoneNumberEt.addTextChangedListener {
            nextButton.isEnabled=!(it.isNullOrEmpty() || it.length<10)
        }
        nextButton.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
      countryCode=ccp.selectedCountryCodeWithPlus
        phoneNumber=countryCode +phoneNumberEt.text.toString()
        notifyUser()
    }

    private fun notifyUser() {
      MaterialAlertDialogBuilder(this).apply{
          setMessage("We will be verifying the phone number:$phoneNumber\n"+
                     "Is this OK,or would you like to edit the number?")
          setPositiveButton("OK"){_,_->
              showOtpActivity()
          }
          setNegativeButton("Edit"){dialog,which->
              dialog.dismiss()
          }
          setCancelable(false)
          create()
          show()
      }
    }

    private fun showOtpActivity() {
     startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,phoneNumber))
        finish()
    }
}