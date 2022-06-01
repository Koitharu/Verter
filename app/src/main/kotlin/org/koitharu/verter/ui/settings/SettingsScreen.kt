package org.koitharu.verter.ui.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import org.koitharu.verter.R
import org.koitharu.verter.ui.common.EmptyState

@Composable
fun SettingsScreen(snackbarState: SnackbarHostState) {
	EmptyState(
		icon = painterResource(R.drawable.ic_settings),
		text = "Not implemented",
	)
}