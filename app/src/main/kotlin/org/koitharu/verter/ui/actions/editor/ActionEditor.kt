package org.koitharu.verter.ui.actions.editor

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
				var name by viewModel.name
				var cmdline by viewModel.cmdline

				OutlinedTextField(
					modifier = fieldModifier,
					value = name,
					enabled = !isBusy,
					onValueChange = { name = it.trim() },
					label = { Text(stringResource(R.string.name)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.Sentences,
						keyboardType = KeyboardType.Text,
						autoCorrect = true,
						imeAction = ImeAction.Next,
					),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = cmdline,
					enabled = !isBusy,
					onValueChange = { cmdline = it },
					label = { Text(stringResource(R.string.command)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.Text,
						autoCorrect = false,
						imeAction = ImeAction.Done,
					),
					singleLine = true,
				)
				Spacer(
					modifier = Modifier.weight(1f).padding(top = 12.dp)
				)
				val isDoneEnabled = !isBusy && name.isNotEmpty() && cmdline.isNotEmpty()
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