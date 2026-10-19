package com.example.quranapp.presentation.screen.home

import org.junit.Assert.*
import org.junit.Test

class HomeLayoutPreferencesTest {

    @Test
    fun testDefaultCardOrder_containsAllFourCardsInOriginalSequence() {
        val defaultOrder = HomeCardType.DEFAULT_ORDER
        assertEquals(4, defaultOrder.size)
        assertEquals(HomeCardType.CURRENT_TIME, defaultOrder[0])
        assertEquals(HomeCardType.PRAYER_PROGRESS, defaultOrder[1])
        assertEquals(HomeCardType.PRAYER_TIMES, defaultOrder[2])
        assertEquals(HomeCardType.QUICK_ACCESS, defaultOrder[3])
    }

    @Test
    fun testSerializationAndParsing_roundTripPreservesSequence() {
        val customOrder = listOf(
            HomeCardType.QUICK_ACCESS,
            HomeCardType.PRAYER_TIMES,
            HomeCardType.CURRENT_TIME,
            HomeCardType.PRAYER_PROGRESS
        )
        val serialized = HomeCardType.serializeOrder(customOrder)
        assertEquals("quick_access,prayer_times,current_time,prayer_progress", serialized)

        val parsed = HomeCardType.parseOrder(serialized)
        assertEquals(customOrder, parsed)
    }

    @Test
    fun testParseOrder_nullOrEmptyInput_returnsDefaultOrder() {
        assertEquals(HomeCardType.DEFAULT_ORDER, HomeCardType.parseOrder(null))
        assertEquals(HomeCardType.DEFAULT_ORDER, HomeCardType.parseOrder(""))
        assertEquals(HomeCardType.DEFAULT_ORDER, HomeCardType.parseOrder("   "))
    }

    @Test
    fun testParseOrder_incompleteOrCorruptedInput_recoversSafelyAndAppendsMissing() {
        // Only prayer_times was present, plus an unknown garbage ID
        val corrupted = "prayer_times,garbage_widget_id,prayer_times"
        val parsed = HomeCardType.parseOrder(corrupted)

        // Must start with prayer_times, deduplicated, and contain all 4 cards
        assertEquals(4, parsed.size)
        assertEquals(HomeCardType.PRAYER_TIMES, parsed[0])
        assertTrue(parsed.contains(HomeCardType.CURRENT_TIME))
        assertTrue(parsed.contains(HomeCardType.PRAYER_PROGRESS))
        assertTrue(parsed.contains(HomeCardType.QUICK_ACCESS))
        assertEquals(4, parsed.distinct().size)
    }

    @Test
    fun testMoveCardUp_swapsWithPreviousElement() {
        val initial = listOf(
            HomeCardType.CURRENT_TIME,
            HomeCardType.PRAYER_PROGRESS,
            HomeCardType.PRAYER_TIMES,
            HomeCardType.QUICK_ACCESS
        )

        // Move PRAYER_PROGRESS up -> should swap with CURRENT_TIME
        val result = HomeCardType.moveCardUp(initial, HomeCardType.PRAYER_PROGRESS)
        assertEquals(
            listOf(
                HomeCardType.PRAYER_PROGRESS,
                HomeCardType.CURRENT_TIME,
                HomeCardType.PRAYER_TIMES,
                HomeCardType.QUICK_ACCESS
            ),
            result
        )
    }

    @Test
    fun testMoveCardUp_topCardIsNoOp() {
        val initial = HomeCardType.DEFAULT_ORDER
        val result = HomeCardType.moveCardUp(initial, HomeCardType.CURRENT_TIME)
        assertEquals(initial, result)
    }

    @Test
    fun testMoveCardDown_swapsWithNextElement() {
        val initial = listOf(
            HomeCardType.CURRENT_TIME,
            HomeCardType.PRAYER_PROGRESS,
            HomeCardType.PRAYER_TIMES,
            HomeCardType.QUICK_ACCESS
        )

        // Move PRAYER_PROGRESS down -> should swap with PRAYER_TIMES
        val result = HomeCardType.moveCardDown(initial, HomeCardType.PRAYER_PROGRESS)
        assertEquals(
            listOf(
                HomeCardType.CURRENT_TIME,
                HomeCardType.PRAYER_TIMES,
                HomeCardType.PRAYER_PROGRESS,
                HomeCardType.QUICK_ACCESS
            ),
            result
        )
    }

