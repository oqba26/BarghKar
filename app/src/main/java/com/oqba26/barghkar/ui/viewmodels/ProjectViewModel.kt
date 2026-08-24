package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.ProjectRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.InstallmentEntity
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository
    val allProjects: StateFlow<List<ProjectEntity>>

    private val _projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    init {
        val projectDao = AppDatabase.getDatabase(application).projectDao()
        repository = ProjectRepository(projectDao)
        allProjects = _projects
        
        viewModelScope.launch {
            repository.allProjects.collectLatest {
                _projects.value = it
            }
        }
    }

    fun addProject(
        name: String, 
        description: String, 
        customerId: Long? = null, 
        totalWage: Long = 0L,
        area: Double = 0.0,
        priceFixture: Long = 0L,
        priceMeter: Long = 0L,
        p1: Long = 0L,
        p2: Long = 0L,
        p3: Long = 0L
    ) {
        viewModelScope.launch {
            repository.insertProject(ProjectEntity(
                name = name, 
                description = description, 
                customerId = customerId, 
                totalWage = totalWage,
                infrastructureArea = area,
                pricePerFixture = priceFixture,
                pricePerMeter = priceMeter,
                firstPayment = p1,
                secondPayment = p2,
                thirdPayment = p3
            ))
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.insertProject(project) // Insert with ID acts as update
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun getMaterials(projectId: Long): StateFlow<List<MaterialEntity>> {
        val materials = MutableStateFlow<List<MaterialEntity>>(emptyList())
        viewModelScope.launch {
            repository.getMaterialsForProject(projectId).collectLatest {
                materials.value = it
            }
        }
        return materials
    }

    fun addMaterial(projectId: Long, name: String, quantity: Int, unit: String, pricePerUnit: Long = 0L, status: com.oqba26.barghkar.data.model.RecordStatus = com.oqba26.barghkar.data.model.RecordStatus.APPROVED) {
        viewModelScope.launch {
            repository.insertMaterial(MaterialEntity(projectId = projectId, name = name, quantity = quantity, unit = unit, pricePerUnit = pricePerUnit, status = status))
        }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
        }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            repository.insertMaterial(material)
        }
    }

    fun getInstallments(projectId: Long): StateFlow<List<InstallmentEntity>> {
        val installments = MutableStateFlow<List<InstallmentEntity>>(emptyList())
        viewModelScope.launch {
            repository.getInstallmentsForProject(projectId).collectLatest {
                installments.value = it
            }
        }
        return installments
    }

    fun addInstallment(projectId: Long, amount: Long, dueDate: Long, status: com.oqba26.barghkar.data.model.RecordStatus = com.oqba26.barghkar.data.model.RecordStatus.APPROVED) {
        viewModelScope.launch {
            repository.insertInstallment(InstallmentEntity(projectId = projectId, amount = amount, dueDate = dueDate, status = status))
        }
    }

    fun deleteInstallment(installment: InstallmentEntity) {
        viewModelScope.launch {
            repository.deleteInstallment(installment)
        }
    }

    fun updateInstallment(installment: InstallmentEntity) {
        viewModelScope.launch {
            repository.insertInstallment(installment)
        }
    }
}
