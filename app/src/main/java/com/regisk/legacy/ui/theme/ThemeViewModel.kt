package com.regisk.legacy.ui.theme

import com.regisk.legacy.arch.BaseViewModel
import com.regisk.legacy.events.RecreateEvent
import com.regisk.legacy.events.dialog.DarkThemeDialog
import com.regisk.legacy.view.TappableHeadlineItem

class ThemeViewModel : BaseViewModel(), TappableHeadlineItem.Listener {

    val themeHeadline = TappableHeadlineItem.ThemeMode

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        is TappableHeadlineItem.ThemeMode -> darkModePressed()
        else -> Unit
    }

    fun saveTheme(theme: Theme) {
        theme.select()
        RecreateEvent().publish()
    }

    private fun darkModePressed() = DarkThemeDialog().publish()

}
