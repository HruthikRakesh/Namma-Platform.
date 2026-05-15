package com.example.nammaplatform.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nammaplatform.R

class CoachAdapter(
    private val coaches: List<String>
) : RecyclerView.Adapter<CoachAdapter.CoachViewHolder>() {

    inner class CoachViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView.findViewById(R.id.coachRoot)
        val tvEmoji: TextView = itemView.findViewById(R.id.tvCoachEmoji)
        val tvLabel: TextView = itemView.findViewById(R.id.tvCoachLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoachViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coach, parent, false)
        return CoachViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoachViewHolder, position: Int) {
        val coach = coaches[position]
        val ctx = holder.itemView.context

        val (emoji, label, colorRes) = when {
            coach == "Engine" -> Triple("🚂", "Engine\nಇಂಜಿನ್", R.color.coach_engine)
            coach == "GEN" -> Triple("🟢", "General\nಜನರಲ್", R.color.coach_general)
            coach == "Ladies" -> Triple("🟣", "Ladies\nಮಹಿಳೆ", R.color.coach_ladies)
            coach == "Pantry" -> Triple("🍽️", "Pantry\nಪ್ಯಾಂಟ್ರಿ", R.color.coach_pantry)
            coach.startsWith("S") -> Triple("🟠", coach + "\nSlpr", R.color.coach_sleeper)
            coach.startsWith("B") -> Triple("🔵", coach + "\n3AC", R.color.coach_ac)
            coach.startsWith("A") -> Triple("❄️", coach + "\n2AC", R.color.coach_ac)
            coach.startsWith("H") -> Triple("💎", coach + "\n1AC", R.color.coach_ac)
            else -> Triple("🚃", coach, R.color.coach_general)
        }

        holder.tvEmoji.text = emoji
        holder.tvLabel.text = label

        // Set rounded background with coach color
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.RECTANGLE
        bg.cornerRadius = 10f * ctx.resources.displayMetrics.density
        bg.setColor(ContextCompat.getColor(ctx, colorRes))
        holder.root.background = bg
    }

    override fun getItemCount() = coaches.size
}
