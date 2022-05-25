package org.koitharu.verter.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
	modifier: Modifier = Modifier,
	icon: Painter? = null,
	text: String,
	buttonContent: (@Composable RowScope.() -> Unit)? = null,
	onButtonClick: () -> Unit = {}
) = Column(
	modifier = Modifier.fillMaxWidth().fillMaxHeight().then(modifier),
	verticalArrangement = Arrangement.Center,
	horizontalAlignment = Alignment.CenterHorizontally,
) {
	if (icon != null) {
		Icon(
			painter = icon,
			modifier = Modifier.size(64.dp).padding(bottom = 12.dp),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.secondaryContainer,
		)
	}
	Text(
		text = text,
		style = MaterialTheme.typography.titleMedium
	)
	if (buttonContent != null) {
		FilledTonalButton(
			modifier = Modifier.padding(top = 12.dp),
			onClick = onButtonClick,
			content = buttonContent,
		)
	}
}