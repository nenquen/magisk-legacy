package com.regisk.legacy.ui.home

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseUIFragment
import com.regisk.legacy.core.download.BaseDownloader
import com.regisk.legacy.databinding.FragmentHomeMd2Binding
import com.regisk.legacy.di.viewModel
import com.regisk.legacy.events.RebootEvent
import com.topjohnwu.superuser.Shell

class HomeFragment : BaseUIFragment<HomeViewModel, FragmentHomeMd2Binding>() {

    override val layoutRes = R.layout.fragment_home_md2
    override val viewModel by viewModel<HomeViewModel>()

    override fun onStart() {
        super.onStart()
        activity.title = resources.getString(R.string.section_home)
        setHasOptionsMenu(true)
        BaseDownloader.observeProgress(this, viewModel::onProgressUpdate)
    }

    private fun checkTitle(text: TextView, icon: ImageView) {
        text.post {
            if (text.layout.getEllipsisCount(0) != 0) {
                with (icon) {
                    layoutParams.width = 0
                    layoutParams.height = 0
                    requestLayout()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // If titles are squished, hide icons
        with(binding.homeRegiskWrapper) {
            checkTitle(homeRegiskTitle, homeRegiskIcon)
        }
        with(binding.homeManagerWrapper) {
            checkTitle(homeManagerTitle, homeManagerIcon)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home_md2, menu)
        if (!Shell.rootAccess())
            menu.removeItem(R.id.action_reboot)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings ->
                HomeFragmentDirections.actionHomeFragmentToSettingsFragment().navigate()
            R.id.action_reboot -> RebootEvent.inflateMenu(activity).show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

}
