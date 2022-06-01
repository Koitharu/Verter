package org.koitharu.verter.ui.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koitharu.verter.core.actions.RemoteAction
import org.koitharu.verter.interactor.ActionsInteractor
import org.koitharu.verter.ui.common.NavBridge
import javax.inject.Inject

@HiltViewModel
class ActionsViewModel @Inject constructor(
	private val interactor: ActionsInteractor,
	private val navBridge: NavBridge,
) : ViewModel() {

	val actions = interactor.observeActions()
// 		.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Eagerly, null)

	val runningActions = MutableStateFlow(emptySet<Int>())

	val selectedActions = MutableStateFlow(emptySet<Int>())

	val actionResult = MutableSharedFlow<String>(extraBufferCapacity = 1)

	fun onAddClick() {
		navBridge.navigateTo(NavBridge.Target.ActionEditor())
	}

	fun onActionClick(action: RemoteAction) {
		viewModelScope.launch {
			runningActions.update { it + action.id }
			runCatching {
				interactor.executeAction(action)
			}.onSuccess { result ->
				actionResult.emit(result)
			}.onFailure { error ->
				val msg = error.message
				if (msg != null) actionResult.emit(msg)
			}
			runningActions.update { it - action.id }
		}
	}

	fun onDeleteSelectedClick() {
		viewModelScope.launch {
			val selectedItems = selectedActions.value
			selectedActions.value = emptySet()
			interactor.deleteActions(selectedItems)
		}
	}

	fun onEditActionClick() {
		val actionId = selectedActions.value.singleOrNull()
		selectedActions.value = emptySet()
		navBridge.navigateTo(NavBridge.Target.ActionEditor(actionId ?: return))
	}

	fun clearSelection() {
		selectedActions.value = emptySet()
	}

	fun onActionCardClick(item: RemoteAction) {
		toggleSelection(item, canStart = false)
	}

	fun onActionCardLongClick(item: RemoteAction) {
		toggleSelection(item, canStart = true)
	}

	private fun toggleSelection(item: RemoteAction, canStart: Boolean) {
		val selected = selectedActions.value
		if (selected.isEmpty()) {
			if (canStart) {
				selectedActions.value = setOf(item.id)
			}
			return
		}
		if (item.id in selected) {
			selectedActions.value = selected - item.id
		} else {
			selectedActions.value = selected + item.id
		}
	}
}