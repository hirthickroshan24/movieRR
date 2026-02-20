package com.example.movierr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movierr.databinding.ActivityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val apiService = MovieApiService.create()
    
    // Using the provided TMDB API Key
    private val API_KEY = "34d518706297f7ced3a25969c4a2e1c6" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbar)

        binding.rvMovies.layoutManager = LinearLayoutManager(this)
        
        fetchMovies()
    }

    private fun fetchMovies() {
        binding.progressBar.visibility = View.VISIBLE
        
        apiService.getPopularMovies(API_KEY).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    displayMovies(movies)
                } else {
                    Toast.makeText(this@HomeActivity, "API Error", Toast.LENGTH_SHORT).show()
                    showDummyData()
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@HomeActivity, "Network Failure", Toast.LENGTH_SHORT).show()
                showDummyData()
            }
        })
    }

    private fun displayMovies(movies: List<Movie>) {
        binding.rvMovies.adapter = MovieAdapter(movies) { movie ->
            val intent = Intent(this@HomeActivity, MovieDetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
    }

    private fun showDummyData() {
        val dummyMovies = listOf(
            Movie(1, "Sample Movie 1", "Dummy description.", null, "2024-01-01", 8.5),
            Movie(2, "Sample Movie 2", "API call failed.", null, "2024-02-01", 7.0)
        )
        displayMovies(dummyMovies)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_diary -> {
                startActivity(Intent(this, DiaryActivity::class.java))
                return true
            }
            R.id.menu_location -> {
                startActivity(Intent(this, LocationActivity::class.java))
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
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}
