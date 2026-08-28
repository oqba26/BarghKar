package com.oqba26.barghkar.data

import android.util.Log
import com.oqba26.barghkar.data.local.dao.CustomerDao
import com.oqba26.barghkar.data.local.dao.ProjectDao
import com.oqba26.barghkar.data.local.entity.InstallmentEntity
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import com.oqba26.barghkar.data.model.InstallmentRemote
import com.oqba26.barghkar.data.model.MaterialRemote
import com.oqba26.barghkar.data.model.ProjectRemote
import com.oqba26.barghkar.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val customerDao: CustomerDao,
) {
    private val postgrest get() = SupabaseClient.client.postgrest

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allInstallments: Flow<List<InstallmentEntity>> = projectDao.getAllInstallments()

    suspend fun syncRemoteToLocal(ownerId: String) {
        try {
            // ۱. همگام‌سازی پروژه‌ها
            val remoteProjects = postgrest["projects"].select {
                filter { eq("user_id", ownerId) }
            }.decodeList<ProjectRemote>()
            
            remoteProjects.forEach { remoteProject ->
                val remoteId = remoteProject.id ?: return@forEach
                val localProject = projectDao.getProjectByRemoteId(remoteId)
                
                // حل شناسه مشتری محلی
                val localCustomerId = remoteProject.customerId?.let { 
                    customerDao.getCustomerByRemoteId(it)?.id 
                }

                val projectEntity = ProjectEntity(
                    userId = remoteProject.userId,
                    name = remoteProject.name,
                    description = remoteProject.description,
                    customerId = localCustomerId,
                    totalWage = remoteProject.totalWage,
                    createdAt = remoteProject.createdAt,
                    remoteId = remoteId,
                    isSynced = true,
                    infrastructureArea = remoteProject.infrastructureArea,
                    pricePerFixture = remoteProject.pricePerFixture,
                    pricePerMeter = remoteProject.pricePerMeter,
                    firstPayment = remoteProject.firstPayment,
                    secondPayment = remoteProject.secondPayment,
                    thirdPayment = remoteProject.thirdPayment
                )

                if (localProject == null) {
                    projectDao.insertProject(projectEntity)
                } else {
                    projectDao.updateProject(projectEntity.copy(id = localProject.id))
                }
            }

            // ۲. همگام‌سازی مصالح
            val remoteMaterials = postgrest["materials"].select {
                filter { eq("user_id", ownerId) }
            }.decodeList<MaterialRemote>()
            
            remoteMaterials.forEach { remoteMaterial ->
                val remoteId = remoteMaterial.id ?: return@forEach
                val localMaterial = projectDao.getMaterialByRemoteId(remoteId)
                
                // حل شناسه پروژه محلی
                val localProjectId = projectDao.getProjectByRemoteId(remoteMaterial.projectId)?.id ?: return@forEach
                
                val materialEntity = MaterialEntity(
                    userId = remoteMaterial.userId,
                    projectId = localProjectId,
                    name = remoteMaterial.name,
                    quantity = remoteMaterial.quantity,
                    unit = remoteMaterial.unit,
                    pricePerUnit = remoteMaterial.pricePerUnit,
                    remoteId = remoteId,
                    isSynced = true,
                    status = remoteMaterial.status
                )

                if (localMaterial == null) {
                    projectDao.insertMaterial(materialEntity)
                } else {
                    projectDao.updateMaterial(materialEntity.copy(id = localMaterial.id))
                }
            }

            // ۳. همگام‌سازی اقساط
            val remoteInstallments = postgrest["installments"].select {
                filter { eq("user_id", ownerId) }
            }.decodeList<InstallmentRemote>()
            
            remoteInstallments.forEach { remoteInstallment ->
                val remoteId = remoteInstallment.id ?: return@forEach
                val localInstallment = projectDao.getInstallmentByRemoteId(remoteId)
                
                // حل شناسه پروژه محلی
                val localProjectId = projectDao.getProjectByRemoteId(remoteInstallment.projectId)?.id ?: return@forEach
                
                val installmentEntity = InstallmentEntity(
                    userId = remoteInstallment.userId,
                    projectId = localProjectId,
                    amount = remoteInstallment.amount,
                    dueDate = remoteInstallment.dueDate,
                    isPaid = remoteInstallment.isPaid,
                    remoteId = remoteId,
                    isSynced = true,
                    status = remoteInstallment.status
                )

                if (localInstallment == null) {
                    projectDao.insertInstallment(installmentEntity)
                } else {
                    projectDao.updateInstallment(installmentEntity.copy(id = localInstallment.id))
                }
            }

        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error syncing projects", e)
        }
    }

    suspend fun addProject(project: ProjectEntity) = projectDao.insertProject(project.copy(isSynced = false))
    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project.copy(isSynced = false))
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    fun getMaterialsForProject(projectId: Long): Flow<List<MaterialEntity>> = 
        projectDao.getMaterialsForProject(projectId)

    suspend fun addMaterial(material: MaterialEntity) = projectDao.insertMaterial(material.copy(isSynced = false))
    suspend fun updateMaterial(material: MaterialEntity) = projectDao.updateMaterial(material.copy(isSynced = false))
    suspend fun deleteMaterial(material: MaterialEntity) = projectDao.deleteMaterial(material)

    fun getInstallmentsForProject(projectId: Long): Flow<List<InstallmentEntity>> =
        projectDao.getInstallmentsForProject(projectId)

    suspend fun addInstallment(installment: InstallmentEntity) = projectDao.insertInstallment(installment.copy(isSynced = false))
    suspend fun updateInstallment(installment: InstallmentEntity) = projectDao.updateInstallment(installment.copy(isSynced = false))
    suspend fun deleteInstallment(installment: InstallmentEntity) = projectDao.deleteInstallment(installment)
}
