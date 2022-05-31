package org.koitharu.verter.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koitharu.verter.core.devices.RemoteDevice
import org.koitharu.verter.interactor.DeviceInteractor
import org.koitharu.verter.ui.common.NavBridge

@HiltViewModel
class DeviceEditorViewModel @Inject constructor(
	private val interactor: DeviceInteractor,
	private val navBridge: NavBridge,
) : ViewModel() {

	val isLoading = MutableStateFlow(false)

	fun onAddDeviceClick(
		address: String,
		port: Int,
		user: String,
		password: String,
		alias: String,
	) {
		viewModelScope.launch {
			isLoading.value = true
			val device = RemoteDevice(
				id = 0,
				address = address,
				port = port,
				user = user,
				password = password,
				alias = alias.trim().takeUnless { it.isEmpty() }
			)
			interactor.addDevice(device)
			navBridge.popBackStack()
		}
	}
}