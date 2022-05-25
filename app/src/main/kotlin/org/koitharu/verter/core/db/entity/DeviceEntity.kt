package org.koitharu.verter.core.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.koitharu.verter.core.devices.RemoteDevice

@Entity(tableName = "devices")
class DeviceEntity(
	@ColumnInfo("id") @PrimaryKey(autoGenerate = true) val id: Int,
	@ColumnInfo("address") val address: String,
	@ColumnInfo("port") val port: Int,
	@ColumnInfo("user") val user: String,
	@ColumnInfo("password") val password: String,
	@ColumnInfo("alias") val alias: String?,
)

fun RemoteDevice.toEntity() = DeviceEntity(
	id = id,
	address = address,
	port = port,
	user = user,
	password = password,
	alias = alias,
)

fun DeviceEntity.toDevice() = RemoteDevice(
	id = id,
	address = address,
	port = port,
	user = user,
	password = password,
	alias = alias,
)