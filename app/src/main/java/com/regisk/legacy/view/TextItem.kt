package com.regisk.legacy.view

import com.regisk.legacy.R
import com.regisk.legacy.databinding.ComparableRvItem

class TextItem(val text: Int) : ComparableRvItem<TextItem>() {
    override val layoutRes = R.layout.item_text

    override fun contentSameAs(other: TextItem) = text == other.text
    override fun itemSameAs(other: TextItem) = contentSameAs(other)
}
