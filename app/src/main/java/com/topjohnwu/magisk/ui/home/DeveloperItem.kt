package com.topjohnwu.magisk.ui.home

import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.databinding.RvItem

sealed class DeveloperItem {

    abstract val items: List<IconLink>
    abstract val name: Int

    object Main : DeveloperItem() {
        override val items = emptyList<IconLink>()
        override val name get() = R.string.topjohnwu
    }

    object App : DeveloperItem() {
        override val items = emptyList<IconLink>()
        override val name get() = R.string.diareuse
    }
}

sealed class IconLink : RvItem() {

    abstract val icon: Int
    abstract val title: Int
    abstract val link: String

    override val layoutRes get() = R.layout.item_icon_link
}
