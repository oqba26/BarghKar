package com.oqba26.barghkar.data

import com.oqba26.barghkar.data.local.dao.ProjectDao
import com.oqba26.barghkar.data.local.entity.InstallmentEntity
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun insertProject(project: ProjectEntity) = projectDao.insertProject(project)
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    fun getMaterialsForProject(projectId: Long): Flow<List<MaterialEntity>> = 
        projectDao.getMaterialsForProject(projectId)

    suspend fun insertMaterial(material: MaterialEntity) = projectDao.insertMaterial(material)
    suspend fun deleteMaterial(material: MaterialEntity) = projectDao.deleteMaterial(material)

    fun getInstallmentsForProject(projectId: Long): Flow<List<InstallmentEntity>> =
        projectDao.getInstallmentsForProject(projectId)

    suspend fun insertInstallment(installment: InstallmentEntity) = projectDao.insertInstallment(installment)
    suspend fun deleteInstallment(installment: InstallmentEntity) = projectDao.deleteInstallment(installment)
}
