package com.regisk.legacy.events.dialog

import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import com.regisk.legacy.R
import com.regisk.legacy.core.base.BaseActivity
import com.regisk.legacy.view.RegiskDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

abstract class MarkDownDialog : DialogEvent() {

    abstract suspend fun getMarkdownText(): String

    @CallSuper
    override fun build(dialog: RegiskDialog) {
        with(dialog) {
            val view = LayoutInflater.from(context).inflate(R.layout.markdown_window_md2, null)
            applyView(view)
            (ownerActivity as BaseActivity).lifecycleScope.launch {
                val tv = view.findViewById<TextView>(R.id.md_txt)
                withContext(Dispatchers.Main) {
                    tv.text = getMarkdownText()
                }
            }
        }
    }
}
