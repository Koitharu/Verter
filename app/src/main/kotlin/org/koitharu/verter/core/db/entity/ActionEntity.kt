package org.koitharu.verter.core.db.entity

import androidx.room.*
import org.koitharu.verter.core.actions.RemoteAction

@Entity(
	tableName = "actions",
	foreignKeys = [
		ForeignKey(
			entity = ActionEntity::class,
			parentColumns = ["id"],
			childColumns = ["device_id"],
			onDelete = ForeignKey.CASCADE,
		)
	],
	indices = [Index("device_id")],
)
data class ActionEntity(
	@ColumnInfo("id") @PrimaryKey(autoGenerate = true) val id: Int,
	@ColumnInfo("name") val name: String,
	@ColumnInfo("cmdline") val cmdline: String,
	@ColumnInfo("device_id") val deviceId: Int?,
	@ColumnInfo("enabled") val enabled: Boolean,
	@ColumnInfo("order_key") val orderKey: Int,
)

fun ActionEntity.toAction() = RemoteAction(
	id = id,
	name = name,
	cmdline = cmdline,
)