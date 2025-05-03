package com.example.tomatoleafcare

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.tomatoleafcare.viewmodel.DiseaseViewModel
import com.google.android.material.imageview.ShapeableImageView

class DiseaseDetailFragment : Fragment() {

    private val diseaseViewModel: DiseaseViewModel by activityViewModels()

    private lateinit var imageView: ShapeableImageView
    private lateinit var nameTextView: TextView
    private lateinit var causesTextView: TextView
    private lateinit var symptomsTextView: TextView
    private lateinit var impactTextView: TextView
    private lateinit var solutionTextView: TextView
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.disease_detail_fragment, container, false)

        imageView = view.findViewById(R.id.detailDiseaseImage)
        nameTextView = view.findViewById(R.id.detailDiseaseName)
        causesTextView = view.findViewById(R.id.detailDiseaseCauses)
        symptomsTextView = view.findViewById(R.id.detailDiseaseSymptoms)
        impactTextView = view.findViewById(R.id.detailDiseaseImpact)
        solutionTextView = view.findViewById(R.id.detailDiseaseSolution)
        backButton = view.findViewById(R.id.btnBack)

        val diseaseName = arguments?.getString("disease_name")
        diseaseName?.let {
            diseaseViewModel.loadDiseaseByName(it)
        }

        diseaseViewModel.disease.observe(viewLifecycleOwner, Observer { disease ->
            nameTextView.text = disease.name
            causesTextView.text = disease.cause
            symptomsTextView.text = disease.symptoms
            impactTextView.text = disease.impact
            solutionTextView.text = disease.solution

            val imageUriString = arguments?.getString("image_uri")
            if (!imageUriString.isNullOrEmpty()) {
                val imageUri = Uri.parse(imageUriString)
                imageView.setImageURI(imageUri)
            } else {

                imageView.setImageResource(disease.imageResId)
            }
        })

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
