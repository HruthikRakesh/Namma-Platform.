package com.example.nammaplatform.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nammaplatform.R
import com.example.nammaplatform.model.Train

class TrainAdapter(
    private val trains: List<Train>,
    private val onTrainClick: (Train, Int) -> Unit
) : RecyclerView.Adapter<TrainAdapter.TrainViewHolder>() {

    private var selectedPosition = -1

    inner class TrainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView.findViewById(R.id.trainCardRoot)
        val tvPlatform: TextView = itemView.findViewById(R.id.tvPlatformNumber)
        val tvName: TextView = itemView.findViewById(R.id.tvTrainName)
        val tvNameKn: TextView = itemView.findViewById(R.id.tvTrainNameKn)
        val tvTime: TextView = itemView.findViewById(R.id.tvArrivalTime)
        val tvDest: TextView = itemView.findViewById(R.id.tvDestination)
        val tvNumber: TextView = itemView.findViewById(R.id.tvTrainNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val train = trains[position]
        holder.tvPlatform.text = train.platform.toString()
        holder.tvName.text = train.train_name
        holder.tvNameKn.text = train.train_name_kn
        holder.tvTime.text = train.arrival_time
        holder.tvDest.text = "${train.destination} / ${train.destination_kn}"
        holder.tvNumber.text = "#${train.train_number}"

        // Highlight selected card
        if (selectedPosition == position) {
            holder.root.setBackgroundResource(R.drawable.bg_card_selected)
        } else {
            holder.root.setBackgroundResource(R.drawable.bg_card)
        }

        holder.root.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onTrainClick(train, selectedPosition)
        }
    }

    override fun getItemCount() = trains.size
}
