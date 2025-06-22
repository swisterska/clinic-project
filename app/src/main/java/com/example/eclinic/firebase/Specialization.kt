package com.example.eclinic.firebase

/**
 * Enum class representing various medical specializations available in the eClinic system.
 * Each specialization has a `displayName` property for a user-friendly representation.
 *
 * @property displayName The human-readable name of the specialization.
 */
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

    /**
     * Companion object providing utility functions for the [Specialization] enum.
     */
    companion object {
        /**
         * Returns the [Specialization] enum entry corresponding to the given display string,
         * or `null` if no match is found (case-insensitive).
         *
         * @param value The display name string to match against the enum entries.
         * @return The matching [Specialization] enum entry, or `null` if not found.
         */
        fun fromString(value: String): Specialization? =
            values().find { it.displayName.equals(value, ignoreCase = true) }
    }
}