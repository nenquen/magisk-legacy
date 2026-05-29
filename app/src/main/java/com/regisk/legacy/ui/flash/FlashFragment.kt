package com.regisk.legacy.ui.flash

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.navigation.NavDeepLinkBuilder
import com.regisk.legacy.MainDirections
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseUIFragment
import com.regisk.legacy.core.Const
import com.regisk.legacy.core.cmp
import com.regisk.legacy.databinding.FragmentFlashMd2Binding
import com.regisk.legacy.di.viewModel
import com.regisk.legacy.ui.MainActivity

class FlashFragment : BaseUIFragment<FlashViewModel, FragmentFlashMd2Binding>() {

    override val layoutRes = R.layout.fragment_flash_md2
    override val viewModel by viewModel<FlashViewModel>()

    private var defaultOrientation = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.args = FlashFragmentArgs.fromBundle(requireArguments())
    }

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        activity.setTitle(R.string.flash_screen_title)

        viewModel.subtitle.observe(this) {
            activity.supportActionBar?.setSubtitle(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_flash, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return viewModel.onMenuItemClicked(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        viewModel.startFlashing()
    }

    @SuppressLint("WrongConstant")
    override fun onDestroyView() {
        if (defaultOrientation != -1) {
            activity.requestedOrientation = defaultOrientation
        }
        super.onDestroyView()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return when(event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> true
            else -> false
        }
    }

    override fun onBackPressed(): Boolean {
        if (viewModel.loading) return true
        return super.onBackPressed()
    }

    override fun onPreBind(binding: FragmentFlashMd2Binding) = Unit

    companion object {

        private fun createIntent(context: Context, args: FlashFragmentArgs) =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.main)
                .setComponentName(MainActivity::class.java.cmp(context.packageName))
                .setDestination(R.id.flashFragment)
                .setArguments(args.toBundle())
                .createPendingIntent()

        private fun flashType(isSecondSlot: Boolean) =
            if (isSecondSlot) Const.Value.FLASH_INACTIVE_SLOT else Const.Value.FLASH_REGISK

        /* Flashing is understood as installing / flashing regisk itself */

        fun flash(isSecondSlot: Boolean) = MainDirections.actionFlashFragment(
            action = flashType(isSecondSlot)
        )

        /* Patching is understood as injecting img files with regisk */

        fun patch(uri: Uri) = MainDirections.actionFlashFragment(
            action = Const.Value.PATCH_FILE,
            additionalData = uri
        )

        /* Uninstalling is understood as removing regisk entirely */

        fun uninstall() = MainDirections.actionFlashFragment(
            action = Const.Value.UNINSTALL
        )

        /* Installing is understood as flashing modules / zips */

        fun installIntent(context: Context, file: Uri, id: Int = -1) = FlashFragmentArgs(
            action = Const.Value.FLASH_ZIP,
            additionalData = file,
            dismissId = id
        ).let { createIntent(context, it) }

        fun install(file: Uri, id: Int) = MainDirections.actionFlashFragment(
            action = Const.Value.FLASH_ZIP,
            additionalData = file,
            dismissId = id
        )
    }

}
