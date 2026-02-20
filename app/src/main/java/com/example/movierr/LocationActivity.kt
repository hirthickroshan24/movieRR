package com.example.movierr

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.movierr.databinding.ActivityLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val CHANNEL_ID = "location_notifications"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLocation)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLocation.setNavigationOnClickListener { onBackPressed() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

        binding.btnFindTheatres.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=movie theatres near me")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Google Maps not found", Toast.LENGTH_SHORT).show()
            }
        }
        
        createNotificationChannel()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLocation() {
        binding.progressBarLocation.visibility = View.VISIBLE
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                binding.progressBarLocation.visibility = View.GONE
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    
                    binding.tvLat.text = "Latitude: $lat"
                    binding.tvLon.text = "Longitude: $lon"
                    
                    getCityName(lat, lon)
                    
                    Toast.makeText(this, "Location Fetched Successfully", Toast.LENGTH_SHORT).show()
                    sendLocationNotification()
                } else {
                    Toast.makeText(this, "Could not fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            binding.progressBarLocation.visibility = View.GONE
            Toast.makeText(this, "Permission Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCityName(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val cityName = addresses[0].locality ?: "Unknown City"
                binding.tvCity.text = "City: $cityName"
            } else {
                binding.tvCity.text = "City: Not Found"
            }
        } catch (e: Exception) {
            binding.tvCity.text = "City: Error fetching"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Location Status Channel"
            val descriptionText = "Channel for location update status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendLocationNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Location Updated")
            .setContentText("Your current location detected successfully")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, builder.build())
    }
}
