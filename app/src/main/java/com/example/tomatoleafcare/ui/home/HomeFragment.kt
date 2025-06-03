package com.example.tomatoleafcare.ui.home

import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tomatoleafcare.DiseaseDetailFragment
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.adapter.DiseaseAdapter
import com.example.tomatoleafcare.adapter.SliderAdapter
import com.example.tomatoleafcare.model.Disease
import com.example.tomatoleafcare.repository.DiseaseRepository
import com.example.tomatoleafcare.ui.note.NoteFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiseaseAdapter
    private lateinit var viewPager: ViewPager2
    private val sliderHandler = Handler(Looper.getMainLooper())

    private val sliderRunnable = object : Runnable {
        override fun run() {
            val itemCount = viewPager.adapter?.itemCount ?: 0
            if (itemCount > 0) {
                val nextItem = (viewPager.currentItem + 1) % itemCount
                viewPager.setCurrentItem(nextItem, true)
                sliderHandler.postDelayed(this, 10000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        val classList = DiseaseRepository.classList.filter { it.name != "Tanaman Sehat" }
        val topThreeDiseases = classList.take(3)

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = SliderAdapter(this)

        val space = resources.getDimensionPixelSize(R.dimen.slider_gap)
        viewPager.addItemDecoration(SliderItemDecoration(space))

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val seeMoreText: TextView = view.findViewById(R.id.seeMoreText)
        seeMoreText.paintFlags = seeMoreText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        seeMoreText.setOnClickListener {
            val noteFragment = NoteFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, noteFragment)
                .addToBackStack(null)
                .commit()

            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.selectedItemId = R.id.navigation_note
        }

        adapter = DiseaseAdapter(topThreeDiseases, object : DiseaseAdapter.OnItemClickListener {
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

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    class SliderItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.left = space
            outRect.right = space
        }
    }
}
