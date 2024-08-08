package com.example.quranapp.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection

fun addPaddingValues(original: PaddingValues, additional: PaddingValues): PaddingValues {
    return PaddingValues(
        start = original.calculateStartPadding(LayoutDirection.Ltr) + additional.calculateStartPadding(LayoutDirection.Ltr),
        top = original.calculateTopPadding() + additional.calculateTopPadding(),
        end = original.calculateEndPadding(LayoutDirection.Ltr) + additional.calculateEndPadding(LayoutDirection.Ltr),
        bottom = original.calculateBottomPadding() + additional.calculateBottomPadding()
    )
}