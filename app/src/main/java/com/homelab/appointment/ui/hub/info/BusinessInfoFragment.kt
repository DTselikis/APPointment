package com.homelab.appointment.ui.hub.info

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

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            openingHoursRv.adapter = OpeningHoursAdapter()
            viewModel = this@InfoFragment.viewModel
        }

        observeInfoFetched()
        viewModel.fetchBusinessInfo()
    }

    private fun observeInfoFetched() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.infoFetched.collectLatest { fetched ->
                if (fetched) {
                    scrollToCurrentDate()
                }
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