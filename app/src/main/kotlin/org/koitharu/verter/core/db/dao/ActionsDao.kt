package org.koitharu.verter.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.koitharu.verter.core.db.entity.ActionEntity

@Dao
interface ActionsDao {

	@Query("SELECT * FROM actions WHERE device_id IS NULL OR device_id = :deviceId ORDER BY order_key")
	fun observeAll(deviceId: Int): Flow<List<ActionEntity>>

	@Query("SELECT * FROM actions WHERE device_id IS NULL ORDER BY order_key")
	fun observeAllCommon(): Flow<List<ActionEntity>>

	@Insert
	suspend fun insert(entity: ActionEntity)
}