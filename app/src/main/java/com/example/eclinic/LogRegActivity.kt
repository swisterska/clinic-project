package com.example.eclinic

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.integrity.p

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

        // Set up the "Login" button to navigate to the LoginActivity when clicked
        val buttonLogin = findViewById<ImageButton>(R.id.ChoiceLoginButton)
        buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Set up the "Register" button to navigate to the RegisterActivity when clicked
        val buttonRegister = findViewById<ImageButton>(R.id.ChoiceRegisterButton)
        buttonRegister.setOnClickListener {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val doctoraccount = findViewById<Button>(R.id.Gotodoctor)
        doctoraccount.setOnClickListener {
            val intent = Intent(this, MainPageDoctor::class.java)
            startActivity(intent)
        }

        val patientbutton = findViewById<Button>(R.id.Gotopatient)
        patientbutton.setOnClickListener {
            val intent = Intent(this, MainPagePatient::class.java)
            startActivity(intent)
        }
    }
}
