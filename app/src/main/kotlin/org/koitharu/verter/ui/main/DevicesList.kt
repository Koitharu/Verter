package org.koitharu.verter.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.koitharu.verter.R
import org.koitharu.verter.core.devices.RemoteDevice

@Composable
fun DevicesDropDownList(
	expanded: Boolean = false,
	list: List<RemoteDevice>,
	onDismissRequest: () -> Unit,
	onAddClick: () -> Unit,
	onItemSelected: (RemoteDevice) -> Unit,
) {
	DropdownMenu(
		modifier = Modifier.fillMaxWidth(),
		expanded = expanded,
		onDismissRequest = { onDismissRequest() },
	) {
		list.forEach {
			DropdownMenuItem(
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					onDismissRequest()
					onItemSelected(it)
				},
				text = {
					Text(
						text = it.displayName,
						modifier = Modifier.wrapContentWidth().align(Alignment.Start),
					)
				}
			)
		}
		Divider(
			modifier = Modifier.fillMaxWidth(),
		)
		DropdownMenuItem(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				onDismissRequest()
				onAddClick()
			},
			leadingIcon = {
				Icon(Icons.Default.Add, contentDescription = null)
			},
			text = {
				Text(
					text = stringResource(R.string.add_device),
					modifier = Modifier.wrapContentWidth().align(Alignment.Start),
				)
			}
		)
	}
}