package com.example.tomatoleafcare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tomatoleafcare.helper.HistoryDatabaseHelper
import com.example.tomatoleafcare.model.History

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = HistoryDatabaseHelper(application)

    private val _historyList = MutableLiveData<List<History>>()
    val historyList: LiveData<List<History>> get() = _historyList

    init {
        loadAllHistory()
    }

    fun loadAllHistory() {
        val allHistory = dbHelper.getAllHistory()
        _historyList.postValue(allHistory)
    }

    fun deleteHistoryById(id: Int) {
        val success = dbHelper.deleteHistory(id.toLong())
        if (success) {
            loadAllHistory()
        }
    }

    fun insertHistory(item: History) {
        dbHelper.insertHistory(item)
        loadAllHistory()
    }
}
