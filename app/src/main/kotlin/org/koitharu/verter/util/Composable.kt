package org.koitharu.verter.util

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun LazyListState.isScrollingUp(): Boolean {
	var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
	var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
	return remember(this) {
		derivedStateOf {
			if (previousIndex != firstVisibleItemIndex) {
				previousIndex > firstVisibleItemIndex
			} else {
				previousScrollOffset >= firstVisibleItemScrollOffset
			}.also {
				previousIndex = firstVisibleItemIndex
				previousScrollOffset = firstVisibleItemScrollOffset
			}
		}
	}.value
}

@Composable
fun BackPressedEffect(enabled: Boolean = true, onBack: () -> Unit) {
	val currentOnBack by rememberUpdatedState(onBack)
	val backCallback = remember {
		object : OnBackPressedCallback(enabled) {
			override fun handleOnBackPressed() {
				currentOnBack()
			}
		}
	}
	SideEffect {
		backCallback.isEnabled = enabled
	}
	val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
		"No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
	}.onBackPressedDispatcher
	val lifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner, backDispatcher) {
		backDispatcher.addCallback(lifecycleOwner, backCallback)
		onDispose {
			backCallback.remove()
		}
	}
}