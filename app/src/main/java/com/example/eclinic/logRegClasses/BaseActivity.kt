package com.example.eclinic.logRegClasses

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.eclinic.R
import com.google.android.material.snackbar.Snackbar

/**
 * Base class for all activities in the eClinic application.
 * This class extends [AppCompatActivity] and provides common utility methods
 * that can be reused across different activities, promoting code reusability
 * and consistency in UI feedback mechanisms.
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * Displays a [Snackbar] with a specified message to the user.
     * The appearance of the Snackbar (background color) changes based on
     * whether the message represents an error or a success.
     *
     * @param message The [String] content to be displayed within the Snackbar.
     * @param errorMessage A [Boolean] flag. If `true`, the Snackbar's background
     * will be set to an error color (e.colorSnackBarError).
     * If `false`, it will be set to a success color (R.color.colorSnackBarSuccess).
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        // Create a Snackbar instance, attaching it to the activity's root content view.
        // It will display the provided message for a long duration.
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view

        // Set the background color of the Snackbar's view.
        // The color chosen depends on the `errorMessage` flag.
        snackbarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity, // Use the current activity's context
                if (errorMessage) R.color.colorSnackBarError else R.color.colorSnackBarSuccess
            )
        )
        // Show the Snackbar to the user.
        snackbar.show()
    }
}