    @Test
    fun testMoveCardDown_bottomCardIsNoOp() {
        val initial = HomeCardType.DEFAULT_ORDER
        val result = HomeCardType.moveCardDown(initial, HomeCardType.QUICK_ACCESS)
        assertEquals(initial, result)
    }

    @Test
    fun testSwapCards_validIndices() {
        val initial = HomeCardType.DEFAULT_ORDER
        // Swap 0 (CURRENT_TIME) and 3 (QUICK_ACCESS)
        val result = HomeCardType.swap(initial, 0, 3)
        assertEquals(HomeCardType.QUICK_ACCESS, result[0])
        assertEquals(HomeCardType.PRAYER_PROGRESS, result[1])
        assertEquals(HomeCardType.PRAYER_TIMES, result[2])
        assertEquals(HomeCardType.CURRENT_TIME, result[3])
    }

    @Test
    fun testSwapCards_outOfBoundsIndices_isSafeNoOp() {
        val initial = HomeCardType.DEFAULT_ORDER
        assertEquals(initial, HomeCardType.swap(initial, -1, 2))
        assertEquals(initial, HomeCardType.swap(initial, 0, 10))
    }

    @Test
    fun testHiddenCardsSerializationAndToggling() {
        val hiddenSet = setOf(HomeCardType.CURRENT_TIME, HomeCardType.PRAYER_PROGRESS)
        val serialized = HomeCardType.serializeHidden(hiddenSet)
        assertEquals("current_time,prayer_progress", serialized)

        val parsed = HomeCardType.parseHidden(serialized)
        assertEquals(hiddenSet, parsed)

        // Parsing empty or blank
        assertTrue(HomeCardType.parseHidden(null).isEmpty())
        assertTrue(HomeCardType.parseHidden("").isEmpty())
    }

    @Test
    fun testToggleVisibility_cannotHideAllCards() {
        val allCards = HomeCardType.DEFAULT_ORDER.toSet()
        // If 3 cards are already hidden, attempting to hide the 4th must be prevented
        val threeHidden = setOf(
            HomeCardType.CURRENT_TIME,
            HomeCardType.PRAYER_PROGRESS,
            HomeCardType.PRAYER_TIMES
        )
        val result = HomeCardType.toggleCardVisibility(
            currentHidden = threeHidden,
            cardToToggle = HomeCardType.QUICK_ACCESS,
            allCards = HomeCardType.DEFAULT_ORDER
        )
        // Should NOT allow hiding QUICK_ACCESS since it is the last visible card
        assertEquals(threeHidden, result)

        // But un-hiding a hidden card must always succeed
        val unhiddenResult = HomeCardType.toggleCardVisibility(
            currentHidden = threeHidden,
            cardToToggle = HomeCardType.CURRENT_TIME,
            allCards = HomeCardType.DEFAULT_ORDER
        )
        assertFalse(unhiddenResult.contains(HomeCardType.CURRENT_TIME))
        assertEquals(2, unhiddenResult.size)
    }

    @Test
    fun testHomeUiState_defaultsAndVisibilityComputations() {
        val defaultState = HomeUiState()
        assertEquals(HomeCardType.DEFAULT_ORDER, defaultState.cardOrder)
        assertTrue(defaultState.hiddenCards.isEmpty())
        assertFalse(defaultState.isEditMode)
        assertEquals(4, defaultState.visibleCards.size)

        // All cards visible initially
        HomeCardType.entries.forEach { card ->
            assertTrue("Card $card should be visible by default", defaultState.isCardVisible(card))
        }

        // Hide clock card
        val customState = defaultState.copy(
            cardOrder = listOf(
                HomeCardType.QUICK_ACCESS,
                HomeCardType.PRAYER_PROGRESS,
                HomeCardType.PRAYER_TIMES,
                HomeCardType.CURRENT_TIME
            ),
            hiddenCards = setOf(HomeCardType.CURRENT_TIME),
            isEditMode = true
        )
        assertFalse(customState.isCardVisible(HomeCardType.CURRENT_TIME))
        assertTrue(customState.isCardVisible(HomeCardType.QUICK_ACCESS))
        assertEquals(3, customState.visibleCards.size)
        assertEquals(HomeCardType.QUICK_ACCESS, customState.visibleCards[0])
        assertEquals(HomeCardType.PRAYER_PROGRESS, customState.visibleCards[1])
        assertEquals(HomeCardType.PRAYER_TIMES, customState.visibleCards[2])
    }
}
