package org.koitharu.verter.ui.actions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.verter.R

@Composable
fun ActionsScreen(snackbarHostState: SnackbarHostState) {
	val viewModel: ActionsViewModel = hiltViewModel()
	LaunchedEffect("actions_result") {
		viewModel.actionResult.onEach {
			snackbarHostState.showSnackbar(it)
		}.launchIn(this)
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
			Column(
				modifier = Modifier.fillMaxWidth().fillMaxHeight(),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Icon(painterResource(R.drawable.ic_actions), contentDescription = null)
				Text(stringResource(R.string.actions_placeholder))
				Button(
					onClick = { viewModel.onAddClick() },
				) {
					Text(stringResource(R.string.add_action))
				}
			}
		}
		false -> {
			Column(
				modifier = Modifier.fillMaxWidth().fillMaxHeight(),
			) {
				val listState = rememberLazyListState()
				val runningActions by viewModel.runningActions.collectAsState()
				LazyColumn(
					state = listState,
					modifier = Modifier.fillMaxWidth().weight(1f)
				) {
					itemsIndexed(actions.orEmpty()) { position, item ->
						if (position > 0) {
							Divider()
						}
						val isExecuting = item.id in runningActions
						Box(
							modifier = Modifier.fillParentMaxWidth().clickable(
								interactionSource = MutableInteractionSource(),
								indication = rememberRipple(bounded = true),
								onClick = { viewModel.onActionClick(item) }
							).heightIn(min = 54.dp)
								.padding(horizontal = 12.dp, vertical = 8.dp),
						) {
							Column(
								modifier = Modifier.align(Alignment.CenterStart),
							) {
								Text(
									text = item.name,
									style = MaterialTheme.typography.bodyLarge,
								)
								Text(
									text = item.cmdline,
									modifier = Modifier.padding(top = 2.dp),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.secondary,
								)
							}
							if (isExecuting) {
								CircularProgressIndicator(
									modifier = Modifier.align(Alignment.CenterEnd),
								)
							}
						}
					}
				}
				Button(
					onClick = { viewModel.onAddClick() },
					modifier = Modifier.fillMaxWidth().padding(12.dp),
				) {
					Icon(Icons.Default.Add, contentDescription = null)
					Text(stringResource(R.string.add_action))
				}
			}
		}
	}
}