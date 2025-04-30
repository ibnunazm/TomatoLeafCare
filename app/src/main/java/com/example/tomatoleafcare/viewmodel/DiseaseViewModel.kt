package com.example.tomatoleafcare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tomatoleafcare.model.Disease
import com.example.tomatoleafcare.repository.DiseaseRepository

class DiseaseViewModel : ViewModel() {

    private val _disease = MutableLiveData<Disease>()
    val disease: LiveData<Disease> get() = _disease

    fun loadDiseaseByName(name: String) {
        val result = DiseaseRepository.findDiseaseByName(name)
        result?.let {
            _disease.value = it
        }
    }
}
