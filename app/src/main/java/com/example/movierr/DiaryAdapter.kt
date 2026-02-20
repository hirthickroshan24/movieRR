package com.example.movierr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movierr.databinding.ItemDiaryBinding

class DiaryAdapter(
    private var entries: List<DiaryEntry>,
    private val onEdit: (DiaryEntry) -> Unit,
    private val onDelete: (DiaryEntry) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    inner class DiaryViewHolder(private val binding: ItemDiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DiaryEntry) {
            binding.tvDiaryMovieTitle.text = entry.movieTitle
            binding.tvDiaryDate.text = "Date: ${entry.date}"
            binding.diaryRatingBar.rating = entry.rating
            binding.tvDiaryReview.text = entry.review

            binding.btnEditDiary.setOnClickListener { onEdit(entry) }
            binding.btnDeleteDiary.setOnClickListener { onDelete(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = ItemDiaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    fun updateData(newEntries: List<DiaryEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
