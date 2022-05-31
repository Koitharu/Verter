package org.koitharu.verter.ui.actions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.verter.R
import org.koitharu.verter.core.actions.RemoteAction
import org.koitharu.verter.ui.common.EmptyState
import org.koitharu.verter.util.isScrollingUp
import org.koitharu.verter.util.lineCount

@Composable
fun ActionsScreen(snackbarHostState: SnackbarHostState) {
	val viewModel: ActionsViewModel = hiltViewModel()
	var alertDialogText by remember { mutableStateOf("") }
	LaunchedEffect("actions_result") {
		viewModel.actionResult.onEach {
			if (it.lineCount() > 2) {
				alertDialogText = it
			} else {
				snackbarHostState.showSnackbar(it)
			}
		}.launchIn(this)
	}
	if (alertDialogText.isNotEmpty()) {
		AlertDialog(
			onDismissRequest = {
				alertDialogText = ""
			},
			text = {
				Text(
					text = alertDialogText,
					modifier = Modifier.verticalScroll(rememberScrollState())
				)
			},
			confirmButton = {
				TextButton(
					onClick = { alertDialogText = "" }
				) {
					Text(stringResource(R.string.close))
				}
			}
		)
	}
	val actions by viewModel.actions.collectAsState()
	when (actions?.isEmpty()) {
		null -> {
			Box(
				modifier = Modifier.fillMaxWidth().fillMaxHeight(),
				contentAlignment = Alignment.Center,
			) {
				CircularProgressIndicator()
			}
		}
		true -> {
			EmptyState(
				icon = painterResource(R.drawable.ic_actions),
				text = stringResource(R.string.actions_placeholder),
				buttonContent = {
					Text(stringResource(R.string.add_action))
				},
				onButtonClick = { viewModel.onAddClick() }
			)
		}
		false -> {
			Box(
				modifier = Modifier.fillMaxWidth().fillMaxHeight(),
			) {
				val listState = rememberLazyListState()
				val runningActions by viewModel.runningActions.collectAsState()
				LazyColumn(
					state = listState,
					modifier = Modifier.fillMaxWidth().fillMaxHeight(),
				) {
					itemsIndexed(actions.orEmpty()) { position, item ->
						val isExecuting = item.id in runningActions
						ActionItem(item, isExecuting, position, viewModel)
					}
				}
				AnimatedVisibility(
					modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
					visible = listState.isScrollingUp(),
					enter = slideInVertically { it * 2 },
					exit = slideOutVertically { it * 2 },
				) {
					FloatingActionButton(
						onClick = { viewModel.onAddClick() },
					) {
						Icon(Icons.Default.Add, contentDescription = null)
					}
				}
			}
		}
	}
}

@Composable
private fun ActionItem(
	item: RemoteAction,
	isExecuting: Boolean,
	index: Int,
	viewModel: ActionsViewModel
) {
	Card(
		modifier = Modifier.padding(
			start = 12.dp,
			end = 12.dp,
			top = if (index == 0) 14.dp else 0.dp,
			bottom = 14.dp,
		).fillMaxWidth(),
	) {
		Text(
			text = item.name,
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier.fillMaxWidth()
				.padding(start = 12.dp, end = 12.dp, top = 8.dp),
		)
		Text(
			text = item.cmdline,
			modifier = Modifier.fillMaxWidth()
				.padding(start = 12.dp, end = 12.dp, top = 4.dp),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.secondary,
		)
		TextButton(
			onClick = { viewModel.onActionClick(item) },
			modifier = Modifier.align(Alignment.End)
				.padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 8.dp),
			enabled = !isExecuting,
		) {
			Text(stringResource(R.string.execute))
		}
	}
}