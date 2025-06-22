import com.example.eclinic.patientClasses.Patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eclinic.R

/**
 * [PatientAdapter] is a [RecyclerView.Adapter] that displays a list of [Patient] objects.
 * Each item in the list shows the patient's profile picture, name, email, and a chat button.
 * It handles click events on the chat button to initiate a conversation with the patient.
 *
 * @param patientList The list of [Patient] objects to be displayed in the RecyclerView.
 * @param onPatientClick A lambda function invoked when the chat button for a patient is clicked.
 * It receives the [Patient] object corresponding to the clicked item.
 */
class PatientAdapter(
    private val patientList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    /**
     * [PatientViewHolder] is a [RecyclerView.ViewHolder] that holds the views for a single patient item.
     * It provides direct access to all the UI elements within a patient list item.
     *
     * @param view The root [View] of the item layout (e.g., `item_patient.xml`).
     */
    class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profilePic: ImageView = view.findViewById(R.id.patient_image)
        val patientName: TextView = view.findViewById(R.id.patient_name)
        val patientEmail: TextView = view.findViewById(R.id.patient_email)
        val chatButton: Button = view.findViewById(R.id.chat_button)
    }

    /**
     * Called when [RecyclerView] needs a new [PatientViewHolder] of the given type to represent an item.
     * This method inflates the layout for a single patient item from `item_patient.xml`.
     *
     * @param parent The [ViewGroup] into which the new [View] will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new [View].
     * @return A new [PatientViewHolder] that holds a [View] of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    /**
     * Called by [RecyclerView] to display the data at the specified `position`.
     * This method updates the contents of the [holder]'s [itemView] to reflect the patient
     * item at the given `position`. It sets the patient's name, email, and profile picture,
     * and attaches a click listener to the chat button.
     *
     * @param holder The [PatientViewHolder] which should be updated to represent the contents of the
     * item at the given `position` in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]

        holder.patientName.text = "${patient.firstName} ${patient.lastName}"
        holder.patientEmail.text = patient.email

        // Sets a default image resource for the patient's profile picture.
        // Note: This code currently uses a static default image. If dynamic image loading
        // from a URL is desired (e.g., from `patient.profilePictureUrl`), a library
        // like Glide or Picasso would be needed here, along with an actual URL in the Patient data class.
        holder.profilePic.setImageResource(R.drawable.default_patient)

        // Set click listener for the chat button, invoking the provided lambda
        holder.chatButton.setOnClickListener {
            onPatientClick(patient)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = patientList.size
}