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
    GASTROENTEROLOGIST("Gastroenterologist"),
    NEPHROLOGIST("Nephrologist"),
    PULMONOLOGIST("Pulmonologist"),
    RHEUMATOLOGIST("Rheumatologist"),
    UROLOGIST("Urologist"),
    OTOLARYNGOLOGIST("Otolaryngologist (ENT)"),
    RADIOLOGIST("Radiologist"),
    PATHOLOGIST("Pathologist"),
    IMMUNOLOGIST("Immunologist"),
    INFECTIOUS_DISEASE_SPECIALIST("Infectious Disease Specialist"),
    ALLERGIST("Allergist"),
    SPORTS_MEDICINE_SPECIALIST("Sports Medicine Specialist"),
    PLASTIC_SURGEON("Plastic Surgeon"),
    FAMILY_MEDICINE("Family Medicine"),
    VASCULAR_SURGEON("Vascular Surgeon"),
    DR_HOUSE("Dr_House");

    companion object {
        fun fromString(value: String): Specialization? =
            values().find { it.displayName.equals(value, ignoreCase = true) }
    }
}
