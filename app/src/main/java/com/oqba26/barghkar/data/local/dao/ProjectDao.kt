package com.oqba26.barghkar.data.local.dao

import androidx.room.*
import com.oqba26.barghkar.data.local.entity.InstallmentEntity
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT * FROM materials WHERE projectId = :projectId")
    fun getMaterialsForProject(projectId: Long): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM installments WHERE projectId = :projectId ORDER BY dueDate ASC")
    fun getInstallmentsForProject(projectId: Long): Flow<List<InstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallment(installment: InstallmentEntity)

    @Delete
    suspend fun deleteInstallment(installment: InstallmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("SELECT * FROM materials WHERE isSynced = 0")
    suspend fun getUnsyncedMaterials(): List<MaterialEntity>

    @Query("SELECT * FROM installments WHERE isSynced = 0")
    suspend fun getUnsyncedInstallments(): List<InstallmentEntity>

    @Update
    suspend fun updateInstallment(installment: InstallmentEntity)

    @Query("SELECT * FROM projects WHERE isSynced = 0")
    suspend fun getUnsyncedProjects(): List<ProjectEntity>

    @Update
    suspend fun updateProject(project: ProjectEntity)
}
