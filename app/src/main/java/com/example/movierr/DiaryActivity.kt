package com.example.movierr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movierr.databinding.ActivityDiaryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryBinding
    private lateinit var adapter: DiaryAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "My Movie Diary"

        setupRecyclerView()
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
