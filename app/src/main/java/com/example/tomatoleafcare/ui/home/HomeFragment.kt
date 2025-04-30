package com.example.tomatoleafcare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tomatoleafcare.DiseaseDetailFragment
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.adapter.DiseaseAdapter
import com.example.tomatoleafcare.repository.DiseaseRepository
import com.example.tomatoleafcare.model.Disease

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiseaseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        val diseaseList = DiseaseRepository.diseaseList

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DiseaseAdapter(diseaseList, object : DiseaseAdapter.OnItemClickListener {
            override fun onItemClick(disease: Disease) {
                val detailFragment = DiseaseDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("disease_name", disease.name)
                        putString("disease_description", disease.description)
                        putString("disease_symptoms", disease.symptoms)
                        putString("disease_causes", disease.cause)
                        putString("disease_impact", disease.impact)
                        putString("disease_solution", disease.solution)
                        putInt("disease_image", disease.imageResId)
                    }
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

        recyclerView.adapter = adapter

        return view
    }
}
