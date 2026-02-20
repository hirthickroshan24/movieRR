package com.example.movierr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movierr.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAbout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAbout.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
