package org.koitharu.verter.core.ssh

import androidx.annotation.AnyThread
import org.koitharu.verter.core.devices.RemoteDevice

interface OnConnectionLostListener {

	@AnyThread
	fun onConnectionLost(device: RemoteDevice, reason: Throwable?)
}