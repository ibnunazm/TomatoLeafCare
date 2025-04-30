package com.example.tomatoleafcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.model.Disease

class DiseaseAdapter(
    private val diseases: List<Disease>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(disease: Disease)
    }

    inner class DiseaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diseaseName: TextView = itemView.findViewById(R.id.diseaseName)
        private val diseaseImage: ImageView = itemView.findViewById(R.id.diseaseImage)
        private val diseaseDescription: TextView = itemView.findViewById((R.id.diseaseDescription))

        fun bind(disease: Disease) {
            diseaseName.text = disease.name
            diseaseImage.setImageResource(disease.imageResId)
            diseaseDescription.text = disease.description

            itemView.setOnClickListener {
                listener.onItemClick(disease)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.disease_item, parent, false)
        return DiseaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.bind(diseases[position])
    }

    override fun getItemCount(): Int = diseases.size
}
