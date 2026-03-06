package com.example.movierr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.movierr.databinding.ActivityMovieLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*

class MovieLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQ_CODE = 200
    
    // Variables to store detected location
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbarLocation)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarLocation.setNavigationOnClickListener { onBackPressed() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnDetectLocation.setOnClickListener {
            if (isLocationEnabled()) {
                requestLocationPermission()
            } else {
                showGPSDisabledDialog()
            }
        }

        binding.btnFindTheatres.setOnClickListener {
            if (currentLat != null && currentLon != null) {
                // Open Google Maps to find nearby theatres using the specific detected coordinates
                val uriString = "geo:${currentLat},${currentLon}?q=movie+theatres"
                val gmmIntentUri = Uri.parse(uriString)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    // Fallback to browser if Google Maps app is not available
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/movie+theatres/@${currentLat},${currentLon},15z"))
                    startActivity(browserIntent)
                }
            } else {
                Toast.makeText(this, "Please detect your location first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGPSDisabledDialog() {
        AlertDialog.Builder(this)
            .setTitle("GPS Disabled")
            .setMessage("Please enable GPS to detect your location.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocation() {
        binding.pbLocation.visibility = View.VISIBLE
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                binding.pbLocation.visibility = View.GONE
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    
                    binding.tvLatitude.text = "Latitude: $currentLat"
                    binding.tvLongitude.text = "Longitude: $currentLon"
                    
                    updateUIWithAddress(currentLat!!, currentLon!!)
                    binding.btnFindTheatres.isEnabled = true
                } else {
                    Toast.makeText(this, "Could not fetch location. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                binding.pbLocation.visibility = View.GONE
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUIWithAddress(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                binding.tvFullAddress.text = "Full Address: ${address.getAddressLine(0)}"
                binding.tvCity.text = "City: ${address.locality ?: "N/A"}"
                binding.tvPostalCode.text = "Postal Code: ${address.postalCode ?: "N/A"}"
                binding.tvCountry.text = "Country: ${address.countryName ?: "N/A"}"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Geocoder error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
