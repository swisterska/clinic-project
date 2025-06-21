package com.example.eclinic.firebase

/**
 * Data class representing a specific medical visit type offered by a doctor.
 *
 * @property name The name of the visit type (e.g., "Online Chat Consultation", "EKG").
 * @property price The price of the visit, typically including currency (e.g., "70 PLN").
 */
data class Visit(
    val name: String,
    val price: String
)

/**
 * A read-only map that associates each [Specialization] with a list of [Visit] types
 * and their corresponding prices. This map provides a predefined set of services
 * that doctors under each specialization might offer.
 */
val visitsBySpecialization = mapOf(
    Specialization.CARDIOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("EKG", "150 PLN"),
        Visit("Echocardiogram", "250 PLN"),
        Visit("Stress Test", "300 PLN")
    ),
    Specialization.DERMATOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Skin Examination", "120 PLN"),
        Visit("Mole Removal", "180 PLN"),
        Visit("Acne Treatment", "160 PLN")
    ),
    Specialization.NEUROLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("EEG", "220 PLN"),
        Visit("Neurological Exam", "200 PLN"),
        Visit("MRI Consultation", "300 PLN")
    ),
    Specialization.PEDIATRICIAN to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Routine Checkup", "100 PLN"),
        Visit("Vaccination", "80 PLN"),
        Visit("Developmental Assessment", "150 PLN")
    ),
    Specialization.GENERAL_PRACTITIONER to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("General Consultation", "100 PLN"),
        Visit("Flu Shot", "60 PLN"),
        Visit("Health Screening", "130 PLN")
    ),
    Specialization.ORTHOPEDIC_SURGEON to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Joint Examination", "180 PLN"),
        Visit("Fracture Follow-up", "160 PLN"),
        Visit("MRI Review", "250 PLN")
    ),
    Specialization.GYNECOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Pap Smear", "150 PLN"),
        Visit("Ultrasound", "200 PLN"),
        Visit("Birth Control Consultation", "130 PLN")
    ),
    Specialization.ONCOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Chemotherapy Planning", "350 PLN"),
        Visit("Cancer Screening", "300 PLN"),
        Visit("Treatment Follow-up", "280 PLN")
    ),
    Specialization.PSYCHIATRIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Initial Consultation", "200 PLN"),
        Visit("Therapy Session", "180 PLN"),
        Visit("Medication Management", "150 PLN")
    ),
    Specialization.GASTROENTEROLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Colonoscopy Prep Consult", "220 PLN"),
        Visit("Stomach Pain Evaluation", "180 PLN"),
        Visit("Liver Function Review", "200 PLN")
    ),
    Specialization.NEPHROLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Kidney Function Assessment", "210 PLN"),
        Visit("Dialysis Planning", "300 PLN"),
        Visit("Urine Test Review", "150 PLN")
    ),
    Specialization.PULMONOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Pulmonary Function Test", "220 PLN"),
        Visit("Asthma Check-up", "180 PLN"),
        Visit("Sleep Apnea Consult", "250 PLN")
    ),
    Specialization.RHEUMATOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Joint Pain Evaluation", "190 PLN"),
        Visit("Autoimmune Panel Review", "230 PLN"),
        Visit("Arthritis Treatment", "210 PLN")
    ),
    Specialization.UROLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Prostate Exam", "170 PLN"),
        Visit("UTI Consultation", "150 PLN"),
        Visit("Ultrasound (Bladder/Kidney)", "200 PLN")
    ),
    Specialization.OTOLARYNGOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Hearing Test", "160 PLN"),
        Visit("Throat Exam", "140 PLN"),
        Visit("Sinus Check-up", "180 PLN")
    ),
    Specialization.RADIOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("X-ray Review", "130 PLN"),
        Visit("CT Scan Analysis", "300 PLN"),
        Visit("MRI Analysis", "320 PLN")
    ),
    Specialization.PATHOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Biopsy Analysis", "350 PLN"),
        Visit("Lab Result Review", "200 PLN")
    ),
    Specialization.IMMUNOLOGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Immune Function Test", "250 PLN"),
        Visit("Allergy Blood Panel", "280 PLN")
    ),
    Specialization.INFECTIOUS_DISEASE_SPECIALIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Infection Diagnosis", "200 PLN"),
        Visit("Antibiotic Therapy Consult", "180 PLN")
    ),
    Specialization.ALLERGIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Skin Prick Test", "220 PLN"),
        Visit("Allergy Management Plan", "200 PLN")
    ),
    Specialization.SPORTS_MEDICINE_SPECIALIST to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Sports Injury Evaluation", "180 PLN"),
        Visit("Rehabilitation Plan", "200 PLN")
    ),
    Specialization.PLASTIC_SURGEON to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Consultation", "250 PLN"),
        Visit("Scar Revision", "400 PLN")
    ),
    Specialization.FAMILY_MEDICINE to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Routine Checkup", "120 PLN"),
        Visit("Preventive Screening", "150 PLN")
    ),
    Specialization.VASCULAR_SURGEON to listOf(
        Visit("Online Chat Consultation", "70 PLN"),
        Visit("Varicose Vein Consult", "200 PLN"),
        Visit("Doppler Ultrasound", "250 PLN")
    ),
    Specialization.DR_HOUSE to listOf(
        Visit("Online Chat Consultation", "500 PLN"),
        Visit("Rare Disease Consultation", "999 PLN"),
        Visit("Diagnostic Mystery Session", "1200 PLN")
    )
)