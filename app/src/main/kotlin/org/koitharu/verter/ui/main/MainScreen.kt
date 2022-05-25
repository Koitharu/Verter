package org.koitharu.verter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.verter.R
import org.koitharu.verter.ui.actions.ActionsScreen
import org.koitharu.verter.ui.common.EmptyState
import org.koitharu.verter.ui.main.MainViewModel
import org.koitharu.verter.ui.main.Screens
import org.koitharu.verter.ui.media.MediaScreen

@Composable
fun MainScreen() {
	val viewModel = hiltViewModel<MainViewModel>()
	val isNoDevices by viewModel.isNoDevices.collectAsState(false)
	if (isNoDevices) {
		EmptyState(
			icon = painterResource(R.drawable.ic_device_link),
			text = stringResource(R.string.no_devices_added),
			buttonContent = {
				Icon(Icons.Default.Add, null)
				Text(stringResource(R.string.add_device))
			},
			onButtonClick = {
				viewModel.onAddDeviceClick()
			}
		)
	} else {
		MainContent(viewModel)
	}
}

@Composable
private fun MainContent(
	viewModel: MainViewModel
) {
	val childNavController = rememberNavController()
	val snackbarState = remember { SnackbarHostState() }
	Scaffold(
		topBar = {
			TopBar(viewModel, snackbarState)
		},
		snackbarHost = {
			SnackbarHost(hostState = snackbarState)
		},
		bottomBar = {
			NavigationBar {
				val navBackStackEntry by childNavController.currentBackStackEntryAsState()
				val currentDestination = navBackStackEntry?.destination
				Screens.values().forEach { screen ->
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(screen.iconId),
								contentDescription = stringResource(screen.resourceId)
							)
						},
						label = { Text(stringResource(screen.resourceId)) },
						selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
						onClick = {
							childNavController.navigate(screen.route) {
								popUpTo(childNavController.graph.findStartDestination().id) {
									saveState = true
								}
								launchSingleTop = true
								restoreState = true
							}
						}
					)
				}
			}
		}
	) { innerPadding ->
		NavHost(childNavController, startDestination = Screens.ACTIONS.route, Modifier.padding(innerPadding)) {
			composable(Screens.ACTIONS.route) { ActionsScreen(snackbarState) }
			composable(Screens.MEDIA.route) { MediaScreen(snackbarState) }
		}
	}
}

@Composable
private fun TopBar(
	viewModel: MainViewModel,
	snackbarHostState: SnackbarHostState,
) {
	var isExpanded by remember { mutableStateOf(false) }
	val isBusy by viewModel.isConnecting.collectAsState()
	val selectedItem by viewModel.selectedDevice.collectAsState()
	val deviceList by viewModel.devices.collectAsState()
	LaunchedEffect("errors") {
		viewModel.errors.onEach {
			snackbarHostState.showSnackbar(it.message.orEmpty())
		}.launchIn(this)
	}
	SmallTopAppBar(
		title = {
			Column {
				Row(
					modifier = Modifier.fillMaxWidth().clickable(
						interactionSource = MutableInteractionSource(),
						indication = null,
						onClick = { isExpanded = true }
					)
				) {
					Icon(
						painter = when {
							isBusy -> painterResource(R.drawable.ic_connection_pending)
							selectedItem == null -> painterResource(R.drawable.ic_connection_none)
							else -> painterResource(R.drawable.ic_connection_ok)
						},
						modifier = Modifier.padding(end = 8.dp),
						contentDescription = null,
					)
					Text(
						text = selectedItem?.displayName ?: stringResource(R.string.no_device_selected),
						color = if (selectedItem != null) {
							Color.Unspecified
						} else {
							MaterialTheme.colorScheme.tertiary
						},
					)
				}
				DevicesDropDownList(
					expanded = isExpanded,
					list = deviceList.orEmpty(),
					onAddClick = { viewModel.onAddDeviceClick() },
					onDismissRequest = { isExpanded = false },
					onItemSelected = { viewModel.switchDevice(it) },
				)
			}
		},
		actions = {
			if (isBusy) {
				CircularProgressIndicator()
			} else {
				IconButton(
					onClick = { isExpanded = !isExpanded },
				) {
					Icon(Icons.Filled.ArrowDropDown, "dropdown")
				}
			}
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.secondaryContainer,
		)
	)
}