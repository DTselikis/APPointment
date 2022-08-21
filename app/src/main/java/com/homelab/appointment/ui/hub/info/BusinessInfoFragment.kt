package com.homelab.appointment.ui.hub.info

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.homelab.appointment.R
import com.homelab.appointment.data.DayOfWeek
import com.homelab.appointment.databinding.FragmentBusinessInfoBinding
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.util.*

class InfoFragment : Fragment() {

    private val viewModel: BusinessInfoViewModel by viewModels()

    private lateinit var binding: FragmentBusinessInfoBinding

    private lateinit var contractProvidersAdapter: ContactProvidersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_business_info, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contractProvidersAdapter = ContactProvidersAdapter()
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            openingHoursRv.adapter = OpeningHoursAdapter()
            contactInfoRv.adapter = contractProvidersAdapter
            viewModel = this@InfoFragment.viewModel
        }

        observeInfoFetched()
        viewModel.fetchBusinessInfo()
        observeExtAppNotFound()
    }

    private fun observeInfoFetched() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.infoFetched.collectLatest { fetched ->
                if (fetched) {
                    scrollToCurrentDate()
                    contractProvidersAdapter.submitList(viewModel.getProvidersList(requireContext()))
                }
            }
        }
    }

    private fun observeExtAppNotFound() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.appName.collectLatest { appName ->
                showWarningDialog(appName)
            }
        }
    }

    private fun scrollToCurrentDate() {
        val dayOfWeek = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().dayOfWeek.value
        } else {
            Calendar.getInstance(Locale.getDefault()).dayOfWeek()
        }

        binding.openingHoursRv.smoothScrollToPosition(dayOfWeek - 1)
    }

    private fun showWarningDialog(appName: String) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }
        builder?.setMessage(getString(R.string.opening_ext_app_problem, appName))
            ?.setPositiveButton(getString(R.string.dismiss_dialog)) { dialog, id ->
                dialog.dismiss()
            }
            ?.create()?.show()
    }

    private fun Calendar.dayOfWeek(): Int = when (this.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> DayOfWeek.MONDAY.code
        Calendar.TUESDAY -> DayOfWeek.TUESDAY.code
        Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY.code
        Calendar.THURSDAY -> DayOfWeek.THURSDAY.code
        Calendar.FRIDAY -> DayOfWeek.FRIDAY.code
        Calendar.SATURDAY -> DayOfWeek.SATURDAY.code
        else -> DayOfWeek.SUNDAY.code
    }
}