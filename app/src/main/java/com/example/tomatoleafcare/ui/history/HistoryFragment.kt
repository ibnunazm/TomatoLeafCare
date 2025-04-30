package com.example.tomatoleafcare.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.ui.adapter.HistoryAdapter
import com.example.tomatoleafcare.viewmodel.HistoryViewModel

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.history_fragment, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter(
            context = requireContext(),
            historyList = listOf(),
            onItemClick = { history ->
                val bundle = Bundle()
                bundle.putSerializable("history", history)

                val detailFragment = HistoryDetailFragment().apply {
                    arguments = bundle
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { item ->
                viewModel.deleteHistoryById(item.id.toInt())
            }
        )

        recyclerView.adapter = historyAdapter

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewModel.historyList.observe(viewLifecycleOwner, Observer { list ->
            historyAdapter.updateData(list)
        })
    }
}
