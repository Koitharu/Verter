package org.koitharu.verter.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.koitharu.verter.core.db.entity.DeviceEntity

@Dao
abstract class DevicesDao {

	@Query("SELECT * FROM devices")
	abstract fun observeAll(): Flow<List<DeviceEntity>>

	@Insert
	abstract suspend fun insert(entity: DeviceEntity)
}