// YouTubePlayerAdapter.kt
@file:Suppress("CanBeParameter")

package com.nikhil.guardian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.guardian.databinding.YoutubePlayerViewItemBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class YouTubePlayerAdapter(private val videoIds: List<String>) :
    RecyclerView.Adapter<YouTubePlayerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = YoutubePlayerViewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoId = videoIds[position]
        holder.bind(videoId)
    }

    override fun getItemCount(): Int = videoIds.size

    class ViewHolder(private val binding: YoutubePlayerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val youTubePlayerView = binding.youtubePlayerView
        private var hasPlayed = false
        private var videoId: String? = null

        fun bind(videoId: String) {
            // Save the videoId
            this.videoId = videoId

            youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    // Load the video without autoplay
                    if (videoId == this@ViewHolder.videoId && !hasPlayed) {
                        youTubePlayer.cueVideo(videoId, 0f)
                        hasPlayed = true
                    }
                }
            })
        }
    }
}
