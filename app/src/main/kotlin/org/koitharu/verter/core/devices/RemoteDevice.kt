package org.koitharu.verter.core.devices

data class RemoteDevice(
	val id: Int,
	val address: String,
	val port: Int,
	val user: String,
	val password: String,
	val alias: String?,
) {

	val displayName: String
		get() = alias ?: "$address:$port"
}