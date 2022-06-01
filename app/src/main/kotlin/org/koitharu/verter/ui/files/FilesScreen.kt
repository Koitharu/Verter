package org.koitharu.verter.ui.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koitharu.verter.R

@Composable
fun FilesScreen(snackbarState: SnackbarHostState) {
	val viewModel = hiltViewModel<FilesViewModel>()
	val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		if (uri != null) viewModel.sendFile(uri)
	}
	val isLoading by viewModel.isLoading
	val context = LocalContext.current
	LaunchedEffect("results") {
		viewModel.doneFiles.onEach {
			val text = context.getString(R.string.file_transferred, it)
			snackbarState.showSnackbar(text)
		}.launchIn(this)
	}
	LaunchedEffect("errors") {
		viewModel.errors.onEach {
			it.message?.let { msg ->
				snackbarState.showSnackbar(msg)
			}
		}.launchIn(this)
	}
	Column(
		modifier = Modifier.fillMaxWidth().fillMaxHeight(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Icon(
			painter = painterResource(R.drawable.ic_file_transfer),
			modifier = Modifier.size(64.dp).padding(bottom = 12.dp),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
		)
		Text(
			text = stringResource(R.string.file_transfer_text),
			style = MaterialTheme.typography.titleMedium
		)
		if (isLoading) {
			CircularProgressIndicator(
				modifier = Modifier.padding(top = 12.dp),
			)
		} else {
			FilledTonalButton(
				modifier = Modifier.padding(top = 12.dp),
				onClick = { picker.launch(arrayOf("*/*")) },
				content = {
					Text(stringResource(R.string.pick_file))
				},
			)
		}
	}
}