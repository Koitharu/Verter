package org.koitharu.verter.core.ssh

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.koitharu.verter.core.devices.RemoteDevice
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext

class RemoteDeviceSession(
	private val device: RemoteDevice,
) : ConnectionMonitor {

	private val connection = Connection(device.address, device.port)
	private val context: CoroutineContext = Dispatchers.IO
	private val isConnected = MutableStateFlow(false)

	init {
		connection.addConnectionMonitor(this)
	}

	override fun connectionLost(reason: Throwable?) {
		isConnected.value = false
	}

	fun getConnectedAsFlow(): Flow<RemoteDeviceSession?> = isConnected.map {
		if (it) this else null
	}

	suspend fun connect() = withContext(context) {
		connection.connect()
		val result = connection.authenticateWithPassword(device.user, device.password)
		isConnected.value = result
		result
	}

	suspend fun close() = withContext(context + NonCancellable) {
		connection.close()
	}

	suspend fun execute(cmdline: String): String? {
		if (!connection.isAuthenticationComplete) {
			return null
		}
		return withContext(context) {
			connection.openSession().use { session ->
				session.execCommand(cmdline)
				session.waitForCondition(ChannelCondition.EXIT_STATUS, 5_000L)
				if (session.exitStatus == 0) {
					session.stdout.bufferedReader().use { it.readText() }.trim()
				} else {
					val errMsg = session.stderr.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
					throw RemoteProcessException(
						exitCode = session.exitStatus,
						message = errMsg,
					)
				}
			}
		}
	}

	fun executeContinuously(cmdline: String): Flow<String> {
		if (!connection.isAuthenticationComplete) {
			return emptyFlow()
		}
		return channelFlow {
			connection.openSession().use { session ->
				session.execCommand(cmdline)
				invokeOnClose {
					session.close()
				}
				session.stdout.bufferedReader().use {
					for (line in it.lineSequence()) {
						val result = trySend(line.trim())
						if (result.isClosed) {
							break
						}
					}
				}
				if (session.exitStatus == 0) {
					close()
				} else {
					val errMsg = session.stderr.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
					close(
						RemoteProcessException(
							exitCode = session.exitStatus,
							message = errMsg,
						)
					)
				}
			}
		}.flowOn(context)
	}

	suspend fun getFileContent(path: String): ByteArray = runInterruptible(context) {
		val client = connection.createSCPClient()
		val output = ByteArrayOutputStream()
		client.get(path, output)
		output.toByteArray()
	}
}