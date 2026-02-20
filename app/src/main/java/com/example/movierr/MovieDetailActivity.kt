package com.example.movierr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movierr.databinding.ActivityMovieDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private val CHANNEL_ID = "movie_notifications"
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movie = intent.getSerializableExtra("movie") as? Movie

        movie?.let {
            binding.tvDetailTitle.text = it.title
            binding.tvDetailReleaseDate.text = "Release Date: ${it.releaseDate}"
            binding.tvDetailRating.text = "Rating: ${it.voteAverage}"
            binding.tvDetailDescription.text = it.overview

            Glide.with(this)
                .load(it.getFullPosterPath())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivFullPoster)

            binding.btnAddDiary.setOnClickListener { _ ->
                saveReview(it.title)
            }
        }
        
        createNotificationChannel()
    }

    private fun saveReview(movieName: String) {
        val rating = binding.ratingBar.rating
        val review = binding.etReview.text.toString()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (review.isEmpty()) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val entry = DiaryEntry(
                movieTitle = movieName,
                rating = rating,
                review = review,
                date = currentDate
            )
            db.diaryDao().insertEntry(entry)
            
            Toast.makeText(this@MovieDetailActivity, "Movie Added to Diary", Toast.LENGTH_SHORT).show()
            sendStatusNotification(movieName)
            // We don't need finish() here, so user is returned to HomeActivity
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Movie Review Channel"
            val descriptionText = "Channel for movie diary status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendStatusNotification(movieName: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Movie Added")
            .setContentText("$movieName added to your diary")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
