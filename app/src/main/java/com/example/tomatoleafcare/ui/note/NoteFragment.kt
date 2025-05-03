package com.example.tomatoleafcare.ui.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tomatoleafcare.DiseaseDetailFragment
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.adapter.DiseaseAdapter
import com.example.tomatoleafcare.repository.DiseaseRepository
import com.example.tomatoleafcare.model.Disease
import com.example.tomatoleafcare.viewmodel.DiseaseViewModel

class NoteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiseaseAdapter
    private val viewModel: DiseaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.note_fragment, container, false)

        val classList = DiseaseRepository.classList.filter { it.name != "Tanaman Sehat" }

        recyclerView = view.findViewById(R.id.recyclerViewNote)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DiseaseAdapter(classList, object : DiseaseAdapter.OnItemClickListener {
            override fun onItemClick(disease: Disease) {
                viewModel.loadDiseaseByName(disease.name)

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DiseaseDetailFragment())
                    .addToBackStack(null)
                    .commit()
            }
        })

        recyclerView.adapter = adapter

        return view
    }
}
