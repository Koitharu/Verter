package org.koitharu.verter.core.ssh

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.koitharu.verter.core.devices.RemoteDevice
import java.io.File

interface SshConnection {

	val deviceInfo: RemoteDevice

	val lastError: Throwable?

	fun getIsConnectedAsFlow(): StateFlow<Boolean>

	suspend fun execute(cmdline: String): String

	fun executeContinuously(cmdline: String): Flow<String>

	suspend fun getFileContent(path: String): ByteArray

	suspend fun writeFile(file: File, destinationDir: String)
}

val SshConnection.isConnected: Boolean
	get() = getIsConnectedAsFlow().value

suspend fun SshConnection.tryExecute(cmdline: String) = runCatching {
	execute(cmdline)
}

suspend fun SshConnection.awaitConnection() {
	try {
		withTimeout(5_000) {
			getIsConnectedAsFlow().filter { it }.first()
		}
	} catch (e: Exception) {
		val error = lastError ?: e
		throw error
	}
}