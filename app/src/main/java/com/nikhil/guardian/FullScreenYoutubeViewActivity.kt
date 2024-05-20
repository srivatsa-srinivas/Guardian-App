// FullScreenYoutubeViewActivity.kt
package com.nikhil.guardian

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.nikhil.guardian.databinding.ActivityFullScreenYoutubeViewBinding

class FullScreenYoutubeViewActivity : AppCompatActivity() {

    private val videoIds = listOf(
        "g6D2rzuzdZs",
        "qMXsKmZ1IIE",
        "snVcpmTDl08",
        "N1lUUj1WzDo",
        "JWHn5KqmhrQ"
    )

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: YouTubePlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityFullScreenYoutubeViewBinding =
            ActivityFullScreenYoutubeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cardView: CardView = binding.cardView
        recyclerView = binding.recyclerView

        val slideInAnimation = ObjectAnimator.ofFloat(cardView, "translationY", 800f, 0f)
        slideInAnimation.duration = 1000
        slideInAnimation.interpolator = AccelerateInterpolator()
        slideInAnimation.start()

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = YouTubePlayerAdapter(videoIds)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
