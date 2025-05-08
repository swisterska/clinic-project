package com.example.eclinic.logRegClasses

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.eclinic.R
import com.example.eclinic.calendar.MainCalendarActivity
import com.example.eclinic.doctorClasses.MainPageDoctor
import com.example.eclinic.patientClasses.MainPagePatient

/**
 * LogRegActivity is the activity where the user chooses to either log in, register, or go back to the main page.
 * It handles the navigation between different screens like the LoginActivity, RegisterActivity, and MainPageActivity.
 */
class LogRegActivity : AppCompatActivity() {

    /**
     * This method is called when the activity is created.
     * It sets up the UI elements and defines click listeners for buttons to navigate to other activities.
     *
     * @param savedInstanceState a Bundle object that contains the activity's previously saved state, or null if there is no state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_reg_activity)

        // Set up the "Login" text to navigate to the LoginActivity when clicked
        val textLogin = findViewById<TextView>(R.id.LoginTextView)
        textLogin.isClickable = true
        textLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Register" button to navigate to the RegisterActivity when clicked
        val buttonRegister = findViewById<ImageButton>(R.id.ChoiceRegisterButton)
        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Doctor" button to navigate to MainPageDoctor
        val doctorButton = findViewById<Button>(R.id.Gotodoctor)
        doctorButton.setOnClickListener {
            val intent = Intent(this, MainPageDoctor::class.java)
            startActivity(intent)
        }

        // Set up the "Patient" button to navigate to MainPagePatient
        val patientButton = findViewById<Button>(R.id.Gotopatient)
        patientButton.setOnClickListener {
            val intent = Intent(this, MainPagePatient::class.java)
            startActivity(intent)
        }


    }
}
