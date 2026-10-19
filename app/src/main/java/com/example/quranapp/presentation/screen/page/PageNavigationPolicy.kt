package com.example.quranapp.presentation.screen.page

/**
 * Encapsulates navigation rules, page boundary clamps, and index conversions
 * for the 604-page Madinah Mushaf.
 */
object PageNavigationPolicy {
    const val MIN_PAGE = 1
    const val MAX_PAGE = 604

    fun clampPage(page: Int): Int = page.coerceIn(MIN_PAGE, MAX_PAGE)

    fun pageIndexToPageNumber(index: Int): Int = index + 1

    fun pageNumberToPageIndex(page: Int): Int = (page - 1).coerceIn(0, MAX_PAGE - 1)

    fun hasPrevious(page: Int): Boolean = page > MIN_PAGE

    fun hasNext(page: Int): Boolean = page < MAX_PAGE

    fun getPreviousPage(page: Int): Int = clampPage(page - 1)

    fun getNextPage(page: Int): Int = clampPage(page + 1)
}
