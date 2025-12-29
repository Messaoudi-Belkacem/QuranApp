package com.example.quranapp.domain.model

/**
 * Represents a prayer time calculation method with its specific angles and parameters
 */
data class PrayerCalculationMethod(
    val id: String,
    val displayName: String,
    val fajrAngle: Double?,
    val ishaAngle: Double?,
    val ishaInterval: Int? = null, // minutes after Maghrib
    val maghribInterval: Int? = null, // minutes after sunset (rare)
    val description: String = ""
) {
    companion object {
        /**
         * Muslim World League (MWL)
         * Used globally, especially in Europe and parts of the Americas
         */
        val MWL = PrayerCalculationMethod(
            id = "mwl",
            displayName = "Muslim World League",
            fajrAngle = 18.0,
            ishaAngle = 17.0,
            description = "Standard method used worldwide"
        )

        /**
         * Egyptian General Authority of Survey
         * Used in Africa, Syria, Iraq, Lebanon, Malaysia
         */
        val EGYPTIAN = PrayerCalculationMethod(
            id = "egyptian",
            displayName = "Egyptian Authority",
            fajrAngle = 19.5,
            ishaAngle = 17.5,
            description = "Used in Africa and Middle East"
        )

        /**
         * Umm al-Qura University, Makkah
         * Used in Saudi Arabia
         */
        val UMM_AL_QURA = PrayerCalculationMethod(
            id = "makkah",
            displayName = "Umm al-Qura (Makkah)",
            fajrAngle = 18.5,
            ishaAngle = null,
            ishaInterval = 90, // 90 minutes after Maghrib (120 in Ramadan)
            description = "Official method of Saudi Arabia"
        )

        /**
         * Islamic Society of North America (ISNA)
         * Used in North America
         */
        val ISNA = PrayerCalculationMethod(
            id = "isna",
            displayName = "ISNA (North America)",
            fajrAngle = 15.0,
            ishaAngle = 15.0,
            description = "Used in North America"
        )

        /**
         * University of Islamic Sciences, Karachi
         * Used in Pakistan, Bangladesh, India, Afghanistan
         */
        val KARACHI = PrayerCalculationMethod(
            id = "karachi",
            displayName = "University of Karachi",
            fajrAngle = 18.0,
            ishaAngle = 18.0,
            description = "Used in South Asia"
        )

        /**
         * Institute of Geophysics, University of Tehran
         * Used in Iran
         */
        val TEHRAN = PrayerCalculationMethod(
            id = "tehran",
            displayName = "Institute of Tehran",
            fajrAngle = 17.7,
            ishaAngle = 14.0,
            description = "Used in Iran"
        )

        /**
         * Spiritual Administration of Muslims of Russia
         */
        val RUSSIA = PrayerCalculationMethod(
            id = "russia",
            displayName = "Russia",
            fajrAngle = 16.0,
            ishaAngle = 15.0,
            description = "Used in Russia"
        )

        /**
         * Moonsighting Committee Worldwide
         */
        val MOONSIGHTING = PrayerCalculationMethod(
            id = "moonsighting",
            displayName = "Moonsighting Committee",
            fajrAngle = 18.0,
            ishaAngle = 18.0,
            description = "Based on actual moonsighting"
        )

        /**
         * Get all available calculation methods
         */
        fun getAllMethods(): List<PrayerCalculationMethod> = listOf(
            MWL,
            EGYPTIAN,
            UMM_AL_QURA,
            ISNA,
            KARACHI,
            TEHRAN,
            RUSSIA,
            MOONSIGHTING
        )

        /**
         * Get method by ID
         */
        fun getMethodById(id: String): PrayerCalculationMethod? {
            return getAllMethods().find { it.id == id }
        }

        /**
         * Get default method based on country code
         */
        fun getDefaultMethodForCountry(countryCode: String): PrayerCalculationMethod {
            return when (countryCode.uppercase()) {
                "SA", "AE", "KW", "QA", "BH", "OM", "YE" -> UMM_AL_QURA // Gulf countries
                "EG", "SY", "IQ", "LB", "JO", "PS", "MY", "SG" -> EGYPTIAN // Egypt, Syria, Malaysia
                "US", "CA" -> ISNA // North America
                "PK", "BD", "IN", "AF" -> KARACHI // South Asia
                "IR" -> TEHRAN // Iran
                "RU", "KZ", "UZ", "TM", "KG", "TJ", "AZ" -> RUSSIA // Russia & Central Asia
                else -> MWL // Default for rest of world
            }
        }
    }
}

/**
 * Asr calculation method (Juristic opinion)
 */
enum class AsrCalculationMethod(
    val displayName: String,
    val shadowFactor: Int,
    val description: String
) {
    SHAFII(
        displayName = "Standard (Shafi'i, Maliki, Hanbali)",
        shadowFactor = 1,
        description = "Shadow length = object length + 1"
    ),
    HANAFI(
        displayName = "Hanafi",
        shadowFactor = 2,
        description = "Shadow length = object length + 2 (later time)"
    );

    companion object {
        fun fromString(name: String): AsrCalculationMethod {
            return when (name.uppercase()) {
                "HANAFI" -> HANAFI
                else -> SHAFII
            }
        }
    }
}

/**
 * High latitude adjustment method
 * Used when Fajr or Isha cannot be calculated normally
 */
enum class HighLatitudeMethod(
    val displayName: String,
    val description: String
) {
    ANGLE_BASED(
        displayName = "Angle-Based",
        description = "Night portion = angle / 60"
    ),
    MIDDLE_OF_NIGHT(
        displayName = "Middle of Night",
        description = "Night divided by 2"
    ),
    ONE_SEVENTH(
        displayName = "One-Seventh of Night",
        description = "Night divided by 7"
    ),
    NONE(
        displayName = "None",
        description = "No adjustment applied"
    );

    companion object {
        fun fromString(name: String): HighLatitudeMethod {
            return when (name.uppercase().replace(" ", "_")) {
                "MIDDLE_OF_NIGHT", "MIDDLEOFNIGHT" -> MIDDLE_OF_NIGHT
                "ONE_SEVENTH", "ONESEVENTH" -> ONE_SEVENTH
                "NONE" -> NONE
                else -> ANGLE_BASED
            }
        }
    }
}

