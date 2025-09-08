package com.example.quranapp.data.model

data class Location(
    val latitude: Float,
    val longitude: Float
) {
    override fun toString(): String {
        return "$latitude,$longitude"
    }

    companion object {
        fun fromString(locationString: String): Location? {
            return try {
                val parts = locationString.split(",")
                if (parts.size == 2) {
                    Location(
                        latitude = parts[0].toFloat(),
                        longitude = parts[1].toFloat()
                    )
                } else null
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}
