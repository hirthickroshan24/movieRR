package com.example.movierr

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.movierr.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom toolbar
        setSupportActionBar(binding.toolbarDashboard)
        supportActionBar?.title = "Home Dashboard"

        binding.btnExploreMovies.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.btnMyDiary.setOnClickListener {
            startActivity(Intent(this, DiaryActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                // Already on Dashboard
                return true
            }
            R.id.menu_diary -> {
                startActivity(Intent(this, DiaryActivity::class.java))
                return true
            }
            R.id.menu_location -> {
                startActivity(Intent(this, MovieLocationActivity::class.java))
                return true
            }
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.menu_team -> {
                startActivity(Intent(this, TeamActivity::class.java))
                return true
            }
            R.id.menu_project -> {
                startActivity(Intent(this, ProjectDescriptionActivity::class.java))
                return true
            }
            R.id.menu_logout -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
