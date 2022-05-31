package org.koitharu.verter.core.ssh

import androidx.collection.ArrayMap
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koitharu.verter.BuildConfig
import org.koitharu.verter.core.devices.RemoteDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SshConnectionManager @Inject constructor() : OnConnectionLostListener {

	private val coroutineScope = ProcessLifecycleOwner.get().lifecycle.coroutineScope + Dispatchers.Default
	private val connections = ArrayMap<RemoteDevice, ConnectionWrapper>()

	fun getConnection(device: RemoteDevice): SshConnection {
		val connection = connections.getOrPut(device) { ConnectionWrapper(device, this) }
		if (!connection.isConnectedOrConnecting) {
			connection.connectAsync(coroutineScope)
		}
		return connection
	}

	fun closeConnection(device: RemoteDevice) {
		connections.remove(device)?.disconnectAsync(coroutineScope)
	}

	override fun onConnectionLost(device: RemoteDevice, reason: Throwable?) {
		if (BuildConfig.DEBUG) {
			reason?.printStackTrace()
		}
		val connection = connections[device] ?: return
		connection.connectAsync(coroutineScope)
	}

	private class ConnectionWrapper(
		override val deviceInfo: RemoteDevice,
		private val onConnectionLostListener: OnConnectionLostListener,
	) : ConnectionMonitor, SshConnection {

		private val connection = Connection(deviceInfo.address, deviceInfo.port)
		private val isConnected = MutableStateFlow(false)
		private var connectJob: Job? = null
		@Volatile
		override var lastError: Throwable? = null
			private set

		val isConnectedOrConnecting: Boolean
			get() = isConnected.value || connectJob?.isActive == true

		private val errorHandler = CoroutineExceptionHandler { _, throwable ->
			lastError = throwable
		}

		init {
			connection.addConnectionMonitor(this)
		}

		override fun connectionLost(reason: Throwable?) {
			onConnectionLostListener.onConnectionLost(deviceInfo, reason)
		}

		override fun getIsConnectedAsFlow(): StateFlow<Boolean> {
			return isConnected.asStateFlow()
		}

		override suspend fun execute(cmdline: String): String = runInterruptible(Dispatchers.IO) {
			connection.execute(cmdline)
		}

		override fun executeContinuously(cmdline: String): Flow<String> {
			return connection.executeContinuously(cmdline).flowOn(Dispatchers.IO)
		}

		override suspend fun getFileContent(path: String): ByteArray = runInterruptible(Dispatchers.IO) {
			connection.getFileContent(path)
		}

		fun connectAsync(scope: CoroutineScope) {
			val prevJob = connectJob
			connectJob = scope.launch(errorHandler) {
				prevJob?.cancelAndJoin()
				connect()
			}
		}

		fun disconnectAsync(scope: CoroutineScope) {
			val prevJob = connectJob
			connectJob = scope.launch(errorHandler) {
				prevJob?.cancelAndJoin()
				disconnect()
			}
		}

		private suspend fun connect() = runInterruptible(Dispatchers.IO) {
			connection.connect()
			connection.authenticateWithPassword(deviceInfo.user, deviceInfo.password).also { result ->
				isConnected.value = result
			}
		}

		private suspend fun disconnect() = runInterruptible(Dispatchers.IO + NonCancellable) {
			isConnected.value = false
			connection.close()
		}
	}
}