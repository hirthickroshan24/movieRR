package com.example.movierr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movierr.databinding.ActivityMovieDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var existingEntry: DiaryEntry? = null
    private val SMS_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarDetail.setNavigationOnClickListener { onBackPressed() }

        val movie = intent.getSerializableExtra("movie") as? Movie

        movie?.let { movieData ->
            binding.tvDetailTitle.text = movieData.title
            binding.tvDetailReleaseDate.text = "Release Date: ${movieData.releaseDate}"
            binding.tvDetailRating.text = "Rating: ${movieData.voteAverage}"
            binding.tvDetailDescription.text = movieData.overview

            Glide.with(this)
                .load(movieData.getFullPosterPath())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivFullPoster)

            lifecycleScope.launch {
                existingEntry = db.diaryDao().getEntryByTitle(movieData.title)
                existingEntry?.let {
                    binding.etReview.setText(it.review)
                    binding.ratingBar.rating = it.rating
                    binding.btnAddDiary.text = "Update Review"
                }
            }

            binding.btnAddDiary.setOnClickListener {
                saveReviewAndStartService(movieData.title)
            }

            binding.btnSendSms.setOnClickListener {
                val phoneNumber = binding.etPhoneNumber.text.toString()
                if (phoneNumber.isNotEmpty()) {
                    checkSmsPermissionAndSend(phoneNumber)
                } else {
                    Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkSmsPermissionAndSend(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        } else {
            val message = "Thank you for reviewing the movie on Movie Review Diary! Your review has been successfully submitted."
            sendReviewSms(phoneNumber, message)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
                val phoneNumber = binding.etPhoneNumber.text.toString()
                if (phoneNumber.isNotEmpty()) {
                    val message = "Thank you for reviewing the movie on Movie Review Diary! Your review has been successfully submitted."
                    sendReviewSms(phoneNumber, message)
                }
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendReviewSms(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback: Open SMS app if direct send fails
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
                intent.putExtra("sms_body", message)
                startActivity(intent)
                Toast.makeText(this, "Opening SMS app...", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveReviewAndStartService(movieName: String) {
        val rating = binding.ratingBar.rating
        val review = binding.etReview.text.toString()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (review.isEmpty()) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceIntent = Intent(this, ReviewProcessingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        lifecycleScope.launch {
            val entry = DiaryEntry(
                id = existingEntry?.id ?: 0,
                movieTitle = movieName,
                rating = rating,
                review = review,
                date = currentDate
            )
            db.diaryDao().insertEntry(entry)
            val msg = if (existingEntry == null) "Review Submission Started" else "Update Started"
            Toast.makeText(this@MovieDetailActivity, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
