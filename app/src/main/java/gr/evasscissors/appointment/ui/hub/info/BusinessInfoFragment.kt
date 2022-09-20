package gr.evasscissors.appointment.ui.hub.info

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import gr.evasscissors.appointment.R
import gr.evasscissors.appointment.data.DayOfWeek
import gr.evasscissors.appointment.databinding.FragmentBusinessInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

        viewModel.fetchBusinessInfo()
        observeInfoFetched()
        observeTextToCopy()
    }

    private fun observeInfoFetched() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.infoFetched.collectLatest { fetched ->
                if (fetched) {
                    contractProvidersAdapter.submitList(viewModel.getProvidersList(requireContext()))
                    lifecycleScope.launchWhenStarted {
                        launch(Dispatchers.Default) {
                            delay(150)
                            scrollToCurrentDate()
                        }
                    }
                }
            }
        }
    }

    private fun observeTextToCopy() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.textToCopy.collectLatest { text ->
                val clipboard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("social info", text))

                Toast.makeText(
                    requireContext(),
                    getString(R.string.copied_to_clipboard),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun scrollToCurrentDate() {
        val dayOfWeek = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().dayOfWeek.value
        } else {
            Calendar.getInstance(Locale.getDefault()).dayOfWeek()
        }

        binding.openingHoursRv.smoothScrollToPosition(dayOfWeek)
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