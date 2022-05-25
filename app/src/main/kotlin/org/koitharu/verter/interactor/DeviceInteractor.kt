package org.koitharu.verter.interactor

import kotlinx.coroutines.flow.*
import org.koitharu.verter.core.db.AppDatabase
import org.koitharu.verter.core.db.entity.toDevice
import org.koitharu.verter.core.db.entity.toEntity
import org.koitharu.verter.core.devices.RemoteDevice
import org.koitharu.verter.core.ssh.RemoteDeviceSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInteractor @Inject constructor(
	private val database: AppDatabase,
) {

	private var sessionState = MutableStateFlow<RemoteDeviceSession?>(null)
	private val selectedDevice = MutableStateFlow<RemoteDevice?>(null)

	val device: StateFlow<RemoteDevice?>
		get() = selectedDevice

	suspend fun setDevice(device: RemoteDevice?): Boolean {
		sessionState.value?.close()
		sessionState.value = device?.let { RemoteDeviceSession(it) }
		val result = sessionState.value?.connect() ?: true
		selectedDevice.value = device?.takeIf { result }
		return result
	}

	suspend fun execute(cmdline: String): String? {
		val session = getConnectedSession().first()
		return session.execute(cmdline)
	}

	fun executeContinuously(cmdline: String): Flow<String> {
		return getConnectedSession().flatMapLatest { it.executeContinuously(cmdline) }
	}

	suspend fun getFileContent(path: String): ByteArray {
		val session = getConnectedSession().first()
		return session.getFileContent(path)
	}

	suspend fun addDevice(device: RemoteDevice) {
		database.devicesDao.insert(device.toEntity())
	}

	fun observeDevices(): Flow<List<RemoteDevice>> {
		return database.devicesDao.observeAll().map { list ->
			list.map { x -> x.toDevice() }
		}
	}

	private fun getConnectedSession(): Flow<RemoteDeviceSession> {
		return sessionState.flatMapLatest { it?.getConnectedAsFlow() ?: emptyFlow() }.filterNotNull()
	}
}