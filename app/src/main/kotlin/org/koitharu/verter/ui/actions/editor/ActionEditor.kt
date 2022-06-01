package org.koitharu.verter.ui.actions.editor

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import org.koitharu.verter.R

private const val NO_ID = 0

@Composable
fun ActionEditor(navController: NavController, actionId: Int) {
	val viewModel = hiltViewModel<ActionEditorViewModel>()
	viewModel.init(actionId)
	Scaffold(
		modifier = Modifier.imePadding(),
		topBar = {
			SmallTopAppBar(
				title = {
					Text(
						stringResource(
							if (actionId == NO_ID) {
								R.string.add_action
							} else {
								R.string.edit_action
							}
						)
					)
				},
				navigationIcon = {
					IconButton(
						onClick = { navController.popBackStack() },
						content = { Icon(Icons.Default.Close, "Close") },
					)
				}
			)
		},
		content = { padding ->
			Column(
				modifier = Modifier.fillMaxWidth()
					.fillMaxHeight()
					.padding(padding)
					.scrollable(rememberScrollState(0), Orientation.Vertical)
			) {
				val fieldModifier = Modifier.fillMaxWidth().padding(
					vertical = 6.dp,
					horizontal = 12.dp,
				)
				val isBusy by viewModel.isBusy
				val name by viewModel.name.collectAsState()
				val keyboardController = LocalSoftwareKeyboardController.current
				if (isBusy) {
					keyboardController?.hide()
				}
				OutlinedTextField(
					modifier = fieldModifier,
					value = name,
					enabled = !isBusy,
					onValueChange = { viewModel.name.value = it.trim() },
					label = { Text(stringResource(R.string.name)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.Sentences,
						keyboardType = KeyboardType.Text,
						autoCorrect = true,
						imeAction = ImeAction.Next,
					),
					singleLine = true,
				)
				CommandEdit(
					modifier = fieldModifier,
					viewModel = viewModel,
					enabled = !isBusy,
					keyboardController = keyboardController,
				)
				Spacer(
					modifier = Modifier.weight(1f).padding(top = 12.dp)
				)
				val isDoneEnabled = !isBusy && name.isNotEmpty()
				Button(
					onClick = {
						viewModel.save()
					},
					modifier = Modifier.fillMaxWidth().padding(12.dp),
					enabled = isDoneEnabled,
				) {
					Text(stringResource(R.string.save))
				}
			}
		}
	)
}

@Composable
private fun CommandEdit(
	modifier: Modifier,
	viewModel: ActionEditorViewModel,
	enabled: Boolean,
	keyboardController: SoftwareKeyboardController?,
) {
	val cmdline by viewModel.cmdline.collectAsState()
	val completion by viewModel.completion.collectAsState(emptyList(), Dispatchers.Default)
	var isExpanded by remember { mutableStateOf(false) }

	Box(modifier) {
		OutlinedTextField(
			modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
				isExpanded = focusState.isFocused
			},
			value = cmdline,
			enabled = enabled,
			onValueChange = { viewModel.cmdline.value = it },
			label = { Text(stringResource(R.string.command)) },
			keyboardOptions = KeyboardOptions(
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Text,
				autoCorrect = false,
				imeAction = ImeAction.Done,
			),
			keyboardActions = KeyboardActions {
				keyboardController?.hide()
			},
			singleLine = true,
		)
		DropdownMenu(
			modifier = Modifier.heightIn(max = 320.dp)
				.scrollable(rememberScrollState(), Orientation.Vertical),
			expanded = isExpanded && completion.isNotEmpty(),
			onDismissRequest = { },
			properties = PopupProperties(
				focusable = false,
				dismissOnBackPress = true,
				dismissOnClickOutside = true,
			),
		) {
			completion.forEach { item ->
				DropdownMenuItem(
					text = {
						Text(text = item)
					},
					onClick = {
						viewModel.applySuggestion(item)
					}
				)
			}
		}
	}
}