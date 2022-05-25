package org.koitharu.verter.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koitharu.verter.core.db.dao.ActionsDao
import org.koitharu.verter.core.db.dao.DevicesDao
import org.koitharu.verter.core.db.entity.ActionEntity
import org.koitharu.verter.core.db.entity.DeviceEntity

@Database(
	entities = [DeviceEntity::class, ActionEntity::class],
	version = 1,
	exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

	abstract val devicesDao: DevicesDao

	abstract val actionsDao: ActionsDao
}

fun AppDatabase(context: Context) = Room.databaseBuilder(context, AppDatabase::class.java, "verter-db")
	.build()