package com.example.movierr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movierr.databinding.ActivityReviewBinding
import com.google.firebase.database.*

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var database: DatabaseReference
    private lateinit var reviewList: MutableList<ReviewModel>
    private val CAMERA_REQ_CODE = 102
    private val SMS_REQ_CODE = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("reviews")
        reviewList = mutableListOf()

        setupRecyclerView()
        
        // Temporarily commented out until raw resources are added to prevent build failure
        // setupVideoPlayer()

        binding.btnSubmitReview.setOnClickListener { submitReview() }
        binding.btnCaptureImage.setOnClickListener { checkCameraPermission() }
        
        loadReviewsFromFirebase()
    }

    private fun setupVideoPlayer() {
        try {
            // Check if resources exist before trying to play
            val resId = resources.getIdentifier("sample_trailer", "raw", packageName)
            if (resId != 0) {
                val videoPath = "android.resource://" + packageName + "/" + resId
                binding.videoView.setVideoURI(Uri.parse(videoPath))
                val mediaController = MediaController(this)
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setMediaController(mediaController)
                binding.videoView.start()
            }
        } catch (e: Exception) {
            Log.e("ReviewActivity", "Video play error: ${e.message}")
        }
    }

    private fun submitReview() {
        val movie = binding.etMovieName.text.toString().trim()
        val reviewText = binding.etReviewText.text.toString().trim()
        val rating = binding.ratingBarReview.rating
        val phone = binding.etPhoneSms.text.toString().trim()

        if (movie.isEmpty() || reviewText.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val id = database.push().key ?: return
        val review = ReviewModel(id, movie, reviewText, rating, System.currentTimeMillis())

        database.child(id).setValue(review)
            .addOnSuccessListener {
                playSoundEffect()
                if (phone.isNotEmpty()) {
                    checkSmsPermission(phone)
                }
                Toast.makeText(this, "Review Submitted Successfully", Toast.LENGTH_SHORT).show()
                binding.etMovieName.text.clear()
                binding.etReviewText.text.clear()
                binding.etPhoneSms.text.clear()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Error: ${e.message}")
                Toast.makeText(this, "Firebase Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun playSoundEffect() {
        try {
            val resId = resources.getIdentifier("success_sound", "raw", packageName)
            if (resId != 0) {
                val mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { it.release() }
            }
        } catch (e: Exception) {
            Log.e("ReviewActivity", "Sound play error: ${e.message}")
        }
    }

    private fun checkSmsPermission(phone: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_REQ_CODE)
        } else {
            sendSms(phone)
        }
    }

    private fun sendSms(phone: String) {
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phone, null, "Your review has been submitted successfully!", null, null)
            Toast.makeText(this, "SMS Sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("SMS_ERROR", "Error: ${e.message}")
            Toast.makeText(this, "SMS Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQ_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.ivCapturedImage.setImageBitmap(imageBitmap)
            binding.ivCapturedImage.visibility = View.VISIBLE
        }
    }

    private fun loadReviewsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList.clear()
                for (postSnapshot in snapshot.children) {
                    val review = postSnapshot.getValue(ReviewModel::class.java)
                    review?.let { reviewList.add(it) }
                }
                binding.rvFirebaseReviews.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Load cancelled: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvFirebaseReviews.layoutManager = LinearLayoutManager(this)
        binding.rvFirebaseReviews.adapter = FirebaseReviewAdapter(reviewList)
    }
}
