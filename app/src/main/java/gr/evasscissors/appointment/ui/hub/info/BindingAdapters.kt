package gr.evasscissors.appointment.ui.hub.info

import android.os.Build
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import gr.evasscissors.appointment.R
import gr.evasscissors.appointment.data.DayOfWeek
import gr.evasscissors.appointment.data.daysOfWeek
import gr.evasscissors.appointment.data.orderToDayOfWeek
import gr.evasscissors.appointment.model.ContactProviderInfo
import gr.evasscissors.appointment.model.helper.DayOpeningHours
import java.time.LocalDate
import java.util.*

@BindingAdapter("openingHours")
fun bindOpeningHours(recyclerView: RecyclerView, openingHours: List<DayOpeningHours>?) {
    val adapter = recyclerView.adapter as OpeningHoursAdapter

    val sortedList = openingHours?.sortedWith(compareBy { it.order })

    adapter.submitList(sortedList)
}

@BindingAdapter("day")
fun bindDate(materialTextView: MaterialTextView, day: Int?) {
    val dayOfWeek = orderToDayOfWeek[day]

    materialTextView.text = materialTextView.resources.getString(daysOfWeek[dayOfWeek]!!)
}

@BindingAdapter("hours")
fun bindHours(materialTextView: MaterialTextView, dayOpeningHours: DayOpeningHours?) {
    val resources = materialTextView.resources

    var morningHours: String? = null
    dayOpeningHours!!.morning?.let {
        morningHours = resources.getString(R.string.opening_hours, it.from, it.to)
    }

    var afternoonHours: String? = null
    dayOpeningHours.afternoon?.let {
        afternoonHours = "\n${resources.getString(R.string.opening_hours, it.from, it.to)}"
    }

    materialTextView.text = if (morningHours == null && afternoonHours == null) {
        resources.getString(R.string.store_closed)
    } else {
        "${morningHours ?: ""}${afternoonHours ?: ""}"
    }
}

@BindingAdapter("background")
fun bindBackground(materialCardView: MaterialCardView, order: Int?) {
    order?.let {
        val dayOfWeek = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().dayOfWeek.value
        } else {
            Calendar.getInstance(Locale.getDefault()).dayOfWeek()
        }

        if (dayOfWeek == it) {
            materialCardView.apply {
                setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.md_theme_dark_primaryContainer,
                        context.theme
                    )
                )
            }
        }
    }
}

@BindingAdapter("background")
fun bindBackground(constraintLayout: ConstraintLayout, contactProviderInfo: ContactProviderInfo?) {
    contactProviderInfo?.let {
        if (it.background == null) {
            constraintLayout.setBackgroundResource(it.color)
        } else {
            constraintLayout.background = it.background
        }
    }
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
