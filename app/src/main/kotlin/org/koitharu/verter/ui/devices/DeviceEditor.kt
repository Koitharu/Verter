package org.koitharu.verter.ui.devices

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koitharu.verter.R
import org.koitharu.verter.core.devices.RemoteDevice
import org.koitharu.verter.interactor.DeviceInteractor

@Composable
fun DeviceEditor(
	navController: NavController,
	interactor: DeviceInteractor,
) {
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
					.scrollable(ScrollState(0), Orientation.Vertical)
			) {
				val coroutineScope = rememberCoroutineScope()
				val fieldModifier = Modifier.fillMaxWidth().padding(
					vertical = 6.dp,
					horizontal = 12.dp,
				)
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
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = port.toString(),
					onValueChange = { port = it.filter { x -> x.isDigit() }.toIntOrNull() ?: 0 },
					label = { Text(stringResource(R.string.port)) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = user,
					onValueChange = { user = it.trim() },
					label = { Text(stringResource(R.string.user)) },
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = password,
					onValueChange = { password = it },
					label = { Text(stringResource(R.string.password)) },
					visualTransformation = PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
					singleLine = true,
				)
				OutlinedTextField(
					modifier = fieldModifier,
					value = alias,
					onValueChange = { alias = it },
					label = { Text(stringResource(R.string.alias)) },
					singleLine = true,
				)
				Spacer(
					modifier = Modifier.weight(1f).padding(top = 12.dp)
				)
				Button(
					onClick = {
						coroutineScope.launch {
							val device = RemoteDevice(
								id = 0,
								address = address,
								port = port,
								user = user,
								password = password,
								alias = alias.trim().takeUnless { it.isEmpty() }
							)
							interactor.addDevice(device)
							navController.popBackStack()
						}
					},
					modifier = Modifier.fillMaxWidth().padding(12.dp),
				) {
					Text(stringResource(R.string.save))
				}
			}
		}
	)
}