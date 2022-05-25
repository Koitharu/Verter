package org.koitharu.verter.ui.actions.editor

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.koitharu.verter.interactor.ActionsInteractor
import org.koitharu.verter.ui.common.NavBridge

@HiltViewModel
class ActionEditorViewModel @Inject constructor(
	private val interactor: ActionsInteractor,
	private val navBridge: NavBridge,
) : ViewModel() {

	val name = mutableStateOf("")
	val cmdline = mutableStateOf("")

	fun save() {
		viewModelScope.launch {
			interactor.createAction(
				name = name.value,
				cmdline = cmdline.value,
			)
			navBridge.popBackStack()
		}
	}
}