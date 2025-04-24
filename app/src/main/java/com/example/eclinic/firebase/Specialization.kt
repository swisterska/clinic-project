package com.example.eclinic.firebase

enum class Specialization(val displayName: String) {
    CARDIOLOGIST("Cardiologist"),
    DERMATOLOGIST("Dermatologist"),
    NEUROLOGIST("Neurologist"),
    PEDIATRICIAN("Pediatrician"),
    GENERAL_PRACTITIONER("General Practitioner"),
    ORTHOPEDIC_SURGEON("Orthopedic Surgeon"),
    GYNECOLOGIST("Gynecologist"),
    ONCOLOGIST("Oncologist"),
    PSYCHIATRIST("Psychiatrist"),
    ENDOCRINOLOGIST("Endocrinologist"),
    GASTROENTEROLOGIST("Gastroenterologist"),
    NEPHROLOGIST("Nephrologist"),
    PULMONOLOGIST("Pulmonologist"),
    RHEUMATOLOGIST("Rheumatologist"),
    UROLOGIST("Urologist"),
    OPHTHALMOLOGIST("Ophthalmologist"),
    OTOLARYNGOLOGIST("Otolaryngologist (ENT)"),
    RADIOLOGIST("Radiologist"),
    PATHOLOGIST("Pathologist"),
    ANESTHESIOLOGIST("Anesthesiologist"),
    IMMUNOLOGIST("Immunologist"),
    INFECTIOUS_DISEASE_SPECIALIST("Infectious Disease Specialist"),
    HEMATOLOGIST("Hematologist"),
    ALLERGIST("Allergist"),
    SPORTS_MEDICINE_SPECIALIST("Sports Medicine Specialist"),
    PLASTIC_SURGEON("Plastic Surgeon"),
    EMERGENCY_MEDICINE("Emergency Medicine"),
    FAMILY_MEDICINE("Family Medicine"),
    GERIATRICIAN("Geriatrician"),
    NEONATOLOGIST("Neonatologist"),
    NUCLEAR_MEDICINE_SPECIALIST("Nuclear Medicine Specialist"),
    SLEEP_MEDICINE_SPECIALIST("Sleep Medicine Specialist"),
    VASCULAR_SURGEON("Vascular Surgeon"),
    PAIN_MANAGEMENT_SPECIALIST("Pain Management Specialist"),
    DIFFERENT("Different/None of the above"),
    DR_HOUSE("Dr.House");

    companion object {
        fun fromString(value: String): Specialization? =
            values().find { it.displayName.equals(value, ignoreCase = true) }
    }
}
