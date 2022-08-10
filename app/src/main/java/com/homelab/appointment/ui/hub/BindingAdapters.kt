package com.homelab.appointment.ui.hub

import androidx.databinding.BindingAdapter
import coil.load
import coil.request.CachePolicy
import com.homelab.appointment.R
import com.homelab.appointment.data.Gender
import com.homelab.appointment.model.User
import de.hdodenhof.circleimageview.CircleImageView

@BindingAdapter("imgUrl")
fun bindProfilePic(circleImageView: CircleImageView, user: User?) {
    user?.let {
        val placeholder = when (it.gender) {
            Gender.FEMALE.code -> R.drawable.female_placeholder_wo_bg
            Gender.MALE.code -> R.drawable.male_placeholder_wo_bg
            else -> R.drawable.any_placeholder_wo_bg
        }

        circleImageView.load(user.profilePic) {
            placeholder(placeholder)
            error(placeholder)
            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    }
}