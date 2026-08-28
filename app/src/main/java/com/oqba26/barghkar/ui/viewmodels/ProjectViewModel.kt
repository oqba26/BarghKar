package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.ProjectRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.InstallmentEntity
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import com.oqba26.barghkar.security.InputValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.oqba26.barghkar.data.remote.AuthRepository
import com.oqba26.barghkar.data.sync.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository
    private val authRepository = AuthRepository()

    val allProjects: StateFlow<List<ProjectEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        val projectDao = database.projectDao()
        val customerDao = database.customerDao()
        repository = ProjectRepository(projectDao, customerDao)
        allProjects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
        
        viewModelScope.launch {
            authRepository.userProfileFlow.collect { profile ->
                val ownerId = if (profile?.role == com.oqba26.barghkar.data.model.UserRole.APPRENTICE) {
                    profile.masterId ?: profile.id
                } else {
                    profile?.id ?: ""
                }
                if (ownerId.isNotEmpty()) {
                    repository.syncRemoteToLocal(ownerId)
                }
            }
        }
    }

    fun addProjectRemote(
        name: String,
        description: String,
        customerId: Long? = null,
        totalWage: Long = 0L,
        area: Double = 0.0,
        priceFixture: Long = 0L,
        priceMeter: Long = 0L,
        p1: Long = 0L,
        p2: Long = 0L,
        p3: Long = 0L,
    ) {
        InputValidators.validateProject(name, description, area, priceFixture, priceMeter, p1, p2, p3)?.let { 
            throw IllegalArgumentException(it) 
        }

        viewModelScope.launch {
            val profile = authRepository.userProfileFlow.first()
            val ownerId = if (profile?.role == com.oqba26.barghkar.data.model.UserRole.APPRENTICE) {
                profile.masterId ?: profile.id
            } else {
                profile?.id ?: ""
            }

            if (ownerId.isNotEmpty()) {
                repository.addProject(
                    ProjectEntity(
                        userId = ownerId,
                        customerId = customerId,
                        name = name.trim(),
                        description = description.trim(),
                        totalWage = totalWage,
                        infrastructureArea = area,
                        pricePerFixture = priceFixture,
                        pricePerMeter = priceMeter,
                        firstPayment = p1,
                        secondPayment = p2,
                        thirdPayment = p3
                    )
                )
                SyncManager.triggerImmediateSync(getApplication())
            }
        }
    }

    fun deleteProjectRemote(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun updateProjectRemote(project: ProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(project)
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun getMaterialsRemote(projectId: Long): StateFlow<List<MaterialEntity>> {
        val materials = MutableStateFlow<List<MaterialEntity>>(emptyList())
        viewModelScope.launch {
            repository.getMaterialsForProject(projectId).collect {
                materials.value = it
            }
        }
        return materials
    }

    fun addMaterialRemote(projectId: Long, name: String, quantity: Int, unit: String, pricePerUnit: Long = 0L, status: com.oqba26.barghkar.data.model.RecordStatus = com.oqba26.barghkar.data.model.RecordStatus.APPROVED) {
        val validationError = InputValidators.validateMaterial(name, quantity, unit, pricePerUnit)
        if (validationError != null) {
            throw IllegalArgumentException(validationError)
        }

        viewModelScope.launch {
            val profile = authRepository.userProfileFlow.first()
            val ownerId = profile?.id ?: ""
            repository.addMaterial(
                MaterialEntity(
                    userId = ownerId,
                    projectId = projectId,
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit.trim(),
                    pricePerUnit = pricePerUnit,
                    status = status
                )
            )
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun deleteMaterialRemote(material: MaterialEntity) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun updateMaterialRemote(material: MaterialEntity) {
        viewModelScope.launch {
            repository.updateMaterial(material)
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun getInstallmentsRemote(projectId: Long): StateFlow<List<InstallmentEntity>> {
        val installments = MutableStateFlow<List<InstallmentEntity>>(emptyList())
        viewModelScope.launch {
            repository.getInstallmentsForProject(projectId).collect {
                installments.value = it
            }
        }
        return installments
    }

    fun addInstallmentRemote(projectId: Long, amount: Long, dueDate: Long, status: com.oqba26.barghkar.data.model.RecordStatus = com.oqba26.barghkar.data.model.RecordStatus.APPROVED) {
        val validationError = InputValidators.validateInstallment(amount)
        if (validationError != null) {
            throw IllegalArgumentException(validationError)
        }

        viewModelScope.launch {
            val profile = authRepository.userProfileFlow.first()
            val ownerId = profile?.id ?: ""
            repository.addInstallment(
                InstallmentEntity(
                    userId = ownerId,
                    projectId = projectId,
                    amount = amount,
                    dueDate = dueDate,
                    status = status
                )
            )
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun deleteInstallmentRemote(installment: InstallmentEntity) {
        viewModelScope.launch {
            repository.deleteInstallment(installment)
            SyncManager.triggerImmediateSync(getApplication())
        }
    }

    fun updateInstallmentRemote(installment: InstallmentEntity) {
        viewModelScope.launch {
            repository.updateInstallment(installment)
            SyncManager.triggerImmediateSync(getApplication())
        }
    }
}
