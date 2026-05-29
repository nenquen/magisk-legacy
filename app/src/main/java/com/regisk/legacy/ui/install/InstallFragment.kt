package com.regisk.legacy.ui.install

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseUIFragment
import com.regisk.legacy.databinding.FragmentInstallMd2Binding
import com.regisk.legacy.di.viewModel
import com.regisk.legacy.ktx.coroutineScope

class InstallFragment : BaseUIFragment<InstallViewModel, FragmentInstallMd2Binding>() {

    override val layoutRes = R.layout.fragment_install_md2
    override val viewModel by viewModel<InstallViewModel>()

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(R.string.install)

        // Allow markwon to run in viewmodel scope
        binding.releaseNotes.coroutineScope = viewModel.viewModelScope
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel._method = savedInstanceState?.getInt(KEY_CURRENT_METHOD, -1) ?: -1
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_CURRENT_METHOD, viewModel.method)
    }

    companion object {
        private const val KEY_CURRENT_METHOD = "current_method"
    }
}
