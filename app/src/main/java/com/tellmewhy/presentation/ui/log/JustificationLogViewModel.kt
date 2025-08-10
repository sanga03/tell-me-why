package com.tellmewhy.presentation.ui.log // Or your UI package

import android.app.Application
import androidx.lifecycle.AndroidViewModel
// import androidx.lifecycle.LiveData // If using LiveData
// import androidx.lifecycle.asLiveData // If converting Flow to LiveData
import com.tellmewhy.data.local.db.AppDatabase
import com.tellmewhy.domain.model.JustificationEntry
import com.tellmewhy.data.local.dao.JustificationDao
import kotlinx.coroutines.flow.Flow // Use Flow

class JustificationLogViewModel(application: Application) : AndroidViewModel(application) {
    private val justificationDao: JustificationDao

    init {
        val database = AppDatabase.getDatabase(application)
        justificationDao = database.justificationDao()
    }

    // Expose Flow directly
    val allJustifications: Flow<List<JustificationEntry>> =
        justificationDao.getAllJustifications()

    // ... (other methods if any)
}