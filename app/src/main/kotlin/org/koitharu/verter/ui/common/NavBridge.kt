package org.koitharu.verter.ui.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavBridge @Inject constructor() {

	private val targetsSharedFlow = MutableSharedFlow<Target>(extraBufferCapacity = 1)
	private val backNavSharedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

	fun navigateTo(target: Target) {
		targetsSharedFlow.tryEmit(target)
	}

	fun popBackStack() {
		backNavSharedFlow.tryEmit(Unit)
	}

	@Composable
	@SuppressLint("ComposableNaming")
	fun bind(controller: NavController) {
		LaunchedEffect("navigation") {
			targetsSharedFlow.onEach {
				controller.navigate(it.route)
			}.launchIn(this)
			backNavSharedFlow.onEach {
				controller.popBackStack()
			}.launchIn(this)
		}
	}

	sealed class Target(val route: String) {
		object Main : Target(MAIN)
		object DeviceEditor : Target("add_device")
		class ActionEditor(actionId: Int = 0) : Target("add_action/$actionId")

		companion object {

			const val MAIN = "main"
			const val DEVICE_EDITOR = "add_device"
			const val ACTION_EDITOR = "add_action/{action_id}"
		}
	}
}