package org.koitharu.verter.ui.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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
		.stateIn(viewModelScope + Dispatchers.Default, SharingStarted.Eagerly, null)

	val runningActions = MutableStateFlow(emptySet<Int>())

	val actionResult = MutableSharedFlow<String>(extraBufferCapacity = 1)

	fun onAddClick() {
		navBridge.navigateTo(NavBridge.Target.ACTION_EDITOR)
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
}