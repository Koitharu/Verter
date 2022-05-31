package org.koitharu.verter.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koitharu.verter.core.devices.RemoteDevice
import org.koitharu.verter.core.ssh.awaitConnection
import org.koitharu.verter.interactor.DeviceInteractor
import org.koitharu.verter.ui.common.NavBridge
import org.koitharu.verter.util.ErrorHandler
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val deviceInteractor: DeviceInteractor,
	private val navBridge: NavBridge,
) : ViewModel() {

	private var connectionJob: Job

	private val errorHandler = ErrorHandler()

	val devices = deviceInteractor.observeDevices()
		.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	val isNoDevices = devices.map {
		it?.isEmpty() == true
	}

	val isConnectedDevice = deviceInteractor.getActiveConnectionAsFlow().map { it != null }

	val selectedDevice = deviceInteractor.getCurrentDeviceAsFlow()

	val isConnecting = MutableStateFlow(false)

	val errors: SharedFlow<Throwable>
		get() = errorHandler.errors

	init {
		connectionJob = viewModelScope.launch(errorHandler) {
			isConnecting.value = true
			try {
				val device = deviceInteractor.getLastConnectedDevice() ?: return@launch
				deviceInteractor.obtainConnection(device).awaitConnection()
			} finally {
				isConnecting.value = false
			}
		}
	}

	fun onAddDeviceClick() {
		navBridge.navigateTo(NavBridge.Target.DEVICE_EDITOR)
	}

	fun switchDevice(device: RemoteDevice) {
		val prevJob = connectionJob
		connectionJob = viewModelScope.launch(errorHandler) {
			prevJob.cancelAndJoin()
			isConnecting.value = true
			try {
				deviceInteractor.obtainConnection(device).awaitConnection()
				deviceInteractor.markDeviceConnected(device)
			} finally {
				isConnecting.value = false
			}
		}
	}
}