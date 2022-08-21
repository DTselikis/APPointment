package com.homelab.appointment.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object ContactProvider {
    fun callBusiness(context: Context, phone: String) {
        val phoneUri = Uri.parse("tel:$phone")
        val phoneIntent = Intent(Intent.ACTION_DIAL, phoneUri)
        phoneIntent.resolveActivity(context.packageManager)?.let {
            context.startActivity(phoneIntent)
        }
    }
}