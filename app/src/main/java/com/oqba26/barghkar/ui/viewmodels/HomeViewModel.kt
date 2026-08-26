package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.InventoryRepository
import com.oqba26.barghkar.data.ProjectRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.dao.ProjectDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import saman.zamani.persiandate.PersianDate

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
        projectRepository = ProjectRepository(projectDao)
        inventoryRepository = InventoryRepository(database.inventoryDao())

        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            // Get current Persian month and year
            val pDate = PersianDate()
            val currentMonth = pDate.shMonth
            val currentYear = pDate.shYear

            // Observe Installments for Income
            projectDao.getAllInstallments().collectLatest { installments ->
                val income = installments.filter { 
                    it.isPaid && isSamePersianMonth(it.dueDate, currentMonth, currentYear) 
                }.sumOf { it.amount }
                _monthlyIncome.value = income
                calculateProfit()
            }
        }

        viewModelScope.launch {
            inventoryRepository.allInventory.collectLatest { inventory ->
                _inventoryCount.value = inventory.size
            }
        }
    }

    private fun calculateProfit() {
        viewModelScope.launch {
            val pDate = PersianDate()
            val currentMonth = pDate.shMonth
            val currentYear = pDate.shYear

            val installments = projectDao.getAllInstallments().first()
            val materials = projectDao.getAllMaterials().first()

            val income = installments.filter { 
                it.isPaid && isSamePersianMonth(it.dueDate, currentMonth, currentYear) 
            }.sumOf { it.amount }

            val materialCost = materials.sumOf { it.quantity * it.pricePerUnit }
            
            _monthlyProfit.value = income - materialCost
        }
    }

    private fun isSamePersianMonth(timestamp: Long, month: Int, year: Int): Boolean {
        val pDate = PersianDate(timestamp)
        return pDate.shMonth == month && pDate.shYear == year
    }
}
