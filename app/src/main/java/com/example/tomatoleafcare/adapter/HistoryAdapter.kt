package com.example.tomatoleafcare.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.model.History
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val context: Context,
    private var historyList: List<History>,
    private val onItemClick: (History) -> Unit,
    private val onDeleteClick: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        holder.bind(history)

        holder.itemView.setOnClickListener {
            onItemClick(history)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(history)
        }
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newList: List<History>) {
        historyList = newList
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diseaseName: TextView = itemView.findViewById(R.id.txtDiseaseName)
        private val date: TextView = itemView.findViewById(R.id.txtDate)
        private val image: ImageView = itemView.findViewById(R.id.imageHistory)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(history: History) {
            diseaseName.text = history.diseaseName
            date.text = history.date
            Picasso.get().load(history.imagePath)
                .placeholder(R.drawable.placeholder)
                .into(image)
        }
    }
}
