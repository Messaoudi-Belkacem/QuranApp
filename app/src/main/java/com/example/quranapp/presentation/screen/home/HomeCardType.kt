package com.example.quranapp.presentation.screen.home

import androidx.annotation.StringRes
import com.example.quranapp.R

/**
 * Modular cards that can be reordered, hidden, or displayed on the Home Screen.
 */
enum class HomeCardType(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    CURRENT_TIME(
        id = "current_time",
        titleRes = R.string.card_current_time,
        descriptionRes = R.string.card_current_time_desc
    ),
    PRAYER_PROGRESS(
        id = "prayer_progress",
        titleRes = R.string.card_prayer_progress,
        descriptionRes = R.string.card_prayer_progress_desc
    ),
    PRAYER_TIMES(
        id = "prayer_times",
        titleRes = R.string.card_prayer_times,
        descriptionRes = R.string.card_prayer_times_desc
    ),
    QUICK_ACCESS(
        id = "quick_access",
        titleRes = R.string.card_quick_access,
        descriptionRes = R.string.card_quick_access_desc
    );

    companion object {
        /**
         * The default visual sequence matching original production layout
         */
        val DEFAULT_ORDER: List<HomeCardType> = listOf(
            CURRENT_TIME,
            PRAYER_PROGRESS,
            PRAYER_TIMES,
            QUICK_ACCESS
        )

        /**
         * Resolves enum entry by stable string identifier
         */
        fun fromId(id: String): HomeCardType? = entries.firstOrNull { it.id.equals(id, ignoreCase = true) }

        /**
         * Parses comma-separated string into a validated card sequence.
         * Appends any missing cards to ensure no card is unintentionally permanently lost.
         */
        fun parseOrder(serialized: String?): List<HomeCardType> {
            if (serialized.isNullOrBlank()) return DEFAULT_ORDER
            val parsed = serialized.split(",")
                .map { it.trim() }
                .mapNotNull { fromId(it) }
                .distinct()

            if (parsed.isEmpty()) return DEFAULT_ORDER
            val missing = entries.filterNot { it in parsed }
            return parsed + missing
        }

        /**
         * Serializes an ordered list of cards into a comma-separated string for DataStore storage.
         */
        fun serializeOrder(order: List<HomeCardType>): String {
            return order.joinToString(",") { it.id }
        }

        /**
         * Parses comma-separated hidden cards string into a set of HomeCardType
         */
        fun parseHidden(serialized: String?): Set<HomeCardType> {
            if (serialized.isNullOrBlank()) return emptySet()
            return serialized.split(",")
                .map { it.trim() }
                .mapNotNull { fromId(it) }
                .toSet()
        }

        /**
         * Serializes a set of hidden cards into a comma-separated string
         */
        fun serializeHidden(hidden: Set<HomeCardType>): String {
            return hidden.joinToString(",") { it.id }
        }

        /**
         * Swaps card with its predecessor if not already at the top
         */
        fun moveCardUp(currentOrder: List<HomeCardType>, card: HomeCardType): List<HomeCardType> {
            val index = currentOrder.indexOf(card)
            if (index <= 0) return currentOrder
            val mutable = currentOrder.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index - 1]
            mutable[index - 1] = temp
            return mutable
        }

        /**
         * Swaps card with its successor if not already at the bottom
         */
        fun moveCardDown(currentOrder: List<HomeCardType>, card: HomeCardType): List<HomeCardType> {
            val index = currentOrder.indexOf(card)
            if (index < 0 || index >= currentOrder.size - 1) return currentOrder
            val mutable = currentOrder.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index + 1]
            mutable[index + 1] = temp
            return mutable
        }

        /**
         * Swaps elements at indices [fromIndex] and [toIndex]
         */
        fun swap(currentOrder: List<HomeCardType>, fromIndex: Int, toIndex: Int): List<HomeCardType> {
            if (fromIndex !in currentOrder.indices || toIndex !in currentOrder.indices || fromIndex == toIndex) {
                return currentOrder
            }
            val mutable = currentOrder.toMutableList()
            val temp = mutable[fromIndex]
            mutable[fromIndex] = mutable[toIndex]
            mutable[toIndex] = temp
            return mutable
        }

        /**
         * Toggles card visibility, enforcing that at least one card stays visible at all times.
         */
        fun toggleCardVisibility(
            currentHidden: Set<HomeCardType>,
            cardToToggle: HomeCardType,
            allCards: List<HomeCardType> = DEFAULT_ORDER
        ): Set<HomeCardType> {
            return if (cardToToggle in currentHidden) {
                currentHidden - cardToToggle
            } else {
                val visibleCount = allCards.count { it !in currentHidden }
                if (visibleCount <= 1) {
                    currentHidden
                } else {
                    currentHidden + cardToToggle
                }
            }
        }
    }
}
