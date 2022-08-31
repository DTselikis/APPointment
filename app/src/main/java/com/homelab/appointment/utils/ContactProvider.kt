package com.homelab.appointment.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.homelab.appointment.data.FB_PACKAGE_NAME

object ContactProvider {
    fun callBusiness(context: Context, phone: String) {
        val phoneUri = Uri.parse("tel:$phone")
        val phoneIntent = Intent(Intent.ACTION_DIAL, phoneUri)
        phoneIntent.resolveActivity(context.packageManager)?.let {
            context.startActivity(phoneIntent)
        }
    }

    fun navigateToBusiness(context: Context, mapsUri: String) {
        val gmmIntentUri =
            Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=$mapsUri"
            )

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.resolveActivity(context.packageManager)?.let {
            context.startActivity(mapIntent)
        }
    }

    fun openFacebookPage(context: Context, pageId: String) {
        val packageManager = context.packageManager

        // TODO fb lite
        val fbUri = try {
            packageManager.getPackageInfo(FB_PACKAGE_NAME, 0)
            "fb://page/$pageId"
        } catch (e: PackageManager.NameNotFoundException) {
            "https://www.facebook.com/$pageId"
        }

        val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fbUri))
        fbIntent.resolveActivity(packageManager)?.let {
            context.startActivity(fbIntent)
        }
    }

    fun openInstagramPage(context: Context, instaProfile: String) {
        val packageManager = context.packageManager

        val instaUri = try {
            packageManager.getPackageInfo("com.instagram.android", 0)
            "http://instagram.com/_u/$instaProfile"
        } catch (e: PackageManager.NameNotFoundException) {
            "https://www.instagram.com/$instaProfile"
        }

        val instaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(instaUri))
        instaIntent.resolveActivity(packageManager)?.let {
            context.startActivity(instaIntent)
        }
    }
}