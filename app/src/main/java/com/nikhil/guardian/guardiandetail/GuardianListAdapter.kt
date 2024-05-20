package com.nikhil.guardian.guardiandetail
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.guardian.R
import com.nikhil.guardian.database.Guardian
import kotlinx.android.synthetic.main.list_item_guardian.view.textName
import kotlinx.android.synthetic.main.list_item_guardian.view.textPhone

class GuardianListAdapter(
    private val guardians: List<Guardian>,
    private val onItemClick: (Guardian) -> Unit
) : RecyclerView.Adapter<GuardianListAdapter.ViewHolder>() {

    private val defaultGuardian: Guardian = Guardian(999, "Emergency", "Emergency", "911")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_guardian, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guardian = if (position == 0) defaultGuardian else guardians[position - 1]
        holder.bind(guardian)
        holder.itemView.setOnClickListener { onItemClick(guardian) }
    }

    override fun getItemCount(): Int {
        // Add 1 for the default 911 entry
        return guardians.size + 1
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(guardian: Guardian) {
            itemView.textName.text = guardian.guardianName
            itemView.textPhone.text = guardian.guardianPhoneNo
        }
    }
}