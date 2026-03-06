package com.example.movierr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movierr.databinding.ActivityDiaryBinding
import kotlinx.coroutines.launch

class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryBinding
    private lateinit var adapter: DiaryAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom toolbar
        setSupportActionBar(binding.toolbarDiary)
        supportActionBar?.title = "My Movie Diary"

        setupRecyclerView()

        // Sync button logic to start Foreground Service
        binding.btnSync.setOnClickListener {
            if (checkNotificationPermission()) {
                startSyncService()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDiaryEntries()
    }

    private fun setupRecyclerView() {
        adapter = DiaryAdapter(emptyList(),
            onEdit = { entry -> showEditDialog(entry) },
            onDelete = { entry -> deleteEntry(entry) }
        )
        binding.rvDiary.layoutManager = LinearLayoutManager(this)
        binding.rvDiary.adapter = adapter
    }

    private fun loadDiaryEntries() {
        lifecycleScope.launch {
            val entries = db.diaryDao().getAllEntries()
            adapter.updateData(entries)
            binding.tvEmptyDiary.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun startSyncService() {
        val intent = Intent(this, ReviewSyncService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Sync Started...", Toast.LENGTH_SHORT).show()
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return false
            }
        }
        return true
    }

    // Handle Menu in the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                return true
            }
            R.id.menu_diary -> return true
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
                finishAffinity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showEditDialog(entry: DiaryEntry) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_diary, null)
        val etReview = dialogView.findViewById<EditText>(R.id.etEditReview)
        val rbRating = dialogView.findViewById<RatingBar>(R.id.rbEditRating)
        val etDate = dialogView.findViewById<EditText>(R.id.etEditDate)

        etReview.setText(entry.review)
        rbRating.rating = entry.rating
        etDate.setText(entry.date)

        AlertDialog.Builder(this)
            .setTitle("Edit Entry")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedEntry = entry.copy(
                    review = etReview.text.toString(),
                    rating = rbRating.rating,
                    date = etDate.text.toString()
                )
                updateEntry(updatedEntry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEntry(entry: DiaryEntry) {
        lifecycleScope.launch {
            db.diaryDao().updateEntry(entry)
            Toast.makeText(this@DiaryActivity, "Entry updated", Toast.LENGTH_SHORT).show()
            loadDiaryEntries()
        }
    }

    private fun deleteEntry(entry: DiaryEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this log?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    db.diaryDao().deleteEntry(entry)
                    Toast.makeText(this@DiaryActivity, "Entry deleted", Toast.LENGTH_SHORT).show()
                    loadDiaryEntries()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
