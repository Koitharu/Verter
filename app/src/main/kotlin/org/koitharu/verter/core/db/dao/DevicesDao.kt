package org.koitharu.verter.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.koitharu.verter.core.db.entity.DeviceEntity

@Dao
interface DevicesDao {

	@Query("SELECT * FROM devices")
	fun observeAll(): Flow<List<DeviceEntity>>

	@Insert
	suspend fun insert(entity: DeviceEntity)

	@Query("UPDATE devices SET connected_at = :connectedAt WHERE id = :id")
	suspend fun setConnectedAt(id: Int, connectedAt: Long)

	@Query("SELECT * FROM devices ORDER BY connected_at DESC LIMIT 1")
	suspend fun getLastOrNull(): DeviceEntity?
}