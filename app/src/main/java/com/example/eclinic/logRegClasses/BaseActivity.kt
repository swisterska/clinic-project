package com.example.eclinic.logRegClasses

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.eclinic.R
import com.google.android.material.snackbar.Snackbar

/**
 * Base class for all activities in the application.
 * Provides utility methods shared across activities, such as displaying a Snackbar.
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * Displays a Snackbar with a specified message.
     *
     * @param message The message to display in the Snackbar.
     * @param errorMessage A flag indicating whether the message represents an error (true) or success (false).
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        // Create a Snackbar with the specified message
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Set the background color of the Snackbar based on the message type
        snackbarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                if (errorMessage) R.color.colorSnackBarError else R.color.colorSnackBarSuccess
            )
        )
        // Display the Snackbar
        snackbar.show()
    }
}