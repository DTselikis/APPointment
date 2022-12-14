package gr.evasscissors.appointment.ui.hub

import androidx.databinding.BindingAdapter
import coil.load
import coil.request.CachePolicy
import gr.evasscissors.appointment.R
import gr.evasscissors.appointment.data.Gender
import de.hdodenhof.circleimageview.CircleImageView

@BindingAdapter("imgUrl")
fun bindProfilePic(circleImageView: CircleImageView, user: Pair<String?, String?>?) {
    user?.let {
        val placeholder = when (it.second) {
            Gender.FEMALE.code -> R.drawable.female_placeholder_wo_bg
            Gender.MALE.code -> R.drawable.male_placeholder_wo_bg
            else -> R.drawable.any_placeholder_wo_bg
        }

        circleImageView.load(user.first) {
            placeholder(placeholder)
            error(placeholder)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    }
}