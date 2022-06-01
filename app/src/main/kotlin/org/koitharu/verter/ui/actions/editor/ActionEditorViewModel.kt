package org.koitharu.verter.ui.actions.editor

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.koitharu.verter.interactor.ActionsInteractor
import org.koitharu.verter.ui.common.NavBridge
import javax.inject.Inject

private const val NO_ID = 0

@HiltViewModel
class ActionEditorViewModel @Inject constructor(
	private val interactor: ActionsInteractor,
	private val navBridge: NavBridge,
) : ViewModel() {

	private var isInitialized = false
	private var actionId: Int = NO_ID
	val isBusy = mutableStateOf(true)
	val name = mutableStateOf("")
	val cmdline = mutableStateOf("")

	fun init(actionId: Int) {
		if (isInitialized) {
			return
		}
		this.actionId = actionId
		isInitialized = true
		if (actionId == NO_ID) {
			isBusy.value = false
			return
		}
		viewModelScope.launch {
			val action = interactor.getAction(actionId)
			name.value = action.name
			cmdline.value = action.cmdline
			isBusy.value = false
		}
	}

	fun save() {
		viewModelScope.launch {
			isBusy.value = true
			if (actionId == NO_ID) {
				interactor.createAction(
					name = name.value,
					cmdline = cmdline.value,
				)
			} else {
				interactor.updateAction(
					id = actionId,
					name = name.value,
					cmdline = cmdline.value,
				)
			}
			navBridge.popBackStack()
		}
	}
}