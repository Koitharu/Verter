package org.koitharu.verter.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.verter.R
import org.koitharu.verter.core.media.PlayerState
import org.koitharu.verter.ui.common.EmptyState
import org.koitharu.verter.util.formatTimeSeconds
import kotlin.math.roundToInt

@Composable
fun MediaScreen(
	snackbarHostState: SnackbarHostState,
) {
	val viewModel = hiltViewModel<MediaViewModel>()
	LaunchedEffect("errors") {
		viewModel.errors.onEach {
			snackbarHostState.showSnackbar(it.message.orEmpty())
		}.launchIn(this)
	}
	val isAvailable by viewModel.isAvailable.collectAsState()
	if (isAvailable) {
		MediaController(viewModel)
	} else {
		EmptyState(
			icon = null,
			text = stringResource(R.string.playerctl_not_available),
			buttonContent = {
				Text(stringResource(R.string.help))
			}
		)
	}
}

@Composable
private fun MediaController(viewModel: MediaViewModel) {
	val metadata by viewModel.metadata.collectAsState()
	val state by viewModel.playerState.collectAsState()
	val cover by viewModel.coverArt.collectAsState()
	Column(
		modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(12.dp),
	) {
		if (metadata != null) {
			Column(
				modifier = Modifier.fillMaxWidth().weight(1f),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				if (cover == null) {
					Image(
						painter = painterResource(R.drawable.ic_vinyl),
						contentDescription = null,
						modifier = Modifier.size(240.dp)
							.padding(bottom = 16.dp)
							.aspectRatio(1f),
						contentScale = ContentScale.Inside,
						alpha = 0.6f,
					)
				} else cover?.let {
					Image(
						bitmap = it.asImageBitmap(),
						contentDescription = null,
						modifier = Modifier.size(240.dp)
							.padding(bottom = 16.dp)
							.aspectRatio(1f),
						contentScale = ContentScale.Inside,
					)
				}
				Text(
					text = metadata?.title.orEmpty(),
					modifier = Modifier.fillMaxWidth(),
					style = MaterialTheme.typography.titleLarge,
					textAlign = TextAlign.Center,
				)
				Text(
					text = metadata?.artist.orEmpty(),
					modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
					style = MaterialTheme.typography.titleMedium,
					textAlign = TextAlign.Center,
				)
				Text(
					text = metadata?.album.orEmpty(),
					modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
					style = MaterialTheme.typography.titleSmall,
					textAlign = TextAlign.Center,
				)
				val length = metadata?.length ?: 0
				val position = metadata?.position ?: 0
				if (length > 0) {
					Slider(
						value = position.toFloat(),
						onValueChange = { viewModel.onPositionChanged(it.roundToInt()) },
						modifier = Modifier.fillMaxWidth().padding(end = 16.dp, start = 16.dp, top = 12.dp),
						valueRange = 0f..length.toFloat(),
						steps = 0,
					)
					Row(
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(end = 16.dp, start = 16.dp, top = 4.dp)
					) {
						Text(position.formatTimeSeconds())
						Spacer(modifier = Modifier.weight(1f))
						Text(length.formatTimeSeconds())
					}
				}
			}
		} else {
			EmptyState(
				modifier = Modifier.fillMaxWidth().weight(1f),
				icon = painterResource(R.drawable.ic_media_player),
				text = stringResource(R.string.no_players),
			)
		}
		Row(
			modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
			horizontalArrangement = Arrangement.SpaceAround,
			verticalAlignment = Alignment.CenterVertically,
		) {
			IconButton(
				onClick = { viewModel.onPreviousClick() },
			) {
				Icon(painterResource(R.drawable.ic_skip_previous), "previous")
			}
			IconButton(
				onClick = { viewModel.onRewind10Click() },
			) {
				Icon(painterResource(R.drawable.ic_rewind_10), "skip_10")
			}
			FilledTonalIconButton(
				onClick = { viewModel.onPlayPauseClick() },
			) {
				when (state) {
					PlayerState.PLAYING -> Icon(painterResource(R.drawable.ic_pause), "pause")
					PlayerState.PAUSED -> Icon(painterResource(R.drawable.ic_play), "play")
					PlayerState.UNKNOWN -> Icon(painterResource(R.drawable.ic_play_pause), "play/pause")
				}
			}
			IconButton(
				onClick = { viewModel.onSkip10Click() },
			) {
				Icon(painterResource(R.drawable.ic_skip_10), "skip_10")
			}
			IconButton(
				onClick = { viewModel.onNextClick() },
			) {
				Icon(painterResource(R.drawable.ic_skip_next), "next")
			}
		}
	}
}