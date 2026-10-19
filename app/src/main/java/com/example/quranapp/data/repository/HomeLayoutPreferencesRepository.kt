package com.example.quranapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.quranapp.presentation.screen.home.HomeCardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing and persisting the Home Screen's card layout order and visibility preferences.
 */
@Singleton
class HomeLayoutPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_CARD_ORDER = stringPreferencesKey("home_layout_card_order")
        private val KEY_HIDDEN_CARDS = stringPreferencesKey("home_layout_hidden_cards")
    }

    /**
     * Continuous reactive Flow of the user's customized card order
     */
    val cardOrderFlow: Flow<List<HomeCardType>> = dataStore.data.map { preferences ->
        HomeCardType.parseOrder(preferences[KEY_CARD_ORDER])
    }

    /**
     * Continuous reactive Flow of the user's hidden cards set
     */
    val hiddenCardsFlow: Flow<Set<HomeCardType>> = dataStore.data.map { preferences ->
        HomeCardType.parseHidden(preferences[KEY_HIDDEN_CARDS])
    }

    suspend fun getCardOrder(): List<HomeCardType> {
        return cardOrderFlow.first()
    }

    suspend fun getHiddenCards(): Set<HomeCardType> {
        return hiddenCardsFlow.first()
    }

    suspend fun saveCardOrder(order: List<HomeCardType>) {
        dataStore.edit { preferences ->
            preferences[KEY_CARD_ORDER] = HomeCardType.serializeOrder(order)
        }
    }

    suspend fun saveHiddenCards(hidden: Set<HomeCardType>) {
        dataStore.edit { preferences ->
            preferences[KEY_HIDDEN_CARDS] = HomeCardType.serializeHidden(hidden)
        }
    }

    suspend fun moveCardUp(card: HomeCardType) {
        val current = getCardOrder()
        val updated = HomeCardType.moveCardUp(current, card)
        if (updated != current) {
            saveCardOrder(updated)
        }
    }

    suspend fun moveCardDown(card: HomeCardType) {
        val current = getCardOrder()
        val updated = HomeCardType.moveCardDown(current, card)
        if (updated != current) {
            saveCardOrder(updated)
        }
    }

    suspend fun swapCards(fromIndex: Int, toIndex: Int) {
        val current = getCardOrder()
        val updated = HomeCardType.swap(current, fromIndex, toIndex)
        if (updated != current) {
            saveCardOrder(updated)
        }
    }

    suspend fun toggleCardVisibility(card: HomeCardType) {
        val currentHidden = getHiddenCards()
        val currentOrder = getCardOrder()
        val updated = HomeCardType.toggleCardVisibility(currentHidden, card, currentOrder)
        if (updated != currentHidden) {
            saveHiddenCards(updated)
        }
    }

    suspend fun resetToDefault() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_CARD_ORDER)
            preferences.remove(KEY_HIDDEN_CARDS)
        }
    }
}
