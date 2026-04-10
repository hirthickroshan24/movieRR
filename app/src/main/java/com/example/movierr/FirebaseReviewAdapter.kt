package com.example.movierr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movierr.databinding.ItemFirebaseReviewBinding

class FirebaseReviewAdapter(private val reviews: List<ReviewModel>) : 
    RecyclerView.Adapter<FirebaseReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ItemFirebaseReviewBinding) : 
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemFirebaseReviewBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.tvMovieNameItem.text = review.movieName
        holder.binding.tvReviewTextItem.text = review.reviewText
        holder.binding.rbItemRating.rating = review.rating
    }

    override fun getItemCount() = reviews.size
}
