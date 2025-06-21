package com.example.eclinic.doctorClasses

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eclinic.R

/**
 * Activity that displays the doctor's calendar interface.
 *
 * This activity enables edge-to-edge display mode,
 * sets the content view to the calendar layout,
 * and applies system window insets as padding to the root view
 * to properly handle system bars (status bar, navigation bar).
 */
class CalendarActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting.
     *
     * Sets up the UI and configures window insets for edge-to-edge layout.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied.
     * Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
