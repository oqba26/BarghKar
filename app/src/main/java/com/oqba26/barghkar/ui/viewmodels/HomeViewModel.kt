package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.InventoryRepository
import com.oqba26.barghkar.data.ProjectRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.dao.ProjectDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val projectRepository: ProjectRepository
    private val inventoryRepository: InventoryRepository
    private val projectDao: ProjectDao

    private val _monthlyIncome = MutableStateFlow(0L)
    val monthlyIncome: StateFlow<Long> = _monthlyIncome.asStateFlow()

    private val _monthlyProfit = MutableStateFlow(0L)
    val monthlyProfit: StateFlow<Long> = _monthlyProfit.asStateFlow()

    private val _inventoryCount = MutableStateFlow(0)
    val inventoryCount: StateFlow<Int> = _inventoryCount.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        projectDao = database.projectDao()
        projectRepository = ProjectRepository(projectDao, database.customerDao())
        inventoryRepository = InventoryRepository(database.inventoryDao())

        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            inventoryRepository.allInventory.collect { inventory ->
                _inventoryCount.value = inventory.size
            }
        }

        viewModelScope.launch {
            projectRepository.allInstallments.collect { installments ->
                val pDate = PersianDate()
                val currentMonth = pDate.shMonth
                val currentYear = pDate.shYear

                val income = installments.asSequence().filter { 
                    it.isPaid && isSamePersianMonth(it.dueDate, currentMonth, currentYear) 
                }.sumOf { it.amount }
                _monthlyIncome.value = income
                
                // Profit calculation placeholder
                _monthlyProfit.value = income / 2
            }
        }
    }

    private fun isSamePersianMonth(timestamp: Long, month: Int, year: Int): Boolean {
        val pDate = PersianDate(timestamp)
        return (pDate.shMonth == month) && (pDate.shYear == year)
    }
}
