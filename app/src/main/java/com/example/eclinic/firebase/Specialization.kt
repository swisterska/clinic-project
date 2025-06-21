package com.example.eclinic.firebase

/**
 * Enum class representing various medical specializations.
 * Each specialization has a [displayName] which is a more readable string representation.
 *
 * @property displayName The human-readable name of the specialization.
 */
enum class Specialization(val displayName: String) {
    /** Represents a Cardiologist specialization. */
    CARDIOLOGIST("Cardiologist"),
    /** Represents a Dermatologist specialization. */
    DERMATOLOGIST("Dermatologist"),
    /** Represents a Neurologist specialization. */
    NEUROLOGIST("Neurologist"),
    /** Represents a Pediatrician specialization. */
    PEDIATRICIAN("Pediatrician"),
    /** Represents a General Practitioner specialization. */
    GENERAL_PRACTITIONER("General Practitioner"),
    /** Represents an Orthopedic Surgeon specialization. */
    ORTHOPEDIC_SURGEON("Orthopedic Surgeon"),
    /** Represents a Gynecologist specialization. */
    GYNECOLOGIST("Gynecologist"),
    /** Represents an Oncologist specialization. */
    ONCOLOGIST("Oncologist"),
    /** Represents a Psychiatrist specialization. */
    PSYCHIATRIST("Psychiatrist"),
    /** Represents a Gastroenterologist specialization. */
    GASTROENTEROLOGIST("Gastroenterologist"),
    /** Represents a Nephrologist specialization. */
    NEPHROLOGIST("Nephrologist"),
    /** Represents a Pulmonologist specialization. */
    PULMONOLOGIST("Pulmonologist"),
    /** Represents a Rheumatologist specialization. */
    RHEUMATOLOGIST("Rheumatologist"),
    /** Represents a Urologist specialization. */
    UROLOGIST("Urologist"),
    /** Represents an Otolaryngologist (ENT) specialization. */
    OTOLARYNGOLOGIST("Otolaryngologist (ENT)"),
    /** Represents a Radiologist specialization. */
    RADIOLOGIST("Radiologist"),
    /** Represents a Pathologist specialization. */
    PATHOLOGIST("Pathologist"),
    /** Represents an Immunologist specialization. */
    IMMUNOLOGIST("Immunologist"),
    /** Represents an Infectious Disease Specialist specialization. */
    INFECTIOUS_DISEASE_SPECIALIST("Infectious Disease Specialist"),
    /** Represents an Allergist specialization. */
    ALLERGIST("Allergist"),
    /** Represents a Sports Medicine Specialist specialization. */
    SPORTS_MEDICINE_SPECIALIST("Sports Medicine Specialist"),
    /** Represents a Plastic Surgeon specialization. */
    PLASTIC_SURGEON("Plastic Surgeon"),
    /** Represents a Family Medicine specialization. */
    FAMILY_MEDICINE("Family Medicine"),
    /** Represents a Vascular Surgeon specialization. */
    VASCULAR_SURGEON("Vascular Surgeon"),
    /** Represents a fictional "Dr_House" specialization, likely for testing or placeholder. */
    DR_HOUSE("Dr_House");

    companion object {
        /**
         * Converts a string value to a [Specialization] enum entry.
         * The comparison is case-insensitive.
         *
         * @param value The string representation of the specialization (e.g., "Cardiologist").
         * @return The corresponding [Specialization] enum entry, or `null` if no match is found.
         */
        fun fromString(value: String): Specialization? =
            values().find { it.displayName.equals(value, ignoreCase = true) }
    }
}