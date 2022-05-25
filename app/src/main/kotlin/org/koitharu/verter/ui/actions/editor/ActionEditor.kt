package org.koitharu.verter.ui.actions.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.koitharu.verter.R

@Composable
fun ActionEditor(navController: NavController) {
	val viewModel = hiltViewModel<ActionEditorViewModel>()
	Scaffold(
		modifier = Modifier.imePadding(),
		topBar = {
			SmallTopAppBar(
				title = {
					Text(stringResource(R.string.add_action))
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
					.scrollable(ScrollState(0), Orientation.Vertical)
			) {
				val fieldModifier = Modifier.fillMaxWidth().padding(
					vertical = 6.dp,
					horizontal = 12.dp,
				)
				var name by viewModel.name
				var cmdline by viewModel.cmdline

				OutlinedTextField(
					modifier = fieldModifier,
					value = name,
					onValueChange = { name = it.trim() },
					label = { Text(stringResource(R.string.name)) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = cmdline,
					onValueChange = { cmdline = it },
					label = { Text(stringResource(R.string.command)) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
					singleLine = true,
				)
				Spacer(
					modifier = Modifier.weight(1f).padding(top = 12.dp)
				)
				Button(
					onClick = {
						viewModel.save()
					},
					modifier = Modifier.fillMaxWidth().padding(12.dp),
				) {
					Text(stringResource(R.string.save))
				}
			}
		}
	)
}