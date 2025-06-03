package com.example.tomatoleafcare.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tomatoleafcare.R
import com.example.tomatoleafcare.databinding.ActivityMainBinding
import com.example.tomatoleafcare.ui.note.NoteFragment
import com.example.tomatoleafcare.ui.camera.CameraFragment
import com.example.tomatoleafcare.ui.history.HistoryFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> replaceFragment(HomeFragment())
                R.id.navigation_camera -> replaceFragment(CameraFragment())
                R.id.navigation_note -> replaceFragment(NoteFragment())
                R.id.navigation_history -> replaceFragment(HistoryFragment())
            }
            true
        }
    }

    fun showBottomNav(show: Boolean) {
        val bottomNav = findViewById<View>(R.id.bottomNavigationView)
        bottomNav.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
