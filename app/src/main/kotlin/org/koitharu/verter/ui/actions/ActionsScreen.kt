package org.koitharu.verter.ui.actions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import org.koitharu.verter.util.BackPressedEffect
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
	val actions by viewModel.actions.collectAsState(null)
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
				val selectedActions by viewModel.selectedActions.collectAsState()
				LazyColumn(
					state = listState,
					modifier = Modifier.fillMaxWidth().fillMaxHeight(),
				) {
					itemsIndexed(actions.orEmpty()) { position, item ->
						val isExecuting = item.id in runningActions
						val isSelected = item.id in selectedActions
						ActionItem(
							modifier = Modifier.padding(
								bottom = if (selectedActions.isNotEmpty() && position == (actions?.lastIndex ?: -1)) {
									92.dp
								} else {
									0.dp
								}
							),
							item = item,
							isExecuting = isExecuting,
							isSelected = isSelected,
							index = position,
							viewModel = viewModel
						)
					}
				}
				AnimatedVisibility(
					modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
					visible = selectedActions.isEmpty() && listState.isScrollingUp(),
					enter = slideInVertically { it * 2 },
					exit = slideOutVertically { it * 2 },
				) {
					FloatingActionButton(
						onClick = { viewModel.onAddClick() },
					) {
						Icon(Icons.Default.Add, contentDescription = null)
					}
				}
				if (selectedActions.isNotEmpty()) {
					SelectionActionMode(
						modifier = Modifier.align(Alignment.BottomCenter),
						selection = selectedActions,
						total = actions?.size ?: 0,
						viewModel = viewModel
					)
				}
			}
		}
	}
}

@Composable
private fun ActionItem(
	modifier: Modifier,
	item: RemoteAction,
	isExecuting: Boolean,
	isSelected: Boolean,
	index: Int,
	viewModel: ActionsViewModel,
) {
	Card(
		modifier = Modifier.padding(
			start = 12.dp,
			end = 12.dp,
			top = if (index == 0) 14.dp else 0.dp,
			bottom = 14.dp,
		).combinedClickable(
			indication = if (isSelected) LocalIndication.current else null,
			interactionSource = remember { MutableInteractionSource() },
			onClick = { viewModel.onActionCardClick(item) },
			onLongClick = { viewModel.onActionCardLongClick(item) },
		).fillMaxWidth().then(modifier),
		border = if (isSelected) {
			CardDefaults.outlinedCardBorder(true)
		} else null,
		colors = if (isSelected) {
			CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
		} else {
			CardDefaults.cardColors()
		},
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

@Composable
private fun SelectionActionMode(
	modifier: Modifier,
	selection: Set<Int>,
	total: Int,
	viewModel: ActionsViewModel,
) {
	ElevatedCard(
		modifier = Modifier.padding(24.dp).fillMaxWidth().then(modifier),
	) {
		Row(
			modifier = Modifier.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				text = "%d/%d".format(selection.size, total),
				style = MaterialTheme.typography.headlineSmall,
			)
			Spacer(Modifier.weight(1f))
			if (selection.size == 1) {
				IconButton(
					onClick = { viewModel.onEditActionClick() },
				) {
					Icon(Icons.Outlined.Edit, null)
				}
			}
			IconButton(
				onClick = { viewModel.onDeleteSelectedClick() },
			) {
				Icon(Icons.Outlined.Delete, null)
			}
		}
	}
	BackPressedEffect {
		viewModel.clearSelection()
	}
}