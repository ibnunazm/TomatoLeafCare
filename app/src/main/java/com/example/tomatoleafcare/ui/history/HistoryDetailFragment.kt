package com.example.tomatoleafcare.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.repository.DiseaseRepository
import com.example.tomatoleafcare.model.History
import com.squareup.picasso.Picasso

class HistoryDetailFragment : Fragment() {

    private var history: History? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.history_detail_fragment, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        history = arguments?.getSerializable("history") as? History

        history?.let {
            showDetails(view, it)
        }

        return view
    }

    private fun showDetails(view: View, history: History) {
        val detailDiseaseName = view.findViewById<TextView>(R.id.detailDiseaseName)
        val detailDiseaseCauses = view.findViewById<TextView>(R.id.detailDiseaseCauses)
        val detailDiseaseSymptoms = view.findViewById<TextView>(R.id.detailDiseaseSymptoms)
        val detailDiseaseImpact = view.findViewById<TextView>(R.id.detailDiseaseImpact)
        val detailDiseaseSolution = view.findViewById<TextView>(R.id.detailDiseaseSolution)
        val detailDiseaseImage = view.findViewById<ImageView>(R.id.detailDiseaseImage)

        Picasso.get().load(history.imagePath).into(detailDiseaseImage)
        detailDiseaseName.text = history.diseaseName

        val matchedDisease = DiseaseRepository.findDiseaseByName(history.diseaseName)

        matchedDisease?.let {
            detailDiseaseCauses.text = it.cause
            detailDiseaseSymptoms.text = it.symptoms
            detailDiseaseImpact.text = it.impact
            detailDiseaseSolution.text = it.solution
        } ?: run {
            detailDiseaseCauses.text = "-"
            detailDiseaseSymptoms.text = "-"
            detailDiseaseImpact.text = "-"
            detailDiseaseSolution.text = "-"
        }
    }
}
