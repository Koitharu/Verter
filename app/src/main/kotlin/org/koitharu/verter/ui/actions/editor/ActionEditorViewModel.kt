package org.koitharu.verter.ui.actions.editor

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
	val name = MutableStateFlow("")
	val cmdline = MutableStateFlow(TextFieldValue(text = "", selection = TextRange.Zero))
	val completion = cmdline
		.map {
			it.text.getWordAt(it.selection.end)
		}.distinctUntilChanged()
		.mapLatest {
			if (it.length < 3) {
				emptyList()
			} else {
				delay(500)
				interactor.getCompletion(it).orEmpty()
			}
		}.distinctUntilChanged()

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
			cmdline.value = TextFieldValue(text = action.cmdline, selection = TextRange(action.cmdline.length))
			isBusy.value = false
		}
	}

	fun applySuggestion(text: String) {
		val cmd = cmdline.value
		val position = cmd.selection.end
		val wordStart = cmd.text.lastIndexOf(' ', maxOf(position - 1, 0))
		val wordEnd = cmd.text.indexOf(' ', position)
		val prefix = if (wordStart < 0) "" else cmd.text.substring(0, wordStart)
		val suffix = if (wordEnd < 0) "" else cmd.text.substring(wordEnd + 1)
		val newValue = "$prefix $text $suffix".trimStart()
		cmdline.value = TextFieldValue(
			text = newValue,
			selection = TextRange(newValue.length - suffix.length),
		)
	}

	fun save() {
		viewModelScope.launch {
			isBusy.value = true
			if (actionId == NO_ID) {
				interactor.createAction(
					name = name.value,
					cmdline = cmdline.value.text,
				)
			} else {
				interactor.updateAction(
					id = actionId,
					name = name.value,
					cmdline = cmdline.value.text,
				)
			}
			navBridge.popBackStack()
		}
	}
}

private fun String.getWordAt(position: Int): String {
	if (position <= 0) {
		return ""
	}
	val index = lastIndexOf(' ', position - 1).coerceAtLeast(0)
	return substring(index, position)
}