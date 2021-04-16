package com.app.userlocation

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.util.*

class GetAddressIntentService : IntentService(IDENTIFIER) {
    private var addressResultReceiver: ResultReceiver? = null
    override fun onHandleIntent(intent: Intent?) {
        val msg: String
        addressResultReceiver = Objects.requireNonNull(intent)!!.getParcelableExtra("add_receiver")
        if (addressResultReceiver == null) {
            Log.e("GetAddressIntentService", "No receiver, not processing the request further")
            return
        }
        val location = intent!!.getParcelableExtra<Location>("add_location")
        if (location == null) {
            msg = "No location, can't go further without location"
            sendResultsToReceiver(0, msg)
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: kotlin.collections.List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: Exception) {
            Log.e("", "Error in getting address for the location")
        }
        if (addresses == null || addresses.size == 0) {
            msg = "No address found for the location"
            sendResultsToReceiver(1, msg)
        } else {
            val address = addresses[0]
            val addressDetails = """
                ${address.featureName}
                ${address.thoroughfare}
                Locality: ${address.locality}
                County: ${address.subAdminArea}
                State: ${address.adminArea}
                Country: ${address.countryName}
                Postal Code: ${address.postalCode}
                
                """.trimIndent()
            sendResultsToReceiver(2, addressDetails)
        }
    }

    private fun sendResultsToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString("address_result", message)
        addressResultReceiver!!.send(resultCode, bundle)
    }

    companion object {
        private const val IDENTIFIER = "GetAddressIntentService"
    }
}