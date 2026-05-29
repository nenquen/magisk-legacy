package com.regisk.legacy.ui.superuser

import android.os.Bundle
import android.view.View
import com.regisk.legacy.R
import com.regisk.legacy.arch.BaseUIFragment
import com.regisk.legacy.databinding.FragmentSuperuserMd2Binding
import com.regisk.legacy.di.viewModel
import com.regisk.legacy.ktx.addSimpleItemDecoration
import com.regisk.legacy.ktx.addVerticalPadding
import com.regisk.legacy.ktx.fixEdgeEffect

class SuperuserFragment : BaseUIFragment<SuperuserViewModel, FragmentSuperuserMd2Binding>() {

    override val layoutRes = R.layout.fragment_superuser_md2
    override val viewModel by viewModel<SuperuserViewModel>()

    override fun onStart() {
        super.onStart()
        activity.title = resources.getString(R.string.superuser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resource = requireContext().resources
        val l_50 = resource.getDimensionPixelSize(R.dimen.l_50)
        val l1 = resource.getDimensionPixelSize(R.dimen.l1)
        binding.superuserList.addVerticalPadding(
            l_50,
            l1
        )
        binding.superuserList.addSimpleItemDecoration(
            left = l1,
            top = l_50,
            right = l1,
            bottom = l_50,
        )
        binding.superuserList.fixEdgeEffect()
    }

    override fun onPreBind(binding: FragmentSuperuserMd2Binding) {}

}
