package org.koitharu.verter.ui.devices

import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.koitharu.verter.R

@Composable
fun DeviceEditorScreen(
	navController: NavController,
) {
	val viewModel = hiltViewModel<DeviceEditorViewModel>()
	Scaffold(
		modifier = Modifier.imePadding(),
		topBar = {
			SmallTopAppBar(
				title = {
					Text(stringResource(R.string.add_device))
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
				val keyboardController = LocalSoftwareKeyboardController.current
				var address by remember { mutableStateOf("") }
				var port by remember { mutableStateOf(22) }
				var user by remember { mutableStateOf("") }
				var password by remember { mutableStateOf("") }
				var alias by remember { mutableStateOf("") }

				OutlinedTextField(
					modifier = fieldModifier,
					value = address,
					onValueChange = { address = it.trim() },
					label = { Text(stringResource(R.string.address)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.Uri,
						autoCorrect = false,
						imeAction = ImeAction.Next,
					),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = port.toString(),
					onValueChange = { port = it.filter { x -> x.isDigit() }.toIntOrNull() ?: 0 },
					label = { Text(stringResource(R.string.port)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.Number,
						autoCorrect = false,
						imeAction = ImeAction.Next,
					),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = user,
					onValueChange = { user = it.trim() },
					label = { Text(stringResource(R.string.user)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.Text,
						autoCorrect = false,
						imeAction = ImeAction.Next,
					),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = password,
					onValueChange = { password = it },
					label = { Text(stringResource(R.string.password)) },
					visualTransformation = PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.Password,
						autoCorrect = false,
						imeAction = ImeAction.Next,
					),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = alias,
					onValueChange = { alias = it },
					label = { Text(stringResource(R.string.alias)) },
					keyboardOptions = KeyboardOptions(
						capitalization = KeyboardCapitalization.Sentences,
						keyboardType = KeyboardType.Text,
						autoCorrect = true,
						imeAction = ImeAction.Done,
					),
					keyboardActions = KeyboardActions {
						keyboardController?.hide()
					},
					singleLine = true,
				)
				Spacer(
					modifier = Modifier.weight(1f).padding(top = 12.dp)
				)
				val isLoading by viewModel.isLoading.collectAsState()
				val isDoneEnabled = !isLoading &&
					address.isNotEmpty() &&
					port > 0 &&
					user.isNotEmpty() &&
					password.isNotEmpty()
				Button(
					onClick = {
						viewModel.onAddDeviceClick(
							address = address,
							port = port,
							user = user,
							password = password,
							alias = alias,
						)
					},
					enabled = isDoneEnabled,
					modifier = Modifier.fillMaxWidth().padding(12.dp),
				) {
					Text(stringResource(R.string.save))
				}
			}
		}
	)
}