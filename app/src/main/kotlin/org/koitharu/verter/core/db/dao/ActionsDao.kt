package org.koitharu.verter.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.koitharu.verter.core.db.entity.ActionEntity

@Dao
interface ActionsDao {

	@Query("SELECT * FROM actions WHERE device_id IS NULL OR device_id = :deviceId ORDER BY order_key")
	fun observeAll(deviceId: Int): Flow<List<ActionEntity>>

	@Query("SELECT * FROM actions WHERE device_id IS NULL ORDER BY order_key")
	fun observeAllCommon(): Flow<List<ActionEntity>>

	@Query("SELECT * FROM actions WHERE id = :id")
	suspend fun get(id: Int): ActionEntity

	@Insert
	suspend fun insert(entity: ActionEntity)

	@Update
	suspend fun update(entity: ActionEntity)

	@Query("DELETE FROM actions WHERE id = :id")
	suspend fun delete(id: Int)
}