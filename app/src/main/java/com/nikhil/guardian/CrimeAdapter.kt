@file:Suppress("CanBeParameter")

package com.nikhil.guardian

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import jp.wasabeef.recyclerview.animators.holder.AnimateViewHolder


class CrimeAdapter(
    private val context: Context,
    private val crimeList: ArrayList<Crime>,
    private val itemClickListener: ItemClickListener,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<CrimeAdapter.ViewHolder>(), AnimateViewHolder {

    private val filteredList = ArrayList<Crime>()

    init {
        filteredList.addAll(crimeList)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val crimeName: TextView = itemView.findViewById(R.id.crimeName)
        val crimeDescription: TextView = itemView.findViewById(R.id.crimeDescription)
        val crimeImage: ImageView = itemView.findViewById(R.id.crimeImage)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedCrime = filteredList[position]
                    itemClickListener.onCrimeClicked(selectedCrime.name, selectedCrime.description)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.crime_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crime = filteredList[position]
        holder.crimeName.text = crime.name
        holder.crimeDescription.text = crime.description

        // Use Glide for image loading
        Glide.with(context)
            .load(crime.imageResourceId)
            .transform(CircleCrop())
            .into(holder.crimeImage)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun animateAddImpl(
        holder: RecyclerView.ViewHolder,
        listener: Animator.AnimatorListener
    ) {
        TODO("Not yet implemented")
    }


    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder, listener: Animator.AnimatorListener) {
        // Define any remove animation logic if needed
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder) {
        // Set the initial state of the view before animation
        holder.itemView.translationX = -recyclerView.width.toFloat()
        holder.itemView.alpha = 0f
    }

    override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder) {
        // Define any pre-remove animation setup if needed
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Crime>) {
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onCrimeClicked(crimeName: String, crimeDescription: String)
    }
